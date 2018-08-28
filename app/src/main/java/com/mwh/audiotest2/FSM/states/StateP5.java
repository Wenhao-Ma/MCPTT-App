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

public class StateP5 extends State{
    public StateP5(VoiceFSM fsm)
    {
        super(fsm);
    }
    @Override
    public void enterEvent()
    {
        Log.e("state",fsm.getName()+" Enter StateP5");
    }

    @Override
    public void leaveEvent()
    {
        Log.e("state",fsm.getName()+" Leave StateP5");
    }

    @Override
    public void receiveEvent(byte[] buf, int len)
    {
        if (buf[1] == Message.TFP4_EXPIRE) {
            byte[] data = new byte[]{Message.COMMAND_MESSAGE, Message.PRIVATE_CALL_ACCEPT};
            try {
                InetAddress address = InetAddress.getByName(fsm.IP());
                int port = fsm.calleePort();
                DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                DatagramSocket socket = new DatagramSocket();
                socket.send(packet);
            } catch (Exception e) {
                // TODO: handle exception
            }
            //fsm.startTimer(4);
        }

        if (buf[1] == Message.PRIVATE_CALL_ACCEPT_ACK) {
            fsm.stopTimer(4);
            //TODO
            //start floor control
            byte[] ui = new byte[]{Message.UI_MESSAGE, Message.PRIVATE_CALL_ACCEPT_ACK};
            fsm.addUIMessage(ui);

            if (fsm.reqDeplux() == Message.FULL_DUPLEX) {
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
            }

            fsm.startTimer(5);
            fsm.setState("StateP4");
        }

        if (buf[1] == Message.TFP4_EXPIRE_N) {
            fsm.startTimer(7);
            //TODO
            //release call control state machine
            fsm.stopTimer(4);
            fsm.setState("StateP1");
        }

        if (buf[1] == Message.TFP2_EXPIRE) {
            byte[] data = new byte[]{Message.COMMAND_MESSAGE, Message.PRIVATE_CALL_REJECT};
            try {
                InetAddress address = InetAddress.getByName(fsm.IP());
                int port = fsm.calleePort();
                DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                DatagramSocket socket = new DatagramSocket();
                socket.send(packet);
            } catch (Exception e) {
                // TODO: handle exception
            }
            fsm.stopTimer(2);
            fsm.startTimer(7);

            byte[] ui = new byte[]{Message.UI_MESSAGE, Message.PRIVATE_CALL_RELEASE};
            fsm.addUIMessage(ui);
            //release call type control state machine
            fsm.setState("StateP1");
        }

        if (buf[1] == Message.ACCEPT_INCOMING_PRIVATE_CALL) {
            //SDP
            //if SDP "a"
            byte[] data = new byte[]{Message.COMMAND_MESSAGE, Message.PRIVATE_CALL_ACCEPT};
            try {
                InetAddress address = InetAddress.getByName(fsm.IP());
                int port = fsm.calleePort();
                DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                DatagramSocket socket = new DatagramSocket();
                socket.send(packet);
            } catch (Exception e) {
                // TODO: handle exception
            }
            //media session
            fsm.stopTimer(2);
            fsm.startTimer(4);
        }

        if (buf[1] == Message.REJECT_INCOMING_PRIVATE_CALL) {
            //SDP
            //if SDP "a"
            byte[] data = new byte[]{Message.COMMAND_MESSAGE, Message.PRIVATE_CALL_REJECT};
            try {
                InetAddress address = InetAddress.getByName(fsm.IP());
                int port = fsm.calleePort();
                DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                DatagramSocket socket = new DatagramSocket();
                socket.send(packet);
            } catch (Exception e) {
                // TODO: handle exception
            }
            fsm.stopTimer(2);
            fsm.startTimer(7);

            byte[] ui = new byte[]{Message.UI_MESSAGE, Message.PRIVATE_CALL_RELEASE};
            fsm.addUIMessage(ui);
            //release call control state machine
            fsm.setState("StateP1");
        }

        if (buf[1] == Message.PRIVATE_CALL_RELEASE) {
            byte[] data = new byte[]{Message.COMMAND_MESSAGE, Message.PRIVATE_CALL_RELEASE_ACK};
            try {
                InetAddress address = InetAddress.getByName(fsm.IP());
                int port = fsm.calleePort();
                DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                DatagramSocket socket = new DatagramSocket();
                socket.send(packet);
            } catch (Exception e) {
                // TODO: handle exception
            }
            fsm.startTimer(7);
            fsm.stopTimer(4);

            byte[] ui = new byte[]{Message.UI_MESSAGE, Message.PRIVATE_CALL_RELEASE};
            fsm.addUIMessage(ui);

            fsm.setState("StateP1");
        }
    }
}
