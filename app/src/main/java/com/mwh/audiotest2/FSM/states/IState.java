package com.mwh.audiotest2.FSM.states;

import java.util.concurrent.Callable;

/**
 * Created by KingFish on 2018/3/13.
 */


public interface IState {
    /**
     * 设置该状态执行enter事件后的回调
     * @param callback Runnable类型的回调
     */
    public void setAfterEnterCallback (Runnable callback);

    /**
     * 设置该状态执行leave事件前的回调
     * @param callback Callable<Boolean>类型的回调，
     *             若返回false则阻断状态的切换，
     *             即leave时间不被执行
     */
    public void setBeforeLeaveCallback(Callable<Boolean> callback);

    /**
     * 获取该状态执行enter事件后的回调
     * 执行后回调被清除
     * @return Runnable类型回调
     */
    public Runnable getAfterEnterCallback();

    /**
     * 获取该状态执行leave事件前的回调
     * 执行后回调被清除
     * @return Runnable类型回调
     */
    public Callable<Boolean> getBeforeLeaveCallback();

    public void enterEvent();

    public void leaveEvent();

    public void receiveEvent(byte[] buf, int len);

    public void setData(Object[] args);

    public Object[] getData();

    public Object getData(int index);

}