package com.panlingxiao.nio.selector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Administrator on 2016/4/8.
 * <p/>
 * 测试将多个Channel注册到Selector中
 */
public class SynSelector {

    static Selector selector1;
    static Selector selector2;
    static ServerSocketChannel serverSocketChannel;

    public static void main(String[] args) throws Exception {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        selector1 = Selector.open();
        selector2 = Selector.open();

        serverSocketChannel.bind(new InetSocketAddress(8080));

        serverSocketChannel.register(selector1, SelectionKey.OP_ACCEPT);
        serverSocketChannel.register(selector2, SelectionKey.OP_ACCEPT);


        MyThread t1 = new MyThread(selector1);
        MyThread t2 = new MyThread(selector2);

        t1.start();
        t2.start();

    }

    static class MyThread extends Thread {
        Selector selector;

        MyThread(Selector selector) {
            this.selector = selector;
        }

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + ":start");
            Set<SelectionKey> keys = null;
            for (; ; ) {
                try {
                    selector1.select();
                    System.out.println(Thread.currentThread().getName() + " have IO Event");
                   keys = selector1.selectedKeys();
                    for (Iterator<SelectionKey> iterator = keys.iterator(); iterator.hasNext(); ) {
                        SelectionKey key = iterator.next();
                        if (key.isAcceptable()) {
                            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            if(socketChannel != null) {
                                socketChannel.configureBlocking(false);
                                //将socketChannel同时像多个Selector中注册
                                socketChannel.register(selector1, SelectionKey.OP_READ);
                                socketChannel.register(selector2, SelectionKey.OP_READ);
                            }
                            System.out.println(Thread.currentThread().getName()+",socketChannel is "+socketChannel);
                        }else if(key.isReadable()){
                            SocketChannel channel  = (SocketChannel) key.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            int n = channel.read(buffer);
                            System.out.println(Thread.currentThread().getName()+",读取到数据: "+new String(buffer.array(),0,buffer.position()));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    if(null != keys){
                        keys.clear();
                    }
                }

            }
        }
    }
}


