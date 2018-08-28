package com.mwh.audiotest2;

import android.util.Log;

import com.mwh.audiotest2.Audio.AudioRecorderHandler;
import com.mwh.audiotest2.FSM.Message;
import com.mwh.audiotest2.FSM.states.VoiceFSM;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by KingFish on 2018/3/13.
 */

public class Receiver extends Thread {
    private int port;
    private final VoiceFSM fsm;

    public Receiver(int port, VoiceFSM fsm) {
        this.port = port;
        this.fsm = fsm;
    }

    @Override
    public void run() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        while (true) {
            byte[] data = new byte[AudioRecorderHandler.MAX_DATA_LENGTH];//创建字节数组，指定接收的数据包的大小
            DatagramPacket packet = new DatagramPacket(data, data.length);
            //3.接收客户端发送的数据
            try {
                socket.receive(packet);//此方法在接收到数据报之前会一直阻塞
            } catch (IOException e) {
                e.printStackTrace();
            }
            //4.读取数据
            byte[] buf = packet.getData();

            if (buf[0] == Message.COMMAND_MESSAGE) {
                if (buf[1] == Message.PRIVATE_CALL_SETUP_REQUEST) {
                    String ip = packet.getAddress().toString().substring(1);
                    fsm.setIP(ip);
                    Log.e("ip", fsm.IP());
                }
                handleCommandMessage(buf);
            } else {
                handleAudioData(buf);
            }
        }
    }

    private void handleCommandMessage(byte[] buf) {
        fsm.onReceive(buf);
    }

    private void handleAudioData(byte[] buf) {
        fsm.audioPlayerHandler.prepare();
        fsm.audioPlayerHandler.onPlaying(buf, 0 , buf.length);
        fsm.audioPlayerHandler.stop();
    }
}
