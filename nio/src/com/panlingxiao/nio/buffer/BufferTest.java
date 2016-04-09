package com.panlingxiao.nio.buffer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BufferTest {

    public static void main(String[] args) {

        RandomAccessFile randomAccessFile = null;
        try {
            File file = new File("./nio/nio.iml");
            System.out.println(file.getAbsoluteFile());

            randomAccessFile = new RandomAccessFile(file, "r");

            ByteBuffer buffer = ByteBuffer.allocate(1024);

            FileChannel channel = randomAccessFile.getChannel();

            StringBuilder builder = new StringBuilder();
            int len = 0;
            while (-1 != (len = channel.read(buffer))) {
                buffer.flip();
                builder.append(new String(buffer.array()));
                buffer.clear();
            }
            System.out.println(builder);
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != randomAccessFile) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
