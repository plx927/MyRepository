package com.panlingxiao.nio.reactor_v1_singleThread;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.regex.Pattern;

/**
 * Created by panlingxiao on 2016/4/6.
 *
 * 处理IO读写的Handler
 */
public class Handler implements  Runnable{

    private static final int BUFFER_SIZE = 1024;

    //存放读取数据的缓冲区
    private ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);

    //存放响应数据的缓冲区
    private ByteBuffer writeBuffer = ByteBuffer.allocate(BUFFER_SIZE);

    //判断是否已经读取完毕的正则
    private  static Pattern READ_COMPLETE_PATTERN = Pattern.compile("^.*([\r\n]|\r\n)$");

    private static final int END = -1;
    private static final int READING = 0;
    private static final int WRITING = 1;
    //表示服务器端不再与客户端进行会话通信
    private static final int COMPLETE = 2;
    private int state = READING;

    //当前Handler对象所维护的SocketChannel
    private SocketChannel channel;

    private SelectionKey key;

    public Handler(SocketChannel channel, Selector selector) throws ClosedChannelException {
            this.channel = channel;
            /*
             * 在将Channel注册到Selector的时候，客户端可能会断开连接
             * 此时会抛出ClosedChannelException
             */
            key = channel.register(selector,SelectionKey.OP_READ,this);

    }

    @Override
    public void run() {
        try {
            //执行读取客户端数据的处理
            if (state == READING) {
                read();
            } else {
                write();
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                key.cancel();
                channel.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * 读取客户端发送的数据
     */
    private void read() throws IOException {
        if (isReadComplete()) {
            //判断key是否被加入到cancel key中
            if (key.isValid()) {
                process();

                if(state != COMPLETE) {
                /*
                 *  改进的方法: 使用State-Object pattern
                 *  sk.attach(new Sender());
                 *  sk.interest(SelectionKey.OP_WRITE);
                 *  sk.selector().wakeup();
                 */
                    state = WRITING;

                    readBuffer.flip();

                    //将读取的Buffer放入到即将要写出去的ByteBuffer中
                    writeBuffer.put(readBuffer);

                    //清空Buffer，供下一次读取使用
                    readBuffer.clear();

                    //将Channel的关注事件设置成写操作
                    key.interestOps(SelectionKey.OP_WRITE);
                }else{
                    key.cancel();
                    channel.close();
                }
            }
        }
    }


    /**
     * 服务器将客户端发送的数据回写回去
     */
    private void write() throws IOException {
        if(key.isValid()) {
            writeBuffer.flip();
            System.out.println("服务器响应数据:" + new String(writeBuffer.array(),0,writeBuffer.limit()).trim());
            channel.write(writeBuffer);
            key.interestOps(SelectionKey.OP_READ);
            state = READING;
            writeBuffer.clear();
        }
    }




    /**
     * 判断是否读取完毕,这里就是通过判断请求的内容是否是\r\n结尾,
     * 如果读取完毕则进行业务处理,否则继续读取。
     * 注:这里没有去判断Buffer是否满了之后对ByteBuffer进行扩容的问题
     *
     * @return 当读取完成返回true,否则返回false
     */
    private boolean isReadComplete() throws IOException {
        boolean flag = false;
        /*
         *  在读取的时候可能客户端已经关闭了连接,此时会抛出IOException
         *  A socket channel whose write half has
         *  been shut down, for example, may still be open for reading.
         */
        int n = channel.read(readBuffer);
        if (n > 0) {
            byte[] data = readBuffer.array();
            int position = readBuffer.position();
            String msg = new String(data, 0, position);
            if (READ_COMPLETE_PATTERN.matcher(msg).matches()) {
                flag = true;
            }
            //当读取字节数为-1，则表示客户端已经断开了连接
        } else if(n == END){
            System.out.println("客户端断开连接");
            flag = true;
            state = COMPLETE;
        }
        return flag;
    }

    /**
     * 处理业务逻辑,这里只将客户端的输入在控制台打印
     */
    private void process() {
        int offset = readBuffer.position();
        String msg = new String(readBuffer.array(),0, offset).trim();
        System.out.println(String.format("服务器收到了:%s",msg));
        if("bye".equalsIgnoreCase(msg)){
            state = COMPLETE;
        }
    }


}
