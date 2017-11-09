package com.panlingxiao.concurrency.aqs;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * Created by panlingxiao on 2016/5/3.
 */
public class BooleanLatch {

    private static class Sync extends AbstractQueuedSynchronizer {


        /**
         * 尝试以共享的模式获取,该方法应该查询同步器的状态，判断其是否允许可以以共享的模式来进行获取。
         * 如果该方法报告线程无法获取，则执行线程就进入到阻塞队列。
         * 当其进入到队列后，直到其他线程通知该线程，它才会从队列中被释放。
         *
         * @param arg 当返回负值，则说明该线程获取失败
         *            如果返回值大于0，则说明线程以共享的模式获取成功，并且后面的线程也可以通过共享的模式获取。
         *            0表示以共享模式成功获取，但是后继节点无法获取
         * @return
         */
        @Override
        protected int tryAcquireShared(int arg) {
            return getState() != 0 ? 0 : -1;
        }

        protected boolean tryReleaseShared(int ignore) {
            setState(1);
            return true;
        }

    }

    private Sync sync = new Sync();


    public void signal() {
        sync.releaseShared(1);
    }


    /**
     * 以线程可中断、共享的模式来获取同步器的拥有权。
     * 该方法会首先检查当前线程的中断标志位，如果发现线程已经被中断，则直接抛出中断异常。
     * 否则线程会尝试去获取AQS的拥有权，如果获取失败,那么线程就会进入到AQS的阻塞队列中。
     *
     * @throws InterruptedException
     */
    public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

}
