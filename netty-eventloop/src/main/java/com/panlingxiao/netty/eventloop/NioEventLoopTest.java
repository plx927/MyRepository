package com.panlingxiao.netty.eventloop;

import io.netty.channel.nio.NioEventLoopGroup;

/**
 * Created by panlingxiao on 2016/4/17.
 */
public class NioEventLoopTest {

    public static void main(String[] args) {

        NioEventLoopGroup boss = new NioEventLoopGroup();

        boss.execute(new Runnable() {
            public void run() {
                System.out.println("hello world!");
            }
        });

        boss.shutdownGracefully();
    }
}
