package com.panlingxiao.nio.reactor_v2_threadPool;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by Administrator on 2016/4/7.
 * 处理客户端的连接，Acceptor在单线程与多线程模式下是一样的。
 */
public class Acceptor implements  Runnable {

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;


    public Acceptor(ServerSocketChannel serverSocketChannel,Selector selector){
        this.serverSocketChannel = serverSocketChannel;
        this.selector = selector;
    }


    @Override
    public void run() {
        SocketChannel socketChannel = null;
        try {
            socketChannel = serverSocketChannel.accept();
            if(socketChannel != null) {
                new Handler(socketChannel, selector);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("获取客户端连接失败");
            try {
                if(socketChannel != null){
                    socketChannel.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
