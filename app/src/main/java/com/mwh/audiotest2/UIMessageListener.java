package com.mwh.audiotest2;

import android.os.Handler;

import com.mwh.audiotest2.FSM.Message;
import com.mwh.audiotest2.FSM.states.VoiceFSM;

/**
 * Created by KingFish on 2018/3/13.
 */

public class UIMessageListener extends Thread {
    private final VoiceFSM fsm;
    private final Handler handler;

    public UIMessageListener(VoiceFSM fsm, Handler handler) {
        this.fsm = fsm;
        this.handler = handler;
    }
    @Override
    public void run() {
        while (true) {
            if (fsm.UIMessageQueue().isEmpty()) {
                continue;
            }
            byte[] buf;
            synchronized (fsm.UIMessageQueue()) {
                buf = fsm.UIMessageQueue().poll();
            }
            if(buf[0] == Message.UI_MESSAGE) {
                if (buf[1] == Message.INITIATE_PRIVATE_CALL) {
                    handler.sendEmptyMessage(1);
                }
                if (buf[1] == Message.PRIVATE_CALL_SETUP_REQUEST) {
                    if (fsm.reqMode() == Message.AUTO) {
                        handler.sendEmptyMessage(5);
                    } else {
                        handler.sendEmptyMessage(6);
                    }
                }
                if (buf[1] == Message.PRIVATE_CALL_ACCEPT_ACK) {
                    handler.sendEmptyMessage(2);
                }
                if (buf[1] == Message.PRIVATE_CALL_RELEASE) {
                    handler.sendEmptyMessage(3);
                    Thread t = new Thread() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            handler.sendEmptyMessage(4);
                        }
                    };
                    t.start();
                }
                if (buf[1] == Message.PRIVATE_CALL_RINGING) {
                    handler.sendEmptyMessage(7);
                }
            }
        }
    }
}
