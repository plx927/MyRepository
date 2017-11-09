package com.panlingxiao.nio.reactor_v3_mainsub;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

/**
 * Created by panlingxiao on 2016/4/11.
 * SubReactor
 */
public class SubReactor {

    private Selector selector;
    private ReadReactor reactorR;
    private WriteReactor reactorW;
    private int number;
    private static final int BASE_IN_CAPACITY = 1024;

    private static Pattern pattern = Pattern.compile(".*([\r\n]|\r\n)$");

    //业务线程池
    private Executor businessThreadPool;

    public SubReactor(int number,Executor businessThreadPool) throws IOException {
        this.number = number;
        selector = Selector.open();
        reactorR = new ReadReactor();
        reactorW = new WriteReactor();
        new Thread(reactorR,"ReadReactor-"+number).start();
        new Thread(reactorW,"WriteReactor-"+number).start();
        this.businessThreadPool = businessThreadPool;
    }

    public void registerConnection(SocketChannel socketChannel) {
        reactorR.registerQueue.offer(socketChannel);
        selector.wakeup();
    }


    @Override
    public String toString() {
        return number + "号 SubReactor";
    }

    /**
     * 处理连接和读取操作操作
     */
    public class ReadReactor implements Runnable {
        //注册接受到的SocketChannel的队列
        private ConcurrentLinkedQueue<SocketChannel> registerQueue = new ConcurrentLinkedQueue<SocketChannel>();
        @Override
        public void run() {
            Set<SelectionKey> keys = null;
            for(;;){
                try {
                    selector.select();
                    //处理客户端的注册
                    handleRegister();
                    keys = selector.selectedKeys();
                    for(Iterator<SelectionKey> iterator = keys.iterator();iterator.hasNext();){
                        SelectionKey key = iterator.next();
                        if(key.isValid() && key.isReadable()){
                            read(key);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        /**
         * 读取数据
         * @param key
         */
        private void read(SelectionKey key) {
            ByteBuffer buffer = (ByteBuffer) key.attachment();
            checkBuffer(buffer, key);
            SocketChannel socketChannel = (SocketChannel) key.channel();
            try {
                int n = socketChannel.read(buffer);
                if(n == -1){
                    throw new IOException("客户端已经断开了连接");
                }
                if(isReadComplete(buffer)){
                    buffer.flip();
                    businessThreadPool.execute(new BusinessHandler(ByteBuffer.wrap(buffer.array(), 0, buffer.limit()),socketChannel));
                    if(buffer.capacity() > BASE_IN_CAPACITY){
                        //异步执行业务处理
                        key.attach(ByteBuffer.allocate(BASE_IN_CAPACITY));
                    }else{
                        buffer.clear();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("读取数据失败");
                key.cancel();
                try {
                    if(socketChannel.isOpen()) {
                        socketChannel.close();
                        System.out.println("服务器关闭连接");
                    }
                } catch (IOException e2) {
                    e.printStackTrace();
                }
            }

        }

        /**
         * 判断内容是否读取完整
         * @param buffer
         * @return
         */
        private boolean isReadComplete(ByteBuffer buffer) {
            byte[] array = buffer.array();
            int position = buffer.position();
            String str = new String(array, 0, position);
            return pattern.matcher(str).matches();
        }

        /**
         * 检查当前ByteBuffer是否还有剩余空间
         * @param buffer
         */
        private void checkBuffer(ByteBuffer buffer,SelectionKey key) {
            //如果Buffer剩余的空间小于50个字节，就将其进行扩展
            if(buffer.remaining() < 50){
                int newCapacity = buffer.capacity() * 4 / 3;
                ByteBuffer extendedBuffer = ByteBuffer.allocate(newCapacity);
                buffer.flip();
                extendedBuffer.put(buffer);
                key.attach(extendedBuffer);
            }
        }

        private void handleRegister() {
            SocketChannel socketChannel = null;
            while(null != (socketChannel = registerQueue.poll())){
                try {
                    socketChannel.register(selector,SelectionKey.OP_READ, ByteBuffer.allocate(BASE_IN_CAPACITY));
                    System.out.println(Thread.currentThread().getName()+": 客户端注册事件成功");
                } catch (ClosedChannelException e) {
                    System.out.println("注册客户端连接失败,客户端连接已经关闭");
                    e.printStackTrace();
                    try {
                        socketChannel.close();
                        System.out.println("关闭已经断开的连接");
                    } catch (IOException e2) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 负责写操作
     */
    private class WriteReactor implements  Runnable{
        private LinkedBlockingQueue<WriteTask> writeQueue = new LinkedBlockingQueue<WriteTask>();
        @Override
        public void run() {
            WriteTask writeTask;
            for(;;){
                try {
                    if((writeTask = writeQueue.take()) != null){
                        //如果底层的缓冲区满了,则说明没有完全写出去,因此需要重新放入到队列继续写
                        if(!writeTask.writeComplete()){
                            writeQueue.offer(writeTask);
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     * 写任务
     */
    private class WriteTask {
        private ByteBuffer buffer;
        private SocketChannel socketChannel;

        public WriteTask(ByteBuffer buffer, SocketChannel socketChannel) {
            this.buffer = buffer;
            this.socketChannel = socketChannel;
        }

        public boolean writeComplete() {
            try {
                int limit =  buffer.limit();
                return limit == socketChannel.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
                return true;
            }
        }
    }

    /**
     * 业务处理器
     */
    private class BusinessHandler implements  Runnable{
        private ByteBuffer buffer;
        private SocketChannel socketChannel;


        public BusinessHandler(ByteBuffer buffer, SocketChannel socketChannel) {
            this.buffer = buffer;
            this.socketChannel = socketChannel;
        }

        @Override

        public void run() {
            int limit = buffer.limit();
            String str = new String(buffer.array(),0,limit);
            System.out.printf("服务器收到了信息:%s",str);
            reactorW.writeQueue.add(new WriteTask(buffer, socketChannel));
        }
    }
}
