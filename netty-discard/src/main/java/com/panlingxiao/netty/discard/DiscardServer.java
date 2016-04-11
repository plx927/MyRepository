package com.panlingxiao.netty.discard;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * Created by panlingxiao on 2016/4/11.
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
