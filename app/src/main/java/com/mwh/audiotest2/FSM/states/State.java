package com.mwh.audiotest2.FSM.states;

import com.mwh.audiotest2.FSM.CallStatus;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by KingFish on 2018/3/13.
 */

public abstract class State implements IState{
    protected final VoiceFSM fsm;
    private AtomicReference<Runnable> enterCallback = new AtomicReference<Runnable>();
    private AtomicReference<Callable<Boolean>> leaveCallback = new AtomicReference<>();

    protected CallStatus callStatus;

    protected State(VoiceFSM fsm) {
        this.fsm = fsm;
        callStatus = fsm.callstatus;
    }

    private Object[] data = null;
    @Override
    public void setData(Object[] data) {
        this.data = data;
    }
    @Override
    public Object[] getData() {
        return data;
    }
    @Override
    public Object getData(int index) {
        if (data.length > index) {
            return data[index];
        }
        else {
            return null;
        }
    }

    /**
     * 设置该状态执行enter事件后的回调
     * @param enterCallback Runnable类型的回调
     */
    @Override
    public void setAfterEnterCallback(Runnable enterCallback) {
        this.enterCallback.set(enterCallback);
    }

    /**
     * 设置该状态执行leave事件前的回调
     * @param leaveCallback Callable<Boolean>类型的回调，
     *             若返回false则阻断状态的切换，
     *             即leave时间不被执行
     */
    @Override
    public void setBeforeLeaveCallback(Callable<Boolean> leaveCallback) {
        this.leaveCallback.set(leaveCallback);
    }

    /**
     * 获取该状态执行enter事件后的回调
     * 执行后回调被清除
     * @return Runnable类型回调
     */
    @Override
    public Runnable getAfterEnterCallback() {
        return enterCallback.getAndSet(null);
    }

    /**
     * 获取该状态执行leave事件前的回调
     * 执行后钩子被清除
     * @return Runnable类型回调
     */
    @Override
    public Callable<Boolean> getBeforeLeaveCallback() {
        return leaveCallback.getAndSet(null);
    }
}

