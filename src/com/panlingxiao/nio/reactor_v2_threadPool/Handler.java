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

    private static Executor threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private int state = READING;

    public Handler(SocketChannel socketChannel, Selector selector) {
        try {
            this.channel = socketChannel;
            this.selector = selector;
            socketChannel.configureBlocking(false);
            key = socketChannel.register(selector, SelectionKey.OP_READ, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            if (READING == state) {
                read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void read() throws IOException {
        if (key.isValid()) {
            int n = channel.read(byteBuffer);
            //客户端关闭连接
            if (END == -1) {
                key.cancel();
                channel.close();
            } else if (n > 0 && isComplete()) {
                //防止多个线程对读取的ByteBuffer进行并发操作
                byteBuffer.flip();

                ByteBuffer copyBuffer = ByteBuffer.allocate(byteBuffer.limit());
                copyBuffer.put(byteBuffer);

                //异步提交业务处理任务
                threadPool.execute(new BusinessProcessor(copyBuffer));

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

    class Sender {

        private ByteBuffer writeBuffer;

        public Sender(ByteBuffer writeBuffer) {
            this.writeBuffer = writeBuffer;
        }

        public void send() throws IOException {
            if (key.isValid()) {
                channel.write(writeBuffer);
            }
        }
    }

    /**
     * 业务处理任务
     */
    class BusinessProcessor implements Runnable {

        private ByteBuffer copyBuffer;

        public BusinessProcessor(ByteBuffer copyBuffer) {
            this.copyBuffer = copyBuffer;
        }

        @Override
        public void run() {
            byte[] array = copyBuffer.array();
            int position = copyBuffer.position();
            String msg = new String(array, 0, position);
            System.out.printf("服务器收到了:" + msg);
            taskQueue.offer(new Sender(copyBuffer));
        }
    }


}
