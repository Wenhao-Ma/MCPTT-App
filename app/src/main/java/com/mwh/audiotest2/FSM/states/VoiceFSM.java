package com.mwh.audiotest2.FSM.states;

import android.util.Log;

import com.mwh.audiotest2.Audio.AudioPlayerHandler;
import com.mwh.audiotest2.Audio.AudioRecorderHandler;
import com.mwh.audiotest2.FSM.CallStatus;
import com.mwh.audiotest2.FSM.FSM;
import com.mwh.audiotest2.FSM.Message;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by KingFish on 2018/3/13.
 */

public final class VoiceFSM extends FSM {
    final ExecutorService audioThreadPool = new ThreadPoolExecutor(0, 1, 60,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    //播放排队提示音的Timer
    private Timer queueTimer;
    private TimerTask queueTimerTask;
    public boolean waitCeased = false;
//	//静音组当前通话的callid,为获取录音使用
//	private int muteGroupCallId = -1;

    private final Object ringLock = new Object();

    private int i = 0;
    public void startQueueRing() {
        synchronized (ringLock) {
            if (queueTimer != null) {
                return;
            }
            queueTimer = new Timer("QueueRingTimer");
            queueTimerTask = new TimerTask() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    addUIMessage(new byte[]{Message.UI_MESSAGE, Message.PRIVATE_CALL_RINGING});
                }
            };
            queueTimer.schedule(queueTimerTask, 0, 2000);
        }
    }

    public boolean stopQueueRing() {
        boolean returnValue = false;
        i = 0;
        synchronized (ringLock) {
            try {
                if (queueTimerTask != null) {
                    queueTimerTask.cancel();
                    queueTimerTask = null;
                }
                if(queueTimer != null) {
                    queueTimer.cancel();
                    queueTimer.purge();
                    queueTimer = null;
                    returnValue = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return returnValue;
    }

    private int d1 = 0, d2 = 0, d3 = 0, d4 = 0, d5 = 0, d6 = 0, d7 = 0;
    private Timer TFP1, TFP2, TFP3, TFP4, TFP5, TFP6, TFP7;
    private TimerTask Task1, Task2, Task3, Task4, Task5, Task6, Task7;
    /**
     * 开启计时器
     * @param i 计时器的种类
     */
    public void startTimer(int i) {
        switch (i) {
            case 1:
                if (Task1 != null) return;
                TFP1 = new Timer();
                Task1 = new TimerTask() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        d1++;
                        if (d1 == 1) {
                            onReceive(new byte[]{Message.TIMER_MESSAGE, Message.TFP1_EXPIRE});
                        }
                        if (d1 == 10) {
                            onReceive(new byte[]{Message.TIMER_MESSAGE, Message.TFP1_EXPIRE_N});
                        }
                    }
                };
                TFP1.scheduleAtFixedRate(Task1, 1000, 1000);
                break;
            case 2:
                if (Task2 != null) return;
                TFP2 = new Timer();
                Task2 = new TimerTask() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        d2++;
                        if (d2 == 1) {
                            onReceive(new byte[]{Message.TIMER_MESSAGE, Message.TFP2_EXPIRE});
                        }
                    }
                };
                TFP2.schedule(Task2, 10000);
                break;
            case 3:
                if (Task3 != null) return;
                TFP3 = new Timer();
                Task3 = new TimerTask() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        d3++;
                        if (d3 == 1) {
                            onReceive(new byte[]{Message.TIMER_MESSAGE, Message.TFP3_EXPIRE});
                        }
                        if (d3 == 10) {
                            onReceive(new byte[]{Message.TIMER_MESSAGE, Message.TFP3_EXPIRE_N});
                        }
                    }
                };
                TFP3.scheduleAtFixedRate(Task3, 1000, 1000);
                break;
            case 4:
                if (Task4 != null) return;
                TFP4 = new Timer();
                Task4 = new TimerTask() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        d4++;
                        if (d4 == 1) {
                            onReceive(new byte[]{Message.TIMER_MESSAGE, Message.TFP4_EXPIRE});
                        }
                        if (d4 == 10) {
                            onReceive(new byte[]{Message.TIMER_MESSAGE, Message.TFP4_EXPIRE_N});
                        }
                    }
                };
                TFP4.scheduleAtFixedRate(Task4, 1000, 1000);
                break;
            case 5:
                if (Task5 != null) return;
                TFP5 = new Timer();
                Task5 = new TimerTask() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        d5++;
                        if (d5 == 1) {
                            onReceive(new byte[]{Message.TIMER_MESSAGE, Message.TFP5_EXPIRE});
                        }

                    }
                };
                TFP5.schedule(Task5, 100000);
                break;
            case 6:
                if (Task6 != null) return;
                TFP6 = new Timer();
                Task6 = new TimerTask() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub

                    }
                };
                TFP6.schedule(Task6, 1000);
                break;
            case 7:
                if (Task7 != null) return;
                TFP7 = new Timer();
                Task7 = new TimerTask() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        d7++;
                        if (d7 == 1) {
                            onReceive(new byte[]{Message.TIMER_MESSAGE, Message.TFP7_EXPIRE});
                        }
                    }
                };
                TFP7.schedule(Task7, 1000);
                break;
            default:
                break;
        }
    }

    /**
     * 关闭计时器
     * @param i 计时器的种类
     */
    public void stopTimer(int i) {
        switch (i) {
            case 1:
                try {
                    if (Task1 != null) {
                        Task1.cancel();
                        Task1 = null;
                    }
                    if(TFP1 != null) {
                        TFP1.cancel();
                        TFP1.purge();
                        TFP1 = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                d1 = 0;
                break;
            case 2:
                try {
                    if (Task2 != null) {
                        Task2.cancel();
                        Task2 = null;
                    }
                    if(TFP2 != null) {
                        TFP2.cancel();
                        TFP2.purge();
                        TFP2 = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                d2 = 0;
                break;
            case 3:
                try {
                    if (Task3 != null) {
                        Task3.cancel();
                        Task3 = null;
                    }
                    if(TFP3 != null) {
                        TFP3.cancel();
                        TFP3.purge();
                        TFP3 = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                d3 = 0;
                break;
            case 4:
                try {
                    if (Task4 != null) {
                        Task4.cancel();
                        Task4 = null;
                    }
                    if(TFP4 != null) {
                        TFP4.cancel();
                        TFP4.purge();
                        TFP4 = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                d4 = 0;
                break;
            case 5:
                try {
                    if (Task5 != null) {
                        Task5.cancel();
                        Task5 = null;
                    }
                    if(TFP5 != null) {
                        TFP5.cancel();
                        TFP5.purge();
                        TFP5 = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                d5 = 0;
                break;
            case 6:
                try {
                    if (Task6 != null) {
                        Task6.cancel();
                        Task6 = null;
                    }
                    if(TFP6 != null) {
                        TFP6.cancel();
                        TFP6.purge();
                        TFP6 = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                d6 = 0;
                break;
            case 7:
                try {
                    if (Task7 != null) {
                        Task7.cancel();
                        Task7 = null;
                    }
                    if(TFP7 != null) {
                        TFP7.cancel();
                        TFP7.purge();
                        TFP7 = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                d7 = 0;
                break;
            default:
                break;
        }
        try {
            if (queueTimerTask != null) {
                queueTimerTask.cancel();
                queueTimerTask = null;
            }
            if(queueTimer != null) {
                queueTimer.cancel();
                queueTimer.purge();
                queueTimer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopAllTimer() {
        for (int i = 1; i <= 7; i++) {
            stopTimer(i);
        }
    }

    public final CallStatus callstatus;

    public VoiceFSM(String name) {
        super(name, VoiceFSM.class.getPackage().getName());
        callstatus = new CallStatus();
        waitCeased = false;

        String path = VoiceFSM.class.getPackage().getName();
    }

    @Override
    public void onReceive(byte[] buf) {
        super.onReceive(buf);
    }


    private byte mode, deplux, reqMode, reqDeplux;

    public void setMode(byte mode) {
        this.mode = mode;
    }

    public byte Deplux() {
        return deplux;
    }

    public void setDeplux(byte deplux) {
        this.deplux = deplux;
    }

    public byte mode() {
        return mode;
    }

    public void setReqMode(byte reqMode) {
        this.reqMode = reqMode;
    }

    public byte reqMode() {
        return reqMode;
    }

    public void setReqDeplux(byte reqDeplux) {
        this.reqDeplux = reqDeplux;
    }

    public byte reqDeplux() {
        return reqDeplux;
    }


    private int callerPort, calleePort;

    public void setPort(int caller, int callee) {
        callerPort = caller;
        calleePort = callee;
    }

    public int callerPort() {
        return callerPort;
    }
    public int calleePort() {
        return calleePort;
    }

    private String IP;

    public void setIP(String IP) {
        this.IP = IP;
    }
    public String IP() {
        return IP;
    }

    private boolean isCaller;

    public void setCaller(boolean call) {
        isCaller = call;
    }

    public boolean isCaller() {
        return isCaller;
    }

    public AudioRecorderHandler audioRecorderHandler = new AudioRecorderHandler(this);
    public AudioPlayerHandler audioPlayerHandler = new AudioPlayerHandler();

    private final Queue<byte[]> UIMessageQueue = new LinkedList<>();

    public void addUIMessage(byte[] buf) {
        synchronized (UIMessageQueue) {
            UIMessageQueue.add(buf);
        }
    }

    public Queue<byte[]> UIMessageQueue() {
        return UIMessageQueue;
    }

//	private SparseIntArray backgroundCallId = new SparseIntArray();
//	public void addBackgroundCallId(int callId, int callee) {
//		backgroundCallId.put(callId, callee);
//	}
//	/**
//	 * 清空所有背景组callId
//	 */
//	public void clearBackgroundCallId() {
//		backgroundCallId.clear();
//	}

}
