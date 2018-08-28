package com.mwh.audiotest2.FSM.states;

import android.util.Log;

import com.mwh.audiotest2.Audio.AudioRecorderHandler;
import com.mwh.audiotest2.FSM.Message;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by KingFish on 2018/3/13.
 */

public class StateP2 extends State {
    public StateP2(VoiceFSM fsm)
    {
        super(fsm);
    }
    @Override
    public void enterEvent()
    {
        Log.e("state",fsm.getName()+" Enter StateP2");
    }

    @Override
    public void leaveEvent()
    {
        Log.e("state",fsm.getName()+" Leave StateP2");
    }

    @Override
    public void receiveEvent(byte[] buf, int len)
    {
        if (buf[1] == Message.TFP1_EXPIRE) {
            //TODO
            //update user location
            byte[] data = new byte[]{Message.COMMAND_MESSAGE, Message.PRIVATE_CALL_SETUP_REQUEST};
            try {
                InetAddress address = InetAddress.getByName(fsm.IP());
                int port = fsm.calleePort();
                DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                DatagramSocket socket = new DatagramSocket();
                socket.send(packet);
            } catch (Exception e) {
                // TODO: handle exception
            }

            //fsm.startTimer(1);
        }

        if (buf[1] == Message.PRIVATE_CALL_RINGING) {
            fsm.addUIMessage(new byte[]{Message.UI_MESSAGE, Message.PRIVATE_CALL_RINGING});
            Log.e("user","PRIVATE_CALL_RINGING");
            //fsm.startQueueRing();
            //System.out.println("铃声~~~~");
        }

        if (buf[1] == Message.TFP1_EXPIRE_N && fsm.mode() == Message.AUTO) {
            fsm.stopTimer(1);
            fsm.startTimer(7);
            fsm.setState("StateP1");
            byte[] ui = new byte[]{Message.UI_MESSAGE, Message.PRIVATE_CALL_RELEASE};
            fsm.addUIMessage(ui);
        }

        if (buf[1] == Message.TFP1_EXPIRE_N && fsm.mode() == Message.MANUAL) {
            fsm.stopTimer(1);
            fsm.startTimer(2);
        }

        if (buf[1] == Message.TFP2_EXPIRE) {
            fsm.stopTimer(2);
            fsm.startTimer(7);
            //TODO
            //release call control state machine
            byte[] ui = new byte[]{Message.UI_MESSAGE, Message.PRIVATE_CALL_RELEASE};
            fsm.addUIMessage(ui);
            fsm.setState("StateP1");
        }

        //with call identifier IE and Reason IE == REJECT
        if (buf[1] == Message.PRIVATE_CALL_REJECT) {
            //fsm.stopQueueRing();
            fsm.stopTimer(1);
            fsm.stopTimer(2);
            fsm.startTimer(7);
            //TODO
            //release call control state machine
            byte[] ui = new byte[]{Message.UI_MESSAGE, Message.PRIVATE_CALL_RELEASE};
            fsm.addUIMessage(ui);

            fsm.setState("StateP1");
        }

        //with the same call identifier
        if (buf[1] == Message.PRIVATE_CALL_ACCEPT) {
            //TODO
            //store the Use floor control indication IE in PRIVATE_CALL_ACCEPT as received floor control indication
            //store SDP answer IE received in PRIVATE_CALL_ACCEPT as answer SDP
            //fsm.stopQueueRing();
            byte[] data = new byte[]{Message.COMMAND_MESSAGE, Message.PRIVATE_CALL_ACCEPT_ACK};
            try {
                InetAddress address = InetAddress.getByName(fsm.IP());
                int port = fsm.calleePort();
                DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                DatagramSocket socket = new DatagramSocket();
                socket.send(packet);
            } catch (Exception e) {
                // TODO: handle exception
            }
            fsm.stopTimer(1);
            fsm.stopTimer(2);

            //establish a media session based on SDP body
            fsm.audioRecorderHandler.startRecord(new AudioRecorderHandler.AudioRecordingCallback() {
                @Override
                public void onRecording(byte[] data, int startIndex, int length) {
                    InetAddress address = null;
                    try {
                        address = InetAddress.getByName(fsm.IP());
                        int port = fsm.calleePort();
                        DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                        DatagramSocket socket = new DatagramSocket();
                        socket.send(packet);
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
                @Override
                public void onStopRecord(String savedPath) {

                }
            });

            byte[] ui = new byte[]{Message.UI_MESSAGE, Message.PRIVATE_CALL_ACCEPT_ACK};
            fsm.addUIMessage(ui);
            //if both the stored received floor control indication and sent fci are TRUE,
            //start floor control as termination floor participant
            fsm.startTimer(5);
            fsm.setState("StateP4");
        }

        if (buf[1] == Message.CANCEL_PRIVATE_CALL) {
            //fsm.stopQueueRing();
            byte[] data = new byte[]{Message.COMMAND_MESSAGE, Message.PRIVATE_CALL_RELEASE};
            try {
                InetAddress address = InetAddress.getByName(fsm.IP());
                int port = fsm.calleePort();
                DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                DatagramSocket socket = new DatagramSocket();
                socket.send(packet);
            } catch (Exception e) {
                // TODO: handle exception
            }
            byte[] ui = new byte[]{Message.UI_MESSAGE, Message.PRIVATE_CALL_RELEASE};
            fsm.addUIMessage(ui);

            fsm.startTimer(3);
            fsm.setState("StateP3");
        }
    }
}

