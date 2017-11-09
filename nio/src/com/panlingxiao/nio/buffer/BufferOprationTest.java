package com.panlingxiao.nio.buffer;

import java.nio.ByteBuffer;

/**
 * Created by Administrator on 2016/4/7.
 */
public class BufferOprationTest {

    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put("hello".getBytes());

        String str = new String(buffer.array()).trim();
        System.out.println(str);

        ByteBuffer buffer2 = ByteBuffer.allocate(1024);
        buffer2.put(buffer);
        //position为1024-5=1019
        System.out.println(buffer2);




    }
}
