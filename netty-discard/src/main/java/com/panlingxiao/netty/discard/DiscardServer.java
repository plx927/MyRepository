package com.panlingxiao.netty.discard;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * Created by panlingxiao on 2016/4/11.
 * Netty 入门:
 * DiscardServer实现,服务器接收到客户端的信息之后不返回
 * 直接将收到的消息进行消费，只在服务器端输出。
 */
public class DiscardServer {


    public static void main(String[] args) {

        //源码分析NioEventLoopGroup
        NioEventLoopGroup parentGroup = new NioEventLoopGroup();
        NioEventLoopGroup childGroup = new NioEventLoopGroup();


        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(parentGroup,childGroup)
                 .bind(8080);


    }

}
