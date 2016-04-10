package com.panlingxiao.nio.reactor_v3_mainsub;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by panlingxiao on 2016/4/11.
 *
 * NIOServer启动类
 */
public class NIOServer {


    private MainReactor mainReactor;
    private SubReactor[] subReactors;
    private int cpuNumber = Runtime.getRuntime().availableProcessors();
    //业务线程池
    private Executor businessThreadPool = Executors.newFixedThreadPool(cpuNumber);

    public NIOServer() throws IOException{
        subReactors = new SubReactor[cpuNumber];
        for(int i = 0;i < subReactors.length;i++){
            subReactors[i] = new SubReactor(i,businessThreadPool);
        }
        mainReactor = new MainReactor(new InetSocketAddress(8080),subReactors);
    }

    public void start(){
        mainReactor.accept();
    }

    public static void main(String[] args) throws IOException{
        new NIOServer().start();
    }
}
