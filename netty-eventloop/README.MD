# Netty线程模型分析
#### 对于深入学习Netty，线程模型是必须跨过的坎;先从new NioEventLoopGroup();这么简单的一个语句，分析其后背做了什么?

## 1.EventExecutorGroup
`EventExecutorGroup`提供对`EventExecutor`(们)的生命周期的管理和维护，并且通过`next()`方法将EventExecutor暴露给外部来获取使用。`EventLoopGroup`还继承
了JDK的`ScheduledExecutorService`,可以完成对任务的调度执行，因此它本身具备线程池的功能;但它不是线程池,它可以代理线程池来完成调度操作。
比较EventExecutorGroup在ScheduledExecutorService的基础上添加了什么功能:
```
        /**
         * Returns one of the {@link EventExecutor}s managed by this {@link EventExecutorGroup}.
         * 返回被EventExecutorGroup所管理的一个EventExecutor
         * 具体返回哪一个取决于实现类选择策略算法的实现，可以是轮训的策略，也可以是随机选择。
         * 用户甚至可以根据自己的需求来对策略进行定制。
         */
        EventExecutor next();

        /**
         * Returns the unmodifiable set of {@link EventExecutor}s managed by this {@link EventExecutorGroup}.
         * 将被EventExecutorGroup所管理EventExecutor以Set的形式返回，并且返回的Set是一个不可以修改的Set。
         */
        <E extends EventExecutor> Set<E> children();
    
    
        /**
         * Shortcut method for {@link #shutdownGracefully(long, long, TimeUnit)} with sensible default values.
         * @return the {@link #terminationFuture()}
         * 实现优雅关闭它所管理的EventExecutor，并且返回一个Future，可以通过在返回的Future中添加监听器来实现当操作完成后的异步通知。
         */
        Future<?> shutdownGracefully();
```
除了上面的描述外，Netty对于JDK中的ScheduledExcutorService中返回java.util.concurrent.Future中的方法都进行了重写，它将返回值改成了io.netty.util.concurrent.Future，该Future是
对JDK中的Future进行了扩展，在代码层面上实现了**异步回调机制**。

## 2.EventExecutor
在上面我们已经说到`EventExecutorGroup`是对`EventExecutor`进行管理的，那么`EventExecutor`到底是个什么呢?
进入EventExecutor源码会发现，EventExecutor继承了EventExecutorGroup,我去，好变态。。。

```
public interface EventExecutor extends EventExecutorGroup{
    //忽略中间的方法
    ....
    
    /**
     * Return the {@link EventExecutorGroup} which is the parent of this {@link EventExecutor},
     * 通过parent()方法可以返回管理当前对象的EvenetExecutor的EventExecutorGroup
     */
    EventExecutorGroup parent();
    
    /**
     * Return {@code true} if the given {@link Thread} is executed in the event loop,
     * {@code false} otherwise.
     * 判断给定的线程是否在event loop中执行。
     */
    boolean inEventLoop(Thread thread);
    
    
    EventExecutor unwrap();

}
```
可能很多人一开始就会被这样的诡异设计给绕晕了，再加上名字的类似，基本上很快就歇菜；此时别急，慢慢来。。。
先看看API文档对EventExecutor的描述：
`EventExecutor`是一个特殊的`EventLoopGroup`,它提供了简便的方法来判断一个线程是否在一个event loop中执行。
其实看到这里，还是没有看懂什么，但是上面提到了一个event loop的东西，可以顺着这个继续看看。

## 3.EventLoopGroup
`EventLoopGroup`也是一个特殊的`EventExecutorGroup`；它除了可以对多个`EventExecutor`进行管理,可以通过可以对客户端连接来Channel进行**异步注册**，我去，终于看到一点
NIO的影子出现了。。。
```
    /*
     * NioEventLoopGroup对EventExecutorGroup的next方法进行了重写
     * 返回的不再是EventExecutor，而是EventLoop,EventLoop是EventExecutor的实现。
     */
    @Override
    EventLoop next();
    
    //异步注册的处理，当注册成功后，ChannelFuture会得到通知。
    ChannelFuture register(Channel channel);

    ChannelFuture register(Channel channel, ChannelPromise promise);
```

## 4.EventLoop
从它的名字可以看出，这个接口是完成事件循环处理;EventLoop是用于处理已经在Selector中注册的Channel所发生的I/O事件。通常情况下，一个EventLoop会处理多个Channel的IO事件。
EventLoop理解成一个Worker线程，因此其底层理论上应该维护着一个Selector;通过NioEventLoop可以看到答案。

