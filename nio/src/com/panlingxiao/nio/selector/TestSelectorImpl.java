package com.panlingxiao.nio.selector;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by panlingxiao on 2016/4/17.
 *
 * Selector底层维护的SelectionKey的Set测试
 * Netty底层对于SelectionKey集合的优化措施
 */
public class TestSelectorImpl {

    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Class<?> selectorClass = Class.forName("sun.nio.ch.SelectorImpl");
        Selector selector = Selector.open();
        Field publicKeysField = selectorClass.getDeclaredField("publicKeys");
        Field publicSelectedKeysField = selectorClass.getDeclaredField("publicSelectedKeys");

        publicKeysField.setAccessible(true);
        publicSelectedKeysField.setAccessible(true);


        /**
         * Netty中是使用自己所编写的Set替换JDK中默认的HashSet。
         * 这样就会导致通过keys返回的Set是可以被修改的。
         */
       // Set<SelectionKey> publicKeys = new HashSet<SelectionKey>();
       // Set<SelectionKey> publicSelectedKeys = new HashSet<SelectionKey>();

        Field keysField = selectorClass.getDeclaredField("keys");
        Field selectedKeysField = selectorClass.getDeclaredField("selectedKeys");
        keysField.setAccessible(true);
        selectedKeysField.setAccessible(true);

        Set<SelectionKey> publicKeys = (Set<SelectionKey>) keysField.get(selector);
        Set<SelectionKey> publicSelectedKeys = (Set<SelectionKey>) selectedKeysField.get(selector);;

        publicKeysField.set(selector,publicKeys);
        publicSelectedKeysField.set(selector, publicSelectedKeys);


        Set<SelectionKey> keys = selector.keys();
        Set<SelectionKey> selectionKeys = selector.selectedKeys();
        System.out.println(keys);
        System.out.println(selectionKeys);

        publicKeys.add(new MySelectionKey("hello"));
        publicKeys.add(new MySelectionKey("world"));


        System.out.println(keys);
        System.out.println(selectionKeys);
    }

    static  class MySelectionKey extends  SelectionKey{

        String name;

        public MySelectionKey(String name){
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public SelectableChannel channel() {
            return null;
        }

        @Override
        public Selector selector() {
            return null;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public void cancel() {

        }

        @Override
        public int interestOps() {
            return 0;
        }

        @Override
        public SelectionKey interestOps(int ops) {
            return null;
        }

        @Override
        public int readyOps() {
            return 0;
        }
    }
}
