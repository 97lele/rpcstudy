package com.gdut.rpcstudy.demo.framework.connection;

import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcRequest;
import com.gdut.rpcstudy.demo.framework.serialize.tranobject.RpcResponse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: lele
 * @date: 2019/11/20 上午9:29
 */
public class RpcFuture implements Future<RpcResponse> {

    private StateSync stateSync;


    private RpcResponse response;

    private ReentrantLock lock = new ReentrantLock();

    public RpcFuture() {
        this.stateSync = new StateSync();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDone() {
        return stateSync.isDone();
    }

    @Override
    public RpcResponse get() throws InterruptedException, ExecutionException {
        stateSync.acquire(0);
        if (this.response != null) {
            return response;
        }
        return null;
    }

    @Override
    public RpcResponse get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean get = stateSync.tryAcquireNanos(0, unit.toNanos(timeout));
        if (get) {
            return this.response == null ? null : this.response;
        }
        return null;
    }

    public void done(RpcResponse response) {
        this.response = response;
        //改变状态为1
        stateSync.release(1);
    }

    //自定义同步器，用于管理锁资源获取和释放，确保结果返回
    static class StateSync extends AbstractQueuedSynchronizer {


        //完成状态，是否持有锁
        private final int done = 1;

        //等待状态
        private final int waitting = 0;

        //尝试获取独占锁，如果获取不了，加入等待队列，当状态为done才能获取
        @Override
        protected boolean tryAcquire(int arg) {
            return getState() == done;
        }

        //尝试释放独占锁，当状态处于等待时，尝试设为完成状态
        @Override
        protected boolean tryRelease(int arg) {
            assert arg == done;
            if (getState() == waitting) {
                return compareAndSetState(waitting, arg);
            } else {
                return true;
            }
        }

        public boolean isDone() {
            return getState() == done;
        }
    }
}
