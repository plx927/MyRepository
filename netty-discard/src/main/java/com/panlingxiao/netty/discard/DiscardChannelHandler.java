package com.panlingxiao.netty.discard;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * Created by panlingxiao on 2016/4/11.
 *
 * ChannelHandler是Netty提供给用户处理I/O事件的接口
 *
 * 这里通过适配器模式,直接继承ChannelHandlerAdapter,
 * 重写对应需要的方法，而无需实现ChannelHandler来实现接口中的每一个方法。
 *
 * 源码分析ChannelHandler的体系结构和设计原则
 */
public class DiscardChannelHandler extends ChannelHandlerAdapter{

    /**
     * 当发送IO异常或者Handler在处理事件的过程中抛出异常时，都将执行该方法
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        //当发生异常时关闭与客户端的连接
        ctx.close();
    }


    /**
     * 当前Channel读取到客户端发送的消息所调用的方法
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        /*
         * 由于没有对数据进行编解码,因此默认读取到的数据类型为ByteBuf
         */
        ByteBuf in = (ByteBuf) msg;
        try {
            //判断是否已经将ByteBuf中的字节读取完毕
            while(in.isReadable()){
                System.out.print((char)in.readByte());
                System.out.flush();
            }
        } finally {
            /*
             * ByteBf是一个引用计数的对象,在使用完必须显示的释放
             * 否则即使它不再被使用也不会被回收
             */
            in.release();
        }
    }


    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        System.out.println("客户端断开连接");
        super.disconnect(ctx, promise);
    }


    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        System.out.println("关闭连接");
        //底层默认使用 ctx.close(promise);
        super.close(ctx, promise);
    }


}
