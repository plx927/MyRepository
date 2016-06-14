package com.panlingxiao.concurrency.synchronier;

import javafx.concurrent.Worker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;


public class CyclicBarrierTest {

    static CyclicBarrier barrier;
    static float[][] matrix = {
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9}
    };

    static volatile boolean isDone = false;
    public static void main(String[] args) {

        Solver solver = new Solver(matrix);

    }



    static class Worker implements Runnable {
        int myRow;
        float sum =0;
        Worker(int row) {
            myRow = row;
        }

        public void run() {
            while (!done()) {
                processRow(myRow);
                try {
                    System.out.println("第N个阻塞:" + barrier.await());
                } catch (InterruptedException ex) {
                    return;
                } catch (BrokenBarrierException ex) {
                    return;
                }
            }
        }

        private void processRow(int myRow) {
            float[] row = matrix[myRow];
            for(int i = 0;i < row.length;i++){
                sum += row[i];
            }
        }


    }

    private static boolean done() {
        return isDone;
    }

    static class Solver {
        final int N;
        final float[][] data;

        private void mergeRows() {
            System.out.println("merge rows");
            //isDone = true;
        }

        public Solver(float[][] matrix) {
            data = matrix;
            N = matrix.length;
            Runnable barrierAction =
                    new Runnable() {
                        public void run() {
                            mergeRows();
                        }
                    };

            barrier = new CyclicBarrier(N, barrierAction);

            List<Thread> threads = new ArrayList<Thread>(N);
            for (int i = 0; i < N; i++) {
                Thread thread = new Thread(new Worker(i));
                threads.add(thread);
                thread.start();
            }
            // wait until done
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                }
            }
        }
    }
}


