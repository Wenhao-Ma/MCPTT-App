package com.mwh.audiotest2.FSM.states;

import android.util.Log;

import com.mwh.audiotest2.FSM.Message;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by KingFish on 2018/3/13.
 */

public class StateP0 extends State{
    public StateP0(VoiceFSM fsm)
    {
        super(fsm);
    }
    @Override
    public void enterEvent()
    {
        Log.e("state",fsm.getName()+" Enter StateP0");
        fsm.stopAllTimer();
    }

    @Override
    public void leaveEvent()
    {
        Log.e("state",fsm.getName()+" Leave StateP0");
    }

    @Override
    public void receiveEvent(byte[] buf, int len)
    {
        if (buf[1] == Message.INITIATE_PRIVATE_CALL) {
            //TODO
            //call identifier
            //caller ID, callee ID
            //store MODE
            //call type control machine
            //if end-to-end context
            //floor control indication
            //user location
            //SDP
            fsm.setCaller(true);
            byte caller1 = (byte) (fsm.callerPort() >> 7);
            byte caller2 = (byte) (fsm.callerPort() & Byte.MAX_VALUE);
            byte callee1 = (byte) (fsm.calleePort() >> 7);
            byte callee2 = (byte) (fsm.calleePort() & Byte.MAX_VALUE);

            byte[] data = new byte[]{Message.COMMAND_MESSAGE, Message.PRIVATE_CALL_SETUP_REQUEST,
                    caller1, caller2, callee1, callee2,
                    fsm.mode(), fsm.Deplux()};
            fsm.setReqMode(fsm.mode());
            fsm.setReqDeplux(fsm.Deplux());
            try {
                InetAddress address = InetAddress.getByName(fsm.IP());
                int port = fsm.calleePort();
                DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                DatagramSocket socket = new DatagramSocket();
                socket.send(packet);
            } catch (Exception e) {
                // TODO: handle exception
            }
            byte[] ui = new byte[]{Message.UI_MESSAGE, Message.INITIATE_PRIVATE_CALL};
            fsm.addUIMessage(ui);

            fsm.startTimer(1);
            fsm.setState("StateP2");
        }

        /**
         //call identifier != stored call identifier && media session can't be established
         if (buf[1] == Message.PRIVATE_CALL_SETUP_REQUEST) {
         //TODO
         //store call identifier IE in the received msg as call identifier
         //store the caller ID in the msg as caller ID
         //store own user ID as callee ID
         byte[] data = new byte[]{Message.PRIVATE_CALL_REJECT};
         try {
         InetAddress address = InetAddress.getByName(fsm.IP());
         int port = 8801;
         DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
         DatagramSocket socket = new DatagramSocket();
         socket.send(packet);
         } catch (Exception e) {
         // TODO: handle exception
         }
         fsm.startTimer(7);
         fsm.setState("StateP1");
         }
         */

        //mode == AUTO && call identifier != stored call identifier && media session can be established
        if (buf[1] == Message.PRIVATE_CALL_SETUP_REQUEST && buf[6] == Message.AUTO) {
            //TODO
            //call identifier IE
            //create call type control state machine
            //caller ID, callee ID
            //Use floor control indication IE
            //if SDP 'a' ……
            //SDP
            fsm.setCaller(false);
            int callee = (buf[2] << 7) | (buf[3]);
            int caller = (buf[4] << 7) | (buf[5]);
            fsm.setPort(caller, callee);

            fsm.setReqMode(buf[6]);
            fsm.setReqDeplux(buf[7]);

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
            byte[] ui = new byte[]{Message.UI_MESSAGE, Message.PRIVATE_CALL_SETUP_REQUEST};
            fsm.addUIMessage(ui);

            fsm.startTimer(4);
            fsm.setState("StateP5");
        }

        //mode == MANUAL
        if (buf[1] == Message.PRIVATE_CALL_SETUP_REQUEST && buf[6] == Message.MANUAL) {
            //TODO
            //call identifier IE
            //create call type control state machine
            //caller ID, callee ID
            //Use floor control indication IE
            fsm.setCaller(false);
            int callee = (buf[2] << 7) | (buf[3]);
            int caller = (buf[4] << 7) | (buf[5]);
            fsm.setPort(caller, callee);

            fsm.setReqMode(buf[6]);
            fsm.setReqDeplux(buf[7]);

            byte[] data = new byte[]{Message.COMMAND_MESSAGE, Message.PRIVATE_CALL_RINGING};
            try {
                InetAddress address = InetAddress.getByName(fsm.IP());
                int port = fsm.calleePort();
                DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                DatagramSocket socket = new DatagramSocket();
                socket.send(packet);
            } catch (Exception e) {
                // TODO: handle exception
            }
            byte[] ui = new byte[]{Message.UI_MESSAGE, Message.PRIVATE_CALL_SETUP_REQUEST};
            fsm.addUIMessage(ui);

            fsm.startTimer(2);
            fsm.setState("StateP5");
        }
    }

}

