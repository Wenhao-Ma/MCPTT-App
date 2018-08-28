package com.mwh.audiotest2.FSM;

import android.util.Pair;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by KingFish on 2018/3/13.
 */

public class FsmThread extends Thread {
    //与状态机共享的信号量
    private Semaphore semaphore;
    //存放进入/离开事件的任务队列
    private final Queue<Pair<Callable<Boolean>, Runnable>> enterleaveQueue = new LinkedList<>();
    //存放接收事件的任务队列
    private final Queue<byte[]> receiveQueue = new LinkedList<>();

    //状态机实例
    private FSM fsm;
    public FsmThread(FSM fsm) {
        super();
        semaphore = new Semaphore(0, true);
        this.fsm = fsm;
    }

    private final Object hookLock = new Object();
    private Runnable enterCallback = null; //某状态enter时的回调线程
    /**
     * 设置enter时的回调
     * @param callback Runnable类型的回调
     */
    public void setEnterCallback(Runnable callback) {
        synchronized (hookLock) {
            this.enterCallback = callback;
            semaphore.release();
        }
    }

    //FSM线程锁
    private final Lock fsmThreadLock = new ReentrantLock();
    /**
     * 安全地运行一个Runnable，防止由于状态机的执行导致状态的改变
     * @param runnable
     */
    public void runAtomic(Runnable runnable) {
        fsmThreadLock.lock();
        try {
            runnable.run();
        }
        finally {
            fsmThreadLock.unlock();
        }
    }

    @Override
    public void run() {
        try {
            String name = "FsmThread-" + fsm.getName() + "-"
                    + Thread.currentThread().getId();
            this.setName(name);//设置线程名
            //线程没有被中断
            while (!interrupted()) {
                semaphore.acquire();
                fsmThreadLock.lock();
                try {
                    //如果有enter的线程，则运行
                    synchronized (hookLock) {
                        if (enterCallback != null) {
                            enterCallback.run();
                            enterCallback = null;
                            continue;
                        }
                    }
                    boolean isEnterLeaveQueueEmpty = enterleaveQueue.isEmpty();
                    boolean isReceiveQueueEmpty = receiveQueue.isEmpty();

                    if (isEnterLeaveQueueEmpty && isReceiveQueueEmpty)
                        continue;
                    //优先考虑进入/离开事件
                    if (!isEnterLeaveQueueEmpty) {
                        Pair<Callable<Boolean>, Runnable> pair;
                        synchronized (enterleaveQueue) {
                            pair = enterleaveQueue.poll();
                        }
                        //执行离开事件
                        if (pair.first != null) {
                            if (!pair.first.call()) {
                                continue;
                            }
                        }
                        //执行进入事件
                        pair.second.run();
                    }
                    //当无进入/离开事件时，执行接收事件
                    else {
                        byte[] buf;
                        synchronized (receiveQueue) {
                            buf = receiveQueue.poll();
                        }
                        fsm.states.get(fsm.getState()).receiveEvent(buf, buf.length);
                    }
                } catch (Exception e) {
                    if (e instanceof InterruptedException)
                        throw (InterruptedException) e;
                }
                finally {
                    fsmThreadLock.unlock();
                }
            }
        }
        catch (InterruptedException ignored) {
        }
    }

    /**
     * 添加进入/离开事件
     * @param pair 进入/离开事件
     */
    void addEnterReceiveEvent(Pair<Callable<Boolean>, Runnable> pair) {
        synchronized (enterleaveQueue) {
            enterleaveQueue.add(pair);
        }
        semaphore.release();
    }

    /**
     * 添加接收事件
     * @param buf 接收事件
     */
    void addReceiveEvent(byte[] buf) {
        synchronized (receiveQueue) {
            receiveQueue.add(buf);
        }
        semaphore.release();
    }

    /**
     *掉线时，清空状态机线程的执行队列
     */
    void clearFSMQueue() {
        synchronized (enterCallback) {
            enterleaveQueue.clear();
        }
        synchronized (receiveQueue) {
            receiveQueue.clear();
        }
    }
}
