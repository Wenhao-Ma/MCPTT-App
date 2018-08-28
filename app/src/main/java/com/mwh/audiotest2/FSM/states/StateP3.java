package com.mwh.audiotest2.FSM.states;

import android.util.Log;

import com.mwh.audiotest2.FSM.Message;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by KingFish on 2018/3/13.
 */


public class StateP3 extends State {
    public StateP3(VoiceFSM fsm)
    {
        super(fsm);
    }
    @Override
    public void enterEvent()
    {
        Log.e("state",fsm.getName()+" Enter StateP3");
    }

    @Override
    public void leaveEvent()
    {
        Log.e("state",fsm.getName()+" Leave StateP3");
    }

    @Override
    public void receiveEvent(byte[] buf, int len) {
        if (buf[1] == Message.TFP3_EXPIRE) {
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
        }

        if (buf[1] == Message.TFP3_EXPIRE_N) {
            //terminate media session
            fsm.stopTimer(3);
            fsm.startTimer(7);
            //release call type control state machine
            fsm.setState("StateP1");
        }

        if (buf[1] == Message.PRIVATE_CALL_RELEASE_ACK) {
            fsm.stopTimer(3);
            //terminate media session
            fsm.startTimer(7);
            //release call type control state machine
            fsm.setState("StateP1");
        }
    }
}
