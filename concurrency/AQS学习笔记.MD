针对AQS的setHeadAndPropagate(Node node, int propagate)
如何模拟出h的status是0，head的status是-1执行

这是时序的模拟需要启动3个线程，2个acquire,一个release。
2个acquire就会在队列中产生3个节点。

情况1:
当release线程执行完tryReleaseShared，第一个节点还没有shouldParkAfterFailedAcquire，
第三个节点刚刚执行完成shouldParkAfterFailedAcquire，将它的前驱节点（第二个节点）设置为-1。
此时第二个节点就进入到setHeadAndProgagate。这样就会造成后续的h的status=0,head的status=-1。

情况2：
release线程在doRlease中执行完成Unpark,将后继节点唤醒,后继节点进入到setAndPropogate,此时也会出现上面的情况。


h.status<0条件不成立有一个前置条件,即后继节点没有被park，因为park的前提是将前驱节点的状态设置为singal（-1)。
因此在JDK1.7中，即使h.waitStauts<0不成立，不执行doRelease操作，因为setHeadAndProgagte(Node),
会导致后面的节点都进入到setHeadAndProgagte方法中。





h.status等于0有两种情况造成:
1.当h为head的时候在默认的情况下，就获取到拥有权，因为AQS节点的默认的waitStatus=0,所以此时为0。
2.当执行doRelase的时候，将原来的singal设置成为了0，导致节点为waitStatus=0。


shouldParkAfterFailedAcquire方法是为了防止线程一直处于阻塞