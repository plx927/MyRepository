package com.panlingxiao.nio.selector;

import java.nio.channels.Selector;

/**
 * Created by Administrator on 2016/4/5.
 */
public class TestCloseSelector {
    static Selector selector;

    public static void main(String[] args) throws  Exception{

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    /**
                     * 过了1秒，关闭Selector
                     * 主线程会被中断
                     */
                    Thread.sleep(1000);
                    selector.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        selector = Selector.open();
        /**
         *  该操作是一个阻塞操作,会导致当前线程无法向下继续执行
         *  一个阻塞的方法并不是等价于线程进入阻塞状态,
         *  通过Jstack分析可以看到当前主线程依旧处于Running状态
         */
        selector.select();
    }
}
