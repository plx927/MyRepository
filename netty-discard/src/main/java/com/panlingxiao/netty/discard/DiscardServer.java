package com.panlingxiao.netty.discard;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Created by panlingxiao on 2016/4/11.
 * Netty 入门:
 * DiscardServer实现,服务器接收到客户端的信息之后不返回
 * 直接将收到的消息进行消费，只在服务器端输出。
 */
public class DiscardServer {


    public static void main(String[] args) throws InterruptedException {
        final int port = 8080;
        /*
         * NioEventLoopGroup:multithreaded event loop that handles I/O operation
         * Netty官方给出的这句话对于初学者是很难理解的。
         *
         * EventLoop是用于处理在Selector中注册的Channel的IO事件。
         */

        /*
         * 最初的理解:用于接受客户端的连接,可以简单的理解成MainReactor
         *
         * 源码分析的理解:创建CPU核数*2个NioEventLoop，同时创建默认的Executor和默认的SelectorProvider。
         * 默认的SelectorProvider是系统所提供的，有sun.nio.ch.DefaultSelectorProvider来创建。
         *
         *  对于完整的NioEventLoopGroup的构造方法的参数列表如下:
         *  public NioEventLoopGroup(int nEventLoops, Executor executor, final SelectorProvider selectorProvider) {
         *       super(nEventLoops, executor, selectorProvider);
         *   }
         */
        NioEventLoopGroup boosGroup = new NioEventLoopGroup();

        /*
         * 用于注册客户端的连接IO事件
         * 映射关系:
         * 每一个Channel在一个Selector，
         * 每一个Selector每一个线程所处理。
         */
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            //通过ServerBootstrap工具类用于创建Server
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boosGroup, workerGroup)
                     /*
                      * 通过指定服务器端Channel的实现类来接口客户端的连接
                      * 可以先简单地理解NioServerSocketChannel是对JDK原生
                      * 的ServerSocketChannel的代理实现。
                      */
                    .channel(NioServerSocketChannel.class)
                      /*
                       * ChannelInitializer是一个特殊的ChannelHandler
                       * 其用于帮助用户配置新连接进行的Channel。
                       *  It is most likely that you want to configure the ChannelPipeline of the new Channel
                       *  by adding some handlers such as DiscardServerHandler to implement your network application
                、。       *  上面的这句话说明每一个Channel都有其对应的ChannelPipeline
                       *
                       */
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new DiscardChannelHandler());
                        }
                    })
                    //option是为NioServerSocketChannel来设置的
                    .option(ChannelOption.SO_BACKLOG, 128)
                    //childOption是为获取到的Channel来设置的
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

        /*
         * Netty中的方法都是异步执行的,通过sync当前线程等待，
         * 直到绑定端口成功后才继续执行，
         * 通过抛出的异常是InterruptedException，也可以看出
         * 当执行sync的时候，当前线程会处于waiting状态
         *
         * 注意:源码分析Netty底层对于将一个异步执行的处理转成同步是如何处理的。
         */
            ChannelFuture f = bootstrap.bind(port).sync();


            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();


        } finally {
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }


    }

}
