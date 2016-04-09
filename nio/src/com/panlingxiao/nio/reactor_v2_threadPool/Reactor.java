package com.panlingxiao.nio.reactor_v2_threadPool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Administrator on 2016/4/7.
 *
 *  多线程模式下的Reactor实现:
 *  将IO的连接、读取放入到Reactor线程中处理
 *  将业务处理放入到线程池中，通过工作线程处理,从而加快Reacotr线程的响应速度。
 *
 */
public class Reactor implements Runnable{

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    public Reactor() {
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(8080));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, new Acceptor(serverSocketChannel, selector));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        System.out.println("服务器启动成功!");
        Set<SelectionKey> set = null;
        for(;;){
            try {
                selector.select();
                set =  selector.selectedKeys();
                for(Iterator<SelectionKey> iterator = set.iterator();iterator.hasNext();){
                    SelectionKey key = iterator.next();
                    dispatch(key);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(set != null){
                    set.clear();
                }
            }
        }
    }

    private void  dispatch(SelectionKey key){
        Runnable runnable = (Runnable) key.attachment();
        if(runnable != null){
            runnable.run();
        }

    }


    public static void main(String[] args) {
        new Reactor().run();
    }
}
