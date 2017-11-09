package com.panlingxiao.nio.reactor_v3_mainsub;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by panlingxiao on 2016/4/11.
 * 封装ServerSocketChannel
 */
public class MainReactor {


    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private  SubReactor[] subReactors;
    private int nextSubReactor;



    public MainReactor(InetSocketAddress address,SubReactor[] subReactors) throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(address);
        this.subReactors = subReactors;
        serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);
    }

    public void accept() {
        Set<SelectionKey> keys = null;
        System.out.println("Main/Sub Reactor NIO 服务器启动成功!");
        try {
            for (; ; ) {
                selector.select();
                keys = selector.selectedKeys();
                for (Iterator<SelectionKey> iterator = keys.iterator(); iterator.hasNext(); ) {
                    SelectionKey key = iterator.next();
                    if(key.isValid() && key.isAcceptable()) {
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        if(socketChannel != null){
                            socketChannel.configureBlocking(false);
                            SubReactor nioProcessor = getNextSubReactor();
                            System.out.println(String.format("服务器端接受到一个连接:通过%s进行处理",nioProcessor));
                            //异步进行注册,将注册操作交给SubReactor
                            nioProcessor.registerConnection(socketChannel);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (keys != null) {
                keys.clear();
            }
        }
    }

    /**
     * 轮询获取NIOProcessor
     * @return
     */
    private SubReactor getNextSubReactor() {
        if(++nextSubReactor == subReactors.length) {
            nextSubReactor = 0;
        }
        return subReactors[nextSubReactor];
    }


}
