# Netty线程模型分析
NioEventLoopGroup这个后背做了什么?

## 1.EventExecutorGroup
`EventLoopGroup`提供对`EventExecutor`的管理和维护，并且通过`next()`方法将EventExecutor暴露给外部来获取使用。`EventLoopGroup`还继承
了JDK的`ScheduledExecutorService`,可以完成对任务的调度执行。

```
    /**
     * Returns one of the {@link EventExecutor}s managed by this {@link EventExecutorGroup}.
     */
    EventExecutor next();

    /**
     * Returns the unmodifiable set of {@link EventExecutor}s managed by this {@link EventExecutorGroup}.
     */
    <E extends EventExecutor> Set<E> children();
    
```

## 2.EventExecutor
EventExecutor是一个特殊的EventLoopGroup，它提供了自我管理的功能；
同时它提供了放来判断判断一个线程是否在一个event loop中执行。


## 3.EventLoopGroup
EventLoopGroup也是一个特殊的EventExecutorGroup，可以通过可以对Channel进行注册。
```
/**
 * Special {@link EventExecutorGroup} which allows registering {@link Channel}s that get
 * processed for later selection during the event loop.
 */
public interface EventLoopGroup extends EventExecutorGroup {
    @Override
    EventLoop next();

    /**
     * Register a {@link Channel} with an {@link EventLoop} from this {@link EventLoopGroup}. The returned
     * {@link ChannelFuture} will get notified once the registration is completed.
     * <p>
     * It's only safe to submit a new task to the {@link EventLoop} from within a
     * {@link ChannelHandler} once the {@link ChannelPromise} succeeded. Otherwise
     * the task may or may not be rejected.
     * </p>
     */
    ChannelFuture register(Channel channel);

    /**
     * Register a {@link Channel} with an {@link EventLoop} from this {@link EventLoopGroup}. The provided
     * {@link ChannelPromise} will get notified once the registration is completed. The returned {@link ChannelFuture}
     * is the same {@link ChannelPromise} that was passed to the method.
     * <p>
     * It's only safe to submit a new task to the {@link EventLoop} from within a
     * {@link ChannelHandler} once the {@link ChannelPromise} succeeded. Otherwise
     * the task may or may not be rejected.
     * </p>
     */
    ChannelFuture register(Channel channel, ChannelPromise promise);
}

```



## 4.EventLoop
>EventLoop是用于处理已经在Selector中注册的Channel所发生的I/O事件。通常情况下，一个EventLoop会处理多个Channel的IO事件。
EventLoop是一个Worker线程，EventLoop底层应该维护着一个Selector。===>通过NioEventLoop可以看到答案。

```
     /**
      * Creates a new default {@link ChannelHandlerInvoker} implementation that uses this {@link EventLoop} to
      * invoke event handler methods.
      */
     ChannelHandlerInvoker asInvoker();

 ```
通过EventLoop来调用event handler(所有的事件执行都在EventLoop)方法。


 从上面的描述中我们可以看出,EventLoop是用于处理服务器端的I/O操作。并且通常情况下，EventLoop和Channel的关系是1对多的关系。
 ```
 
 EventLoop el = getEventLoopFromPool();
 
 el.register(Channel);
 
 ```
 
 
看了这么多接口，能不能看点实现类。 
 
### SingleThreadEventLoop

/**
 * Abstract base class for {@link EventLoop}s that execute all its submitted tasks in a single thread.
 *
 */
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
 
### NioEventLoop

```
   //The NIO {@link Selector}.
   Selector selector;
   
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
     
     
 
```
 
 
## ChannelHandlerInvoker


## ChannelFuture 
Future在JDK中表示一个异步计算的结果。Netty中对JDK中的Future做了扩展，ChannelFuture是一个I/O操作的结果，在Netty中，
所有的I/O操作都是异步的。(只是框架层面的异步，不是OS层面的异步)



## ChannelPromise
 
 
 
## Channel
 
 
 
## NioSocketChannel
 
 