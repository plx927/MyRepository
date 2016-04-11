package com.panlingxiao.nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by panlingxiao on 2016/4/5.
 *
 * 修改版本，观察将一个Channel的读取放入到一个多的线程中出现的问题。
 * 由于异步操作，会导致在一次select的时候,数据可能还没有被读取，因此select
 * 操作会继续执行，从而又产生了多个读任务，此时读取的数据可能就会被分别被2个线程读取,从而导致数据的错乱。
 *
 */
public class NioEchoServer2 {

    private static ServerSocketChannel serverSocketChannel;

    private static Selector selector;

    //创建线程池
    private static Executor threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private static BlockingQueue<SelectionKey> keyQueue = new LinkedBlockingDeque<SelectionKey>();

    public static void main(String[] args) {

        try {
            //创建Server端的Channel
            serverSocketChannel = ServerSocketChannel.open();

            //配置Channel为非阻塞
            serverSocketChannel.configureBlocking(false);

            /*
             *  创建多路复用器,所有Channel的IO事件都由多路复用器都获取
             *  从而处理对应的IO事件
             */
            selector = Selector.open();

            /*
             * 将ServerSocketChannel注册到Selector中，并且指定其感兴趣的IO操作
             * ServerSocketChannel只关注接受到客户端连接的事件
             */
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            serverSocketChannel.bind(new InetSocketAddress(8080));

            System.out.println("服务器启动成功,服务器地址:" + serverSocketChannel.getLocalAddress());


            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    SelectionKey key = null;
                    try {
                        while ((key = keyQueue.take()) != null) {
                            try {
                                /**
                                 * 直接将IO连接、IO读取、业务处理、IO回写都直接放入线程池
                                 * 造成连接失败、读取失败等问题。
                                 */
                                if (key.isValid()) {
                                    handleIOEvent(key);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                key.cancel();
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            for (; ; ) {

                /*
                 * Selector监听注册过的Channel的IO事件
                 * 如果没有发生，会阻塞当前线程执行，直到有IO事件发生
                 */
                selector.select();
                //获取要执行IO操作的Channel
                Set<SelectionKey> keys = selector.selectedKeys();
                try {
                    for (SelectionKey key : keys) {
                        if (key.isValid()) {
                            if(key.isAcceptable()) {
                                handleAccept(key);
                            }
                            if(key.isReadable()){
                                try {
                                    keyQueue.put(key);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            key.cancel();
                        }
                    }
                } finally {
                    keys.clear();
                }

            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != serverSocketChannel) {
                try {
                    serverSocketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (selector != null) {
                try {
                    selector.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }


    /**
     * 处理客户端连接操作
     * @param key
     * @throws IOException
     */
    private static void handleAccept(SelectionKey key) throws IOException {
        //产生连接事件的ServerSocketChannel
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        //获取客户端的Socket连接的SocketChannel
        SocketChannel socketChannel = serverSocketChannel.accept();
        System.out.println(socketChannel);
        //默认的Channel为阻塞IO,因此需要配置成非阻塞
        socketChannel.configureBlocking(false);
        //将SocketChannel
        socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
    }


    public static void handleIOEvent(final SelectionKey key) throws IOException {
        //读取客户端发送的数据
        if (key.isReadable()) {
            handleRead(key);
        }
    }

    /**
     * 处理读取操作
     * @param key
     * @throws IOException
     */
    private static void handleRead(SelectionKey key) throws IOException {

        //通过attachement获取ByteBuffer
        ByteBuffer buffer = (ByteBuffer) key.attachment();

        //通过Channel获取SelectionKey对应的Channel
        SocketChannel channel = (SocketChannel) key.channel();

        //通过channel将接受到的数据存入到buffer中
        int len = 0;

        /**
         *  模拟一个场景:
         *  1. Channel循序读取数据：
         *  当该Channel还在读取的时候，客户端又发送数据，此时后面发送数据是否是在当前的操作中所完成？
         *  测试结果：当在读取的过程中不会继续接受，会再下一次操作中读取。
         *
         *  补充问题:如果多次读取后,下一次select的时候是否会将会依然保留之前的OP_READ操作。
         *  测试结果:不会。
         *   while(0 < (len = channel.read(buffer))){
         *       System.out.println("读取字节的长度为:"+len);
         *       buffer.clear();
         *    }
         *
         *
         *  2.如果使用单次读取
         *  如果使用一次读取的方式进行处理，在读取的过程中，后面客户端可能连续发送了很多数据，
         *  这些数据会被底层的Socket所缓存起来，在下一次IO读取操作的时候一次性全部返回。
         *
         *  len = channel.read(buffer);
         *
         */

        len = channel.read(buffer);
        System.out.println("读取字节的长度为:" + len);
        /**
         * 当使用telnet直接关闭连接的时候,服务器端接受到的字节数为-1
         */
        if (-1 == len) {
            /**
             * 将当前的SelectionKey添加到cancelled-key集合中
             * 在下一次执行selection操作的时候,将这个SelectionKey从
             * key set中移除,并且将对应的Channel从Selector中注销
             */
            //key.cancel();
            channel.close();
            System.out.println("客户端断开了连接!");
        }
    }




}
