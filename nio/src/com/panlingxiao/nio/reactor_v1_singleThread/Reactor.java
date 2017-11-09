package com.panlingxiao.nio.reactor_v1_singleThread;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by panlingxiao on 2016/4/6.
 * <p/>
 *
 * <pre>
 * 单线程模式下的Reactor实现
 * 分析
 * 1.Reactor:
 * responds to IO events by dispatching the appropriate handler
 * 通过将客户端的所产生的IO事件派发给相应的Handler进行处理
 *
 * 2.Handler:
 * perform non-blocking actions
 * 执行非阻塞的操作,读取数据、业务处理、响应请求(回写数据)
 *
 *
 * </pre>
 */
public class Reactor implements Runnable {

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    public Reactor() {
        try {
            //创建SelectorPrivoder,默认使用系统提供的SelectorProvider
            SelectorProvider provider = SelectorProvider.provider();
            selector = provider.openSelector();
            serverSocketChannel = provider.openServerSocketChannel();

            //配置Channel为非阻塞模式
            serverSocketChannel.configureBlocking(false);

            //注册ServerSocketChannel所关注的IO事件
            SelectionKey key = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            //设置IO连接事件的处理器
            key.attach(new Acceptor(serverSocketChannel,selector));

            //绑定8080端口
            serverSocketChannel.bind(new InetSocketAddress((8080)));
            System.out.println("服务器启动成功!");
        } catch (IOException e) {
            e.printStackTrace();
            try {
                if (null != selector) {
                    selector.close();
                }
            } catch (IOException e1) {
                e.printStackTrace();
            }

            try {
                if (null != serverSocketChannel) {
                    serverSocketChannel.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        Set<SelectionKey> selectionKeys = null;
        for (; ; ) {
            try {
                //Selector等待IO事件,如果没有IO事件发生,该方法会阻塞当前线程继续执行
                selector.select();

                //获取发送IO事件的Channel所对应的SelectionKey
                selectionKeys = selector.selectedKeys();

                //Reactor只负责将IO事件派发给对应的Handler进行处理
                for(Iterator<SelectionKey> iterator = selectionKeys.iterator();iterator.hasNext();){
                    SelectionKey key = iterator.next();
                    dispatch(key);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(selectionKeys != null){
                    selectionKeys.clear();
                }
            }

        }
    }

    /**
     * 将IO事件派发给对应的Handler
     * @param key 发送IO事件的Channel对应的SelectionKey
     */
    private void dispatch(SelectionKey key) {
        Runnable runnable  = (Runnable) key.attachment();
        if(runnable != null){
            runnable.run();
        }
    }


    public static void main(String[] args) {
        new Reactor().run();
    }

}
