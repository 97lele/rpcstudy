package com.gdut.rpcstudy.demo.framework.protocol.netty.asyn;

import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcRequest;
import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcResponse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * @author: lele
 * @date: 2019/11/21 上午10:54
 */
public class RpcFuture implements Future<Object> {

    private RpcResponse rpcResponse;

    private RpcRequest rpcRequest;

    private Sync sync;

    public RpcFuture(RpcRequest rpcRequest) {
        this.rpcRequest = rpcRequest;
        this.sync = new Sync();
    }


    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    //返回状态是否改变了
    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    public void done(RpcResponse response) {
        this.rpcResponse = response;
        sync.tryRelease(1);

    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        //自选等待结果
        sync.acquire(-1);
        return this.rpcResponse.getResult() != null ? rpcResponse :
                rpcResponse.getError() != null ? rpcResponse.getError() : null;

    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      //超时获取
        boolean success = sync.tryAcquireNanos(-1, unit.toNanos(timeout));
        return get(success);
    }

    private Object get(boolean success) {
        if (success) {
            return this.rpcResponse.getResult() != null ? rpcResponse : rpcResponse.getError() != null ? rpcResponse.getError() : null;
        } else {
            throw new RuntimeException("超时:requestID" + rpcRequest.getRequestId() +
                    " method:" + rpcRequest.getMethodName() + " interface:" + rpcRequest.getInterfaceName()
            );
        }
    }

    /**
     * 继承同步器，可以用来做锁的功能，根据state来实现，state初始为0
     */
    static class Sync extends AbstractQueuedSynchronizer {
        /**
         * 尝试获取锁,如果获取不了，加入同步队列，阻塞自己，只由同步队列的头自旋获取锁
         * 当状态为1，即有结果返回时可以获取锁进行后续操作,设置result
         *
         * @param arg
         * @return
         */
        @Override
        protected boolean tryAcquire(int arg) {
            return getState() == 1;
        }

        /**
         * 用于远端有返回时，设置状态变更
         * 从头唤醒同步队列的队头下一个等待的节点，如果下一个节点为空，则从队尾唤醒
         *
         * @param arg
         * @return
         */
        @Override
        protected boolean tryRelease(int arg) {
            //把状态设置为1，给tryAcquire获取锁进行操作
            return getState() == 0 ? compareAndSetState(0, 1) : true;
        }

        public boolean isDone() {
            return getState() == 1;
        }
    }

}
