package com.mwh.audiotest2.FSM;

/**
 * Created by KingFish on 2018/3/13.
 */

public class Message {
    public final static byte COMMAND_MESSAGE = -100;
    public final static byte TIMER_MESSAGE = -101;
    public final static byte UI_MESSAGE = -102;

    public final static byte INITIATE_PRIVATE_CALL = 0;
    public final static byte CANCEL_PRIVATE_CALL = -1;

    public final static byte PRIVATE_CALL_SETUP_REQUEST = 1;
    public final static byte PRIVATE_CALL_ACCEPT = 2;
    public final static byte PRIVATE_CALL_ACCEPT_ACK = 3;
    public final static byte PRIVATE_CALL_RINGING = 4;
    public final static byte PRIVATE_CALL_REJECT = 5;
    public final static byte PRIVATE_CALL_RELEASE = 6;
    public final static byte PRIVATE_CALL_RELEASE_ACK = 7;
    public final static byte PRIVATE_CALL_PTT = 8;
    public final static byte PRIVATE_CALL_PTT_ACK = 9;

    public final static byte ACCEPT_INCOMING_PRIVATE_CALL = 50;
    public final static byte REJECT_INCOMING_PRIVATE_CALL = 51;
    public final static byte RELEASE_CALL = 52;
    public final static byte PTT = 53;

    public final static byte AUTO = 20;
    public final static byte MANUAL = 21;
    public final static byte FULL_DUPLEX = 22;
    public final static byte HALF_DUPLEX = 23;

    public final static byte TFP1_EXPIRE = 100;
    public final static byte TFP1_EXPIRE_N = 101;
    public final static byte TFP2_EXPIRE = 102;
    public final static byte TFP4_EXPIRE = 103;
    public final static byte TFP4_EXPIRE_N = 104;
    public final static byte TFP3_EXPIRE = 105;
    public final static byte TFP3_EXPIRE_N = 106;
    public final static byte TFP5_EXPIRE = 107;
    public final static byte TFP7_EXPIRE = 108;
}
