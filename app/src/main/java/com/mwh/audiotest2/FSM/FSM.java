package com.mwh.audiotest2.FSM;

import android.support.v7.app.AppCompatActivity;
import android.util.Pair;

import com.mwh.audiotest2.FSM.states.IState;
import com.mwh.audiotest2.FSM.states.VoiceFSM;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

/**
 * Created by KingFish on 2018/3/13.
 */


public class FSM extends AppCompatActivity {
    protected String statesPath;
    protected String currentState, previousState;
    protected Map<String, IState> states;

    /**
     * 返回状态机创建时的名称
     * @return 名称t
     */
    public String getName() {
        return name;
    }

    protected String name;
    protected boolean debug;

    //用户执行状态改变及进入、接收、离开事件的线程
    protected FsmThread fsmThread;

    //用于同步接收线程及状态机线程
    //防止状态机还未处理完成时继续接收
    private final Object stateLock = new Object();

    /**
     * 创建指定名称的状态机
     * @param name 状态机名称
     * @param statesPath
     */
    public FSM(String name, String statesPath) {
        this.name = name;
        this.statesPath = statesPath;
        this.states = new HashMap<>();
        this.previousState = null;
        this.currentState = null;
        fsmThread = new FsmThread(this);
        fsmThread.start();
    }

    /**
     * 销毁状态机
     * 新建状态机前需要调用此方法来关闭之前状态机的线程
     */
    public void destroy() {
        fsmThread.interrupt();
    }

    /**
     * 获取状态机的上一个状态
     * @return 代表上一个状态的字符串
     */
    public String getPreviousState() {
        return previousState;
    }

    /**
     * 获取当前状态机的状态
     * @return 代表当前状态的字符串
     */
    public String getState() {
        return currentState;
    }

    /**
     * 添加状态，默认不添加事件
     * @param state 当前状态的字符串
     */
    @SuppressWarnings("unchecked")
    public void addState(String state) {
        //是否为第一个状态
        boolean isInitial = (states.size() == 0);
        if (!states.containsKey(state)) {
            Class<IState> stateClass = null;
            try {
                //创建该状态，存入states中
                stateClass = (Class<IState>) Class.forName(statesPath + "." + state);
                Constructor<IState> constructor = stateClass.getConstructor(this.getClass());
                states.put(state, constructor.newInstance(this));
            } catch (Exception e) {
                // TODO: handle exception
                return;
            }
        }
        if (isInitial) {
            //setState(state);
        }
    }

    public void addStates(String[] states) {
        for (String state : states) {
            addState(state);
        }
    }

    /**
     * 改变当前状态，仅供状态机内部调用
     * @param name 要设置的当前状态
     */
    public void changeState(String name) {
        previousState = currentState;
        currentState = name;
    }

    /**
     * 设置当前状态为要设置的状态
     * @param state 要设置的当前状态
     */
    public void setState(String state){
        setState(state, null);
    }

    public void setState(final String state, final Object[] args) {
        if (!states.containsKey(state)) {
            return;
        }
        synchronized (stateLock) {
            boolean runExtraCode = !state.equals(currentState);
            //如果设置的状态与当前状态不相同，则改变当前状态，即runExtraCode
            if (runExtraCode) {
                Callable<Boolean> leaveTask = null;
                //如果当前状态不为空，则离开当前状态，并创建leave事件前的回调
                if (currentState != null) {
                    final IState leaveState = states.get(currentState);
                    leaveTask = new Callable<Boolean>() {

                        @Override
                        public Boolean call() throws Exception {
                            if (args != null) {
                                leaveState.setData(args);
                            }
                            Callable<Boolean> callable = leaveState.getBeforeLeaveCallback();
                            if (callable != null) {
                                try {
                                    if (callable.call() == false) {
                                        return false;
                                    }

                                } catch (Exception e) {
                                    return false;
                                }
                            }
                            leaveState.leaveEvent();
                            return true;
                        }
                    };
                }
                //进入新的状态“state”，并创建enter事件后的回调
                final IState enterState = states.get(state);

                changeState(state);//此处被修改！
                Runnable enterTask = new Runnable() {

                    @Override
                    public void run() {
                        //被挪动！
                        if (args != null) {
                            enterState.setData(args);
                        }
                        enterState.enterEvent();
                        Runnable callback = enterState.getAfterEnterCallback();
                        if (callback != null) {
                            callback.run();
                        }
                    }
                };
                //添加进入/离开事件，并执行
                fsmThread.addEnterReceiveEvent(new Pair<Callable<Boolean>, Runnable>(leaveTask, enterTask));
            }
        }
    }

    /**
     * 设置该状态执行enter事件后的回调
     * @param state 要设置回调的状态
     * @param callback Runnable类型的回调
     */
    public void setAfterEnterCallback(final String state, final Runnable callback) {
        // 使用runAtomic方法防止状态机的执行导致当前状态的变化
        fsmThread.runAtomic(new Runnable() {

            @Override
            public void run() {
                // 如果当前状态就是要设置的状态，则立即运行callback
                IState expectedState = states.get(state);
                IState cur = states.get(currentState);
                if (cur != null && cur.equals(expectedState)) {
                    fsmThread.setEnterCallback(callback);
                } else {
                    expectedState.setAfterEnterCallback(callback);
                }
            }
        });
    }

    /**
     * 设置该状态执行leave事件前的回调
     * @param state 要设置回调的状态
     * @param callback Callable<Boolean>类型的回调，
     *             若返回false则阻断状态的切换，
     *             即leave事件不被执行
     */
    public void setBeforeLeaveCallback(String state, Callable<Boolean> callback) {
        states.get(state).setBeforeLeaveCallback(callback);
    }

    /**
     * 给当前状态发送信息，调用当前状态的receiveEvent
     * @param buf 接收到的信息
     */
    public synchronized void onReceive(final byte[] buf) {
        fsmThread.addReceiveEvent(buf);
    }

    public Object[] getData() {
        return states.get(currentState).getData();
    }

    public Object getData(int index) {
        return states.get(currentState).getData(index);
    }


    private Timer timer;
    private TimerTask timerTask;

    public void setTimeout(int time, final String state) {
        setTimeout(time, state, null);
    }

    public void setTimeout(int time, final String state, final Object[] data) {
        final String curStateInTimeout = currentState;
        clearTimeout();
        timer = new Timer("change to " + state + " timer");
        final Exception exception = new Exception();
        timerTask = new TimerTask() {

            @Override
            public void run() {
                setState(state, data);
                //是否由于超时挂断
                if ("StateDisconnect".equals(state)) {

                }
            }
        };
        timer.schedule(timerTask, time);
    }

    public void clearTimeout() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    /**
     * 用于在掉线时清空状态机线程的执行队列
     */
    public void clearFSMQueue() {
        try {
            fsmThread.clearFSMQueue();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    /**
     * Created by KingFish on 2018/3/13.
     */

    public static class FSMThread {
    }
}

