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
public class StateP4 extends State {
    private Thread call = null;

    public StateP4(VoiceFSM fsm)
    {
        super(fsm);
    }
    @Override
    public void enterEvent()
    {
        Log.e("state",fsm.getName()+" Enter StateP4");
    }

    @Override
    public void leaveEvent()
    {
        Log.e("state",fsm.getName()+" Leave StateP4");
    }

    @Override
    public void receiveEvent(byte[] buf, int len)
    {
        //other client
        if (buf[1] == Message.PRIVATE_CALL_SETUP_REQUEST) {
            byte[] data = new byte[]{Message.COMMAND_MESSAGE, Message.PRIVATE_CALL_REJECT};
            try {
                InetAddress address = InetAddress.getByName(fsm.IP());
                int port = 8800; //other port
                DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                DatagramSocket socket = new DatagramSocket();
                socket.send(packet);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }

        if (buf[1] == Message.RELEASE_CALL) {
            //System.out.println("挂断电话！");
            byte[] data = new byte[]{Message.COMMAND_MESSAGE, Message.PRIVATE_CALL_RELEASE};
            try {
                InetAddress address = InetAddress.getByName(fsm.IP());
                int port = fsm.calleePort(); //另一个
                DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                DatagramSocket socket = new DatagramSocket();
                socket.send(packet);
            } catch (Exception e) {
                // TODO: handle exception
            }
            fsm.startTimer(3);
            fsm.setState("StateP3");

            byte[] ui = new byte[]{Message.UI_MESSAGE, Message.PRIVATE_CALL_RELEASE};
            fsm.addUIMessage(ui);

            fsm.audioRecorderHandler.stopRecord();
            fsm.audioPlayerHandler.stop();
        }

        if (buf[1] == Message.PRIVATE_CALL_RELEASE) {
            //System.out.println("对方挂断电话！");
            Log.e("state","caller has left!");
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
            //terminate media session
            fsm.audioRecorderHandler.stopRecord();
            fsm.audioPlayerHandler.stop();
            //release call control state machine
            byte[] ui = new byte[]{Message.UI_MESSAGE, Message.PRIVATE_CALL_RELEASE};
            fsm.addUIMessage(ui);

            fsm.setState("StateP1");
            fsm.startTimer(7);

        }

        if (buf[1] == Message.TFP5_EXPIRE) {
            //terminate media session
            fsm.audioRecorderHandler.stopRecord();
            fsm.audioPlayerHandler.stop();

            fsm.stopTimer(5);
            fsm.startTimer(7);
            //release call control state machine
            fsm.setState("StateP1");
        }

        if (buf[1] == Message.PTT && !fsm.isCaller() && fsm.reqDeplux() == Message.HALF_DUPLEX) {
            Log.e("PTT","PTT");
            byte[] data = new byte[]{Message.COMMAND_MESSAGE, Message.PRIVATE_CALL_PTT};
            try {
                InetAddress address = InetAddress.getByName(fsm.IP());
                int port = fsm.calleePort();
                DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                DatagramSocket socket = new DatagramSocket();
                socket.send(packet);
            } catch (Exception e) {
                // TODO: handle exception
            }
            fsm.audioPlayerHandler.stop();
        }

        if (buf[1] == Message.PRIVATE_CALL_PTT && fsm.isCaller() && fsm.reqDeplux() == Message.HALF_DUPLEX) {
            Log.e("PTT","receive PTT");
            byte[] data = new byte[]{Message.COMMAND_MESSAGE, Message.PRIVATE_CALL_PTT_ACK};
            try {
                InetAddress address = InetAddress.getByName(fsm.IP());
                int port = fsm.calleePort();
                DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                DatagramSocket socket = new DatagramSocket();
                socket.send(packet);
            } catch (Exception e) {
                // TODO: handle exception
            }
            fsm.setCaller(false);
            fsm.audioRecorderHandler.stopRecord();

            byte[] ui = new byte[]{Message.UI_MESSAGE, Message.PRIVATE_CALL_ACCEPT_ACK};
            fsm.addUIMessage(ui);
        }

        if (buf[1] == Message.PRIVATE_CALL_PTT_ACK && !fsm.isCaller() && fsm.reqDeplux() == Message.HALF_DUPLEX) {
            Log.e("PTT","change PTT");
            fsm.setCaller(true);
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
        }

    }
}

