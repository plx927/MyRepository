package com.panlingxiao.nio.selector;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;

public class TestSelector {

    static Selector selector;

    public static void main(String[] args) throws IOException {

         //通过系统默认的SelectionProvider来创建Selector
         selector = Selector.open();

        /**
         * 获取到所有将要执行IO操作的Channel所对应的SelectionKey
         * 该集合是不支持添加操作的，它里面的元素是Selector在执行select操作时所动态产生的。
         * 用户可以移除里面的SelectionKey。
         */
        Set<SelectionKey> selectionKeys = selector.selectedKeys();
        selectionKeys.add(null);

    }
}
