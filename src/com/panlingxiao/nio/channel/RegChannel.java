package com.panlingxiao.nio.channel;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;

/**
 * Created by Administrator on 2016/4/8.
 */
public class RegChannel {

    public static void main(String[] args) throws  Exception{
        /**
         * 创建ServerSocketChannel的SelectorProvider和创建Selector的SelectorProvider
         * 必须是同一个对象，否则会引发IllegalSelectorException
         */
        SelectorProvider provider = SelectorProvider.provider();
        Selector selector = provider.openSelector();
        ServerSocketChannel serverSocketChannel = provider.openServerSocketChannel();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);


    }
}
