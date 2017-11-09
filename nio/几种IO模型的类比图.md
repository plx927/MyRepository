# 几种IO模型的类比图
## 1.Blocking IO 模型 
在传统的BIO模型中，如果我们将网络通信服务比喻成一个饭店，那么ServerSocket扮演着饭店大厅接待员的角色，一个客户端连接进来，就好比一个顾客光顾该饭店；此时这个大厅接待员就到里面再安排一个服务员专门为这个顾客服务，这个服务员会安排顾客就坐，之后顾客会进行点菜服务，都将由这个服务员一个人完成，此时这个服务员还得扮演厨师的角色，为顾客点的菜进行烹饪，这个过程就等同于业务处理，也就是所谓的IO读写过程。
![传统BIO的线程模型](http://note.youdao.com/yws/public/resource/8558177f0b51dc6892d711b3133de99e/139BAAF0218B4D379889C5FBCCC308F1)


## 2.单线程下的Reactor
![image](http://note.youdao.com/yws/public/resource/8558177f0b51dc6892d711b3133de99e/AD487BF3F0224DF9B5125F5EC27B7300)
在单线程的NIO模型下，该饭店就变成了只有一个服务员，这个服务员既在大门口迎接顾客，还同时负责点单、做菜等服务，但是此时该服务员并不会傻傻等待在顾客那边，而是会先去忙自己的事，知道顾客需要它时，它才会过来服务,这在NIO模型中体现为Selector
中，它将多个IO事件复用在了同一个Selector上。

Java的NIO体现在是非阻塞的读与写。


Basic mechanisms supported in java.nio
- Non-blocking reads and writes
- Dispatch tasks associated with sensed IO events(根据Channel所感兴趣的IO事件，将任务派发给对应的Channel)
- Event-Driven Design


But harder to overlap processing with IO


### 3.多线程的Reactor
![image](http://note.youdao.com/yws/public/resource/8558177f0b51dc6892d711b3133de99e/BBD9A524F64248958F493C4B940AB873)
Offload non-IO processing to other threads，speed up
Reactor thread。

加入多线程的目的是降低执行NIO读写线程的处理负载，将其分担到其他线程上，此时Reactor就会注入于IO处理。
因为之前单线程模型下，Reactor线程必须等到等待一个IO事件被完全处理完毕才能继续获取一个发生IO事件的Channel所对应的SelectionKey，因此为了加速Reacotr能够更快的响应IO事件，因此将原来的线程模型进行改造为多线程版本，将所有的业务处理放入到线程池中进行处理，而**Reactor线程**中只负责连接的获取、数据的读取、数据的写出。(注意：这里强调的是Reactor所在的线程)

如果客户端正在线程池中进行处理任务，此时它继续发生一段数据，应该怎么处理？如果处理，应该注意什么？

>PPT中的处理方式

![PPT中的处理方式](http://note.youdao.com/yws/public/resource/8558177f0b51dc6892d711b3133de99e/754A2B6B6BD94B34A344D5B690E0FC61)
使用synchronized虽然可以解决线程安全问题，但是如果某个客户端会话因为其自身业务执行逻辑时间过长，那么会导致当前整个Reactor处于阻塞状态，其产生的后果会**非常严重**。

>我的思考解决方案:

对Processor这个Runnable的实现进行特殊的处理，让它们自己维护一个队列，在执行业务线程的时候可以判断上一次业务处理是否已经完成，如果没有完成，则将当前的业务处理任务放入到Processor的队列中，Processor在处理完成之后，继续轮询一下它的任务队列。


> 为什么将获取连接、IO读事件直接丢到线程池中处理会导致问题

由于上述的异步操作，如果没有将对应的IO事件消费完，那么再下一次select操作的时候依然会导致前面的事件产生，从而会导致线程池中重复创建任务，执行无效的操作。





### 4.多个Reactor
![image](http://note.youdao.com/yws/public/resource/8558177f0b51dc6892d711b3133de99e/6EA2730C14E540DCA44E03F0EC5CE00D)




### 5. Q & A
- 如果将一个Channel注册到多个Selector会出现什么情况?
- 如果服务器端在发送数据的时候,客户端断开连接?
数据可以正常发送，并且在发送完成后下一次select的时候，依然会产生读事件，而在这次读取的时候就会发送IOException。
- Channel的read方法是在ReadableByteChannel接口中所定义。
- 服务器端一次性读取数据的大小只取决于ByteBuffer的大小，然后发送的数据大于ByteBuffer的所能存储的大小，那么会在一下次select方法中继续产生IO事件，继续读取。


### 6. cas VS lock
在正常情况下，使用cas的乐观并发控制的效率要高于lock的悲观并发控制。其原因在于当一个线程被OS所挂起后，重新进入RUNNABLE状态，需要耗费大约30W的时钟周期；
而如果使用cas操作，当执行并不复杂的情况，它反复操作耗费的时钟周期将要远远小于线程挂起到恢复状态所经历的时间。
但是在CPU工作忙碌的情况下，比如服务器正在处理大量的请求，此时又希望往队列中添加新的任务，如果使用无锁的并发队列(ConcurrentLinkedQueue)，将所造成的效能响应反而会
可能其产生的效果比BlokckingLinkedQueue要来得差一些；其原因在于CPU由于执行当前的任务本身工作已经繁忙，但是又希望其分出时间来执行大量反复无效的操作，反而对CPU性能开销来得更大；
还有最重要的一点是，由于CPU本身处于忙碌的情况，即使把任务添加进来也无法立马得到执行，因此BlockingLinkedQueue是更加适合用于做缓冲的队列。
所以JDK的ThreadPoolExecutor底层使用的是阻塞队列来作为任务的队列，而没有使用一个普通的Queue作为任务队列，同样的一个应用使用场景体现在Cobar的
NIOReactor中，其NIOReactor.R线程使用了LinkedBlockingQueue来作为注册的缓冲队列。