```
    //这尼玛变态的东西...我无语了...
    public interface EventLoop extends EventExecutor, EventLoopGroup
     
     //它把从EventExecutor中继承而来的方法又给重写了，返回的不再是一个EventExecutorGroup，而是EventLoopGroup。
     @Override
     EventLoopGroup parent();   
     
     /**
      * Creates a new default {@link ChannelHandlerInvoker} implementation that uses this {@link EventLoop} to
      * invoke event handler methods.
      * 通过EventLoop来调用event handler的(所有的事件执行都在EventLoop)方法。
      */
     ChannelHandlerInvoker asInvoker();
```

可以看到`EventLoop`继承了`EventExecutor`和`EventLoopGroup`,因此它具有如下的功能:
1. **异步注册Channel**的功能
2. 任务调度和执行的功能；初次以外，它还可以获取到它的parent。
3. 它还具备循环处理IO事件的功能。**(它自身的功能，在Netty中，对于真正处理IO事件的是EventLoop)**
 
看到这里，先用一张简单的结构图表示一下上面4个接口的层级关系和作用。
![理理思路](http://note.youdao.com/yws/public/resource/8558177f0b51dc6892d711b3133de99e/D7EFFF6734F647E3BD0025F4E6EA45D9)


### EventLoop vs EventExecutor
EventLoop除了包含EventExecutor之外，它还具备注册Channel与处理IO事件的功能。


看了这么多接口，能不能看点实现类?
## MultithreadEventExecutorGroup
它是一个EventExecutorGroup的抽象实现,即可以对EventExecutor进行管理，并且还可以对任务进行调度执行可以使用多线程来调用EventExecutor来处理任务;
由上面的分析我们应该推断出这样的结果：它底层应该维护一组EventExecutor，同时它应该底层维护一个线程池。
首先看其底层维护的线程池：
```
    /*
     * 这里没有直接使用JDK中的Executors或者ScheduledThreadPoolExecutor来完成线程池的创建
     * Netty没有直接使用JDK原生的线程池是因为它需要对线程池进行定制来满足它特定的事件循环模型。
     * 这句话可以从ExecutorServiceFactory的注释中看到。
     */
    protected ExecutorService newDefaultExecutorService(int nEventExecutors) {
        return new DefaultExecutorServiceFactory(getClass()).newExecutorService(nEventExecutors);
    }
```

进入到`DefaultExecutorServiceFactory`,看到它创建ExecutorService的创建,嘿，难道不用Doug Lea的ForkJoinPool的线程池?
你点进去一看才发生，原来Netty把JDK的ForkJoinPool重写了，加了一点自己的功能，然后把包名改了，恶心啊。。。

```
    @Override
    public ExecutorService newExecutorService(int parallelism) {
        ForkJoinWorkerThreadFactory threadFactory =
                new DefaultForkJoinWorkerThreadFactory(namePrefix + '-' + executorId.getAndIncrement());
        return new ForkJoinPool(parallelism, threadFactory, DefaultUncaughtExceptionHandler.INSTANCE, true);
    }
```

继续来看MultiThreadEventExecutorGroup是如何创建的，**黎明的曙光**:

```
    private MultithreadEventExecutorGroup(int nEventExecutors,
                                          Executor executor,
                                          boolean shutdownExecutor,
                                          Object... args) {
        if (nEventExecutors <= 0) {
            throw new IllegalArgumentException(
                    String.format("nEventExecutors: %d (expected: > 0)", nEventExecutors));
        }

        if (executor == null) {
            //创建ForkJoinPoolExecutor，它使用的是JDK1.7版本的ForkJoinPool
            executor = newDefaultExecutorService(nEventExecutors);
            shutdownExecutor = true;
        }
        
        //根据nEventExecutors的数量来创建对应的EventExecutor
        children = new EventExecutor[nEventExecutors];
        
        //根据nEventExecutors,选择对于的策略实现，对于EventLoop选择策略的结果为EventExecutorChooser
        
        //判断个数是否为2的次方,底层基于按位与运算来进行处理
        if (isPowerOfTwo(children.length)) {
            chooser = new PowerOfTwoEventExecutorChooser();
        } else {
            //使用通用的策略来实现对EventExecutor的选取，即通过一个整数++,然后对nEventExecutors进行取模得到索引值
            chooser = new GenericEventExecutorChooser();
        }
        
       //创建EventExecutor，MultithreadEventExecutorGroup并没有实现newChild方法，具体的实现会去完成。
       //NioEventLoopGroup底层所创建的EventExecutor就是一个NioEventLoop。
        for (int i = 0; i < nEventExecutors; i ++) {
            boolean success = false;
            try {
                children[i] = newChild(executor, args);
                success = true;
            } catch (Exception e) {
                // TODO: Think about if this is a good exception type
                throw new IllegalStateException("failed to create a child event loop", e);
            } finally {
                //如果存在某一个创建EventExecutor失败，那么就对所有的EventExecutor实现优雅关闭
                if (!success) {
                    for (int j = 0; j < i; j ++) {
                        children[j].shutdownGracefully();
                    }
                    //判断是否已经关闭，如果没有关闭，则会一直等待EventExecutor结束。
                    for (int j = 0; j < i; j ++) {
                        EventExecutor e = children[j];
                        try {
                            while (!e.isTerminated()) {
                                e.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
                            }
                        } catch (InterruptedException interrupted) {
                            // Let the caller handle the interruption.
                            //如果调用者中断线程，那么Netty让调用者自己去处理中断
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
        }

        final boolean shutdownExecutor0 = shutdownExecutor;
        final Executor executor0 = executor;
        
        //创建监听器,该FutureListener用于处理EventExecutor关闭后的操作
        final FutureListener<Object> terminationListener = new FutureListener<Object>() {
            @Override
            public void operationComplete(Future<Object> future) throws Exception {
                if (terminatedChildren.incrementAndGet() == children.length) {
                    terminationFuture.setSuccess(null);
                    if (shutdownExecutor0) {
                        // This cast is correct because shutdownExecutor0 is only try if
                        // executor0 is of type ExecutorService.
                        ((ExecutorService) executor0).shutdown();
                    }
                }
            }
        };
    
    
        //为每一个EventExecutor添加关闭后的监听器,用于回调处理
        for (EventExecutor e: children) {
            e.terminationFuture().addListener(terminationListener);
        }

        //将初始化的EventExecutor添加到一个只读的Set中。
        Set<EventExecutor> childrenSet = new LinkedHashSet<EventExecutor>(children.length);
        Collections.addAll(childrenSet, children);
        readonlyChildren = Collections.unmodifiableSet(childrenSet);
    }

```

EventExecutor选择策略的实现:
```
   private interface EventExecutorChooser {
        EventExecutor next();
    }
    
    //基于2的次方的选择策略
    private final class PowerOfTwoEventExecutorChooser implements EventExecutorChooser {
        @Override
        public EventExecutor next() {
            return children[childIndex.getAndIncrement() & children.length - 1];
        }
    }
    
    //基于通用的选择策略
    private final class GenericEventExecutorChooser implements EventExecutorChooser {
        @Override
        public EventExecutor next() {
            return children[Math.abs(childIndex.getAndIncrement() % children.length)];
        }
    }

```

## NioEventLoopGroup
`NioEventLoopGroup`是一个`MultithreadEventLoopGroup`，它实现了如下功能:
1. 对多个NioEventLoop的管理，通过源码可以看到它在创建的时候会随之创建多个NioEventLoop。
2. Channel异步注册(异步注册是通过返回底层的EventLoop来实现)
3. 任务调度执行的功能。

>Netty User Guide 5中对NioEventLoopGroup的描述:
NioEventLoopGroup is a multithreaded event loop that handles I/O operation. Netty provides various EventLoopGroup implementations for different kind of transports. 
We are implementing a server-side application in this example, and therefore two NioEventLoopGroup will be used. 
The first one, often called 'boss', accepts an incoming connection. The second one, often called 'worker', handles the traffic of the accepted connection once the boss accepts the connection and registers the accepted connection to the worker. 
How many Threads are used and how they are mapped to the created Channels depends on the EventLoopGroup implementation and may be even configurable via a constructor.

```
   /**
    * NioEventLoopGroup完整的方法签名列表如下，需要指定NioEventLoop的个数,默认使用CPU个数*2,默认使用的线程池有Netty提供，
    * 使用的SelectorProvider是基于操作系统的。
    */
    public NioEventLoopGroup(int nEventLoops, Executor executor, final SelectorProvider selectorProvider) {
        //调用MultithreadEventLoopGroup完成Executor的创建，同时会完成nEventLoops个EventLoop
        super(nEventLoops, executor, selectorProvider);
    }
    
    
   //异步注册处理的实现
    @Override
    public EventLoop next() {
        return (EventLoop) super.next();
    }
    
    @Override
    public ChannelFuture register(Channel channel) {
        return next().register(channel);
    }
    
    @Override
    public ChannelFuture register(Channel channel, ChannelPromise promise) {
        return next().register(channel, promise);
    }
    
```


>MultithreadEventLoopGroup源码:

```

        //MultithreadEventLoopGroup构造方法实现
        protected MultithreadEventLoopGroup(int nEventLoops,
                                            Executor executor,
                                            Object... args) {
            /*
             * 通过nEventLoop的个数进行判断,如果为0，则从系统参数io.netty.eventLoopThreads读取，
             * 如果没有，则NioEventLoop的个数为Runtime.getRuntime().availableProcessors() * 2                                
             * 然后就追溯到MultiThreadEventExecutorGroup
             */
            super(nEventLoops == 0 ? DEFAULT_EVENT_LOOP_THREADS : nEventLoops, executor, args);
        }

```



### 疑惑:如何让NioEventLoop和NioEventLoopGroup建立起联系?
```
    
    //NioEventLoopGroup中对于NioEventLoop的扩容方法，直接通过构造方法将NioEventLoopGroup传入到NioEventLoop中，作为它的parent。
    @Override
    protected EventLoop newChild(Executor executor, Object... args) throws Exception {
        return new NioEventLoop(this, executor, (SelectorProvider) args[0]);
    }
    
```
 
### SingleThreadEventExecutor 
EventExecutor的抽象实现，通过将所有提交的任务在一个线程中执行处理。
```
    //底层维护着当前的线程
    private volatile Thread thread;
    
    
    //判断当前线程是在EventLoop中执行
    @Override
    public boolean inEventLoop(Thread thread) {
        return thread == this.thread;
    }
    
    /**
     * @param parent            设置EventExecutorGroup为当前EventExecutor的parent
     * @param executor          设置线程池用于执行任务队列
     * @param addTaskWakesUp    当为true时，任务被添加队列中后会唤醒执行的线程
     *                         
     */
    protected SingleThreadEventExecutor(EventExecutorGroup parent, Executor executor, boolean addTaskWakesUp) {
        super(parent);

        if (executor == null) {
            throw new NullPointerException("executor");
        }

        this.addTaskWakesUp = addTaskWakesUp;
        this.executor = executor;
        taskQueue = newTaskQueue();
    }

```





### SingleThreadEventLoop
EventLoop的抽象实现，通过将所有提交的任务在一个线程中执行处理。 
 
  
  
  
### NioEventLoop
基于Nio的单线程的EventLoop的实现，将客户端的Channel注册到其底层维护的Selector中，然后通过事件循环处理在多路复用器中发生的IO事件。
```
   //The NIO {@link Selector}.
   Selector selector;
   
  /**
    * Boolean that controls determines if a blocked Selector.select should
    * break out of its selection process. In our case we use a timeout for
    * the select method and the select method will block for that time unless
    * waken up.
    *
    *  Netty中通过布尔值控制Selector中的select方法是否应该阻塞当前线程执行。
    */
   private final AtomicBoolean wakenUp = new AtomicBoolean();
   
  // 在Netty中，自己实现了一个Set,用于存放底层的SelectionKey
  // 通过反射替换掉原来JDK默认所使用的HashSet。
   private SelectedSelectionKeySet selectedKeys; 

  private Selector openSelector() {
        final Selector selector;
        try {
            selector = provider.openSelector();
        } catch (IOException e) {
            throw new ChannelException("failed to open a new selector", e);
        }

        if (DISABLE_KEYSET_OPTIMIZATION) {
            return selector;
        }

        try {
            SelectedSelectionKeySet selectedKeySet = new SelectedSelectionKeySet();

            Class<?> selectorImplClass =
                    Class.forName("sun.nio.ch.SelectorImpl", false, PlatformDependent.getSystemClassLoader());

            // Ensure the current selector implementation is what we can instrument.
            if (!selectorImplClass.isAssignableFrom(selector.getClass())) {
                return selector;
            }

            Field selectedKeysField = selectorImplClass.getDeclaredField("selectedKeys");
            Field publicSelectedKeysField = selectorImplClass.getDeclaredField("publicSelectedKeys");

            selectedKeysField.setAccessible(true);
            publicSelectedKeysField.setAccessible(true);

            selectedKeysField.set(selector, selectedKeySet);
            publicSelectedKeysField.set(selector, selectedKeySet);

            selectedKeys = selectedKeySet;
            
            //通过日志信息可以看到，注入一个优化的Set到Selector中。
            logger.trace("Instrumented an optimized java.util.Set into: {}", selector);
        } catch (Throwable t) {
            selectedKeys = null;
            logger.trace("Failed to instrument an optimized java.util.Set into: {}", selector, t);
        }

        return selector;
    }
    
    
    //实现SingleThreadEventExecutor中的所定义的run方法
     @Override
        protected void run() {
            boolean oldWakenUp = wakenUp.getAndSet(false);
            try {
                //判断任务队列中是否有任务,底层维护着一个LinkedBlockingQueue。
                if (hasTasks()) {
                    selectNow();
                } else {
                    select(oldWakenUp);
    
                    // 'wakenUp.compareAndSet(false, true)' is always evaluated
                    // before calling 'selector.wakeup()' to reduce the wake-up
                    // overhead. (Selector.wakeup() is an expensive operation.)
                    //
                    // However, there is a race condition in this approach.
                    // The race condition is triggered when 'wakenUp' is set to
                    // true too early.
                    //
                    // 'wakenUp' is set to true too early if:
                    // 1) Selector is waken up between 'wakenUp.set(false)' and
                    //    'selector.select(...)'. (BAD)
                    // 2) Selector is waken up between 'selector.select(...)' and
                    //    'if (wakenUp.get()) { ... }'. (OK)
                    //
                    // In the first case, 'wakenUp' is set to true and the
                    // following 'selector.select(...)' will wake up immediately.
                    // Until 'wakenUp' is set to false again in the next round,
                    // 'wakenUp.compareAndSet(false, true)' will fail, and therefore
                    // any attempt to wake up the Selector will fail, too, causing
                    // the following 'selector.select(...)' call to block
                    // unnecessarily.
                    //
                    // To fix this problem, we wake up the selector again if wakenUp
                    // is true immediately after selector.select(...).
                    // It is inefficient in that it wakes up the selector for both
                    // the first case (BAD - wake-up required) and the second case
                    // (OK - no wake-up required).
    
                    if (wakenUp.get()) {
                        selector.wakeup();
                    }
                }
    
                cancelledKeys = 0;
                needsToSelectAgain = false;
                final int ioRatio = this.ioRatio;
                if (ioRatio == 100) {
                    processSelectedKeys();
                    runAllTasks();
                } else {
                    final long ioStartTime = System.nanoTime();
    
                    processSelectedKeys();
    
                    final long ioTime = System.nanoTime() - ioStartTime;
                    runAllTasks(ioTime * (100 - ioRatio) / ioRatio);
                }
    
                if (isShuttingDown()) {
                    closeAll();
                    if (confirmShutdown()) {
                        cleanupAndTerminate(true);
                        return;
                    }
                }
            } catch (Throwable t) {
                logger.warn("Unexpected exception in the selector loop.", t);
    
                // TODO: After using a ForkJoinPool that is potentially shared with other software
                // than Netty. The Thread.sleep might be problematic. Even though this is unlikely to ever
                // happen anyways.
    
                // Prevent possible consecutive immediate failures that lead to
                // excessive CPU consumption.
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // Ignore.
                }
            }
    
            scheduleExecution();
        }
```

在EventLoop中定义了一个asInvoker()方法,通过返回一个ChannelHandlerInvoker，然后在EventLoop中执行通过ChannelHandlerInvoker执行ChannleHandler中的IO事件方法。
NioEventLoop中的ChannelHandlerInvokder是从SingleThreadEventLoop中继承而来，其所使用的是DefaultChannelHandlerInvoker。

 
 
## ChannelHandlerInvoker







## ChannelFuture 
Netty中的Future,JDK原生的Future只是代表一个异步计算的结果，无法真正达到异步编程的功能，如果希望获取到结果，依旧需要等待计算的结果。
即没有实现回调的机制，而Netty中通过对原生的Future做了扩展，可以在Future中添加Listener,从而实现回调的功能，达到真正异步的效果。


Netty中对JDK中的Future做了扩展，ChannelFuture是一个I/O操作的结果，在Netty中，
所有的I/O操作都是异步的。(只是框架层面的异步，不是OS层面的异步)



## ChannelPromise
 
 
 
## Channel
 
 
 
## NioSocketChannel
分析NioSocketChannel的注册过程:
```
   @Override
    public ChannelFuture register(Channel channel) {
        return register(channel, new DefaultChannelPromise(channel, this));
    }

    @Override
    public ChannelFuture register(final Channel channel, final ChannelPromise promise) {
        if (channel == null) {
            throw new NullPointerException("channel");
        }
        if (promise == null) {
            throw new NullPointerException("promise");
        }

        channel.unsafe().register(this, promise);
        return promise;
    }

```

 
 