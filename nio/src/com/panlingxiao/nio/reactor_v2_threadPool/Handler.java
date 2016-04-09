package com.panlingxiao.nio.reactor_v2_threadPool;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/4/7.
 * 处理IO读写依旧Reactor,在Handler放入业务线程池中处理
 * 问题：业务处理在线程池中完成后，Reactor线程怎么知道？
 */
public class Handler implements Runnable {

    private SelectionKey key;
    private SocketChannel channel;
    private Selector selector;
    private static final int MAX_IN = 1024;
    private ByteBuffer byteBuffer = ByteBuffer.allocate(MAX_IN);

    /*
     * 存放响应客户端任务的队列，业务处理在线程池中处理完毕之后，将响应的任务存放在队列中
     * Reacotr线程将从任务队列中获取客户端响应的任务回写给客户端。
     */
    private Queue<Sender> taskQueue = new ConcurrentLinkedQueue<Sender>();

    private static final int READING = 0;
    private static final int END = -1;

    private static final Pattern END_PATTERN = Pattern.compile(".+([\r\n]|\r\n)$");


    //获取到CPU的核数，根据核心创建相应的线程
    private static final int N_CPU = Runtime.getRuntime().availableProcessors();
    private static Executor threadPool = Executors.newFixedThreadPool(N_CPU);

    public Handler(SocketChannel socketChannel, Selector selector) throws IOException{
        this.channel = socketChannel;
        this.selector = selector;
        //当获取连接后，如果客户端突然断开连接,那么设置非阻塞操作会抛异常
        socketChannel.configureBlocking(false);
        key = socketChannel.register(selector, SelectionKey.OP_READ, this);
    }

    @Override
    public void run() {
        try {
            read();
            Sender sender = null;
            //迭代写任务的队列，如果有对应的写任务队列,则执行写操作
            while (null != (sender = taskQueue.poll())) {
                sender.send();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void read() throws IOException {
        if (key.isValid()) {
            //在读取的时候客户端可能已经关闭了连接
            int n = channel.read(byteBuffer);
            //客户端关闭连接
            if (END == n) {
                key.cancel();
                channel.close();
                System.out.println("客户端关闭了连接");
            } else if (n > 0 && isComplete()) {
                //防止多个线程对读取的ByteBuffer进行并发操作
                byteBuffer.flip();

                ByteBuffer copyBuffer = ByteBuffer.allocate(byteBuffer.limit());
                copyBuffer.put(byteBuffer);

                //将positio设置为0,默认情况下positon==limit
                copyBuffer.position(0);

                //异步提交业务处理任务
                threadPool.execute(new BusinessProcessor(copyBuffer,channel,key));

                //清空ByteBuffer,用于下一次继续读取
                byteBuffer.clear();
            }
        }

    }

    /**
     * 判断是否已经读取数据完整
     *
     * @return 读取完成返回true, 否则返回false
     */
    private boolean isComplete() {
        byte[] array = byteBuffer.array();
        int positon = byteBuffer.position();
        String msg = new String(array, 0, positon);
        return END_PATTERN.matcher(msg).matches();
    }


    /**
     * 业务处理任务
     */
    class BusinessProcessor implements Runnable {

        private final SocketChannel channel;
        private ByteBuffer copyBuffer;
        private SelectionKey key;

        public BusinessProcessor(ByteBuffer copyBuffer,SocketChannel channel,SelectionKey key) {
            this.copyBuffer = copyBuffer;
            this.channel = channel;
            this.key = key;
        }

        @Override
        public void run() {
            byte[] array = copyBuffer.array();
            int position = copyBuffer.limit();
            String msg = new String(array, 0, position);
            System.out.printf("服务器收到了:" + msg);
            taskQueue.offer(new Sender(copyBuffer, channel,key));

            /*
             * 如果select在wakeup之后发生,则会导致当前写处理被延迟到下一次进行
             * 因此将该Selection添加一个写事件，保证能够有写事件发生
             */
            int ops = key.interestOps();
            key.interestOps(ops | SelectionKey.OP_WRITE);
            selector.wakeup();
        }
    }

    class Sender {

        private ByteBuffer writeBuffer;
        private SocketChannel channel;
        private SelectionKey key;

        public Sender(ByteBuffer writeBuffer,SocketChannel channel,SelectionKey key) {
            this.writeBuffer = writeBuffer;
            this.channel = channel;
            this.key = key;
        }

        public void send(){
            try {
                if (key.isValid()) {
                    channel.write(writeBuffer);
                    key.interestOps(SelectionKey.OP_READ);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("发送数据失败");
                key.cancel();
                try {
                    channel.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
        }
    }


}
