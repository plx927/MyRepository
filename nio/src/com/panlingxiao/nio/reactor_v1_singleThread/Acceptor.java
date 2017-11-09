package com.panlingxiao.nio.reactor_v1_singleThread;

import java.io.IOException;
import java.nio.channels.*;

/**
 * Created by panlingxiao on 2016/4/6.
 * <p/>
 *
 * 专门用户处理客户端连接的IO事件
 *
 */
public class Acceptor implements Runnable {

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    public Acceptor(ServerSocketChannel serverSocketChannel,Selector selector){
        this.serverSocketChannel = serverSocketChannel;
        this.selector = selector;
    }

    @Override
    public void run() {
        SocketChannel socketChannel = null;
        try {
            /*
             *   获取到客户端连接,在非阻塞模式下，如果没有连接则直接返回或者连接已经被获取
             *   则直接返回，并且返回的SocketChannel为null
             */
            socketChannel  = serverSocketChannel.accept();
            if (socketChannel != null) {
                System.out.println("服务器端收到一个连接");
                //设置获取SocketChannel为非阻塞
                socketChannel.configureBlocking(false);
                new Handler(socketChannel,selector);
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                if(null != socketChannel) {
                    socketChannel.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

}
