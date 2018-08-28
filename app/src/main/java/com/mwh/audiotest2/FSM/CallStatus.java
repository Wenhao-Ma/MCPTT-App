package com.mwh.audiotest2.FSM;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by KingFish on 2018/3/13.
 */

public class CallStatus {
    public final static byte TYPE_NONE = -1;

    public final static byte TYPE_SIMPLEX = 0;
    public final static byte TYPE_DUPLEX = 1;
    public ConcurrentHashMap<Integer,Integer> ignoredCallId = new ConcurrentHashMap<>();

    public final static byte CALL_OWNER = 1;

    public final static byte GRANT_TYPE_AUTH_SELF = 0;
    public final static byte GRANT_TYPE_UNAUTH = 1;
    public final static byte GRANT_TYPE_WAIT = 2;
    public final static byte GRANT_TYPE_AUTH_ANOTHER = 3;
    public final static byte GRANT_TYPE_CEASED = -1;

    public final static byte COMM_TYPE_P2P = 0;
    public final static byte COMM_TYPE_P2M = 1;


    public final static byte CALLER_TYPE_CALLEE = 0;
    public final static byte CALLER_TYPE_CALLER = 1;

    public final static byte HOOK_TYPE_NOHOOK = 0;
    public final static byte HOOK_TYPE_HOOK = 1;

    public final static byte TYPE_ENCRYPTION = 1;
    public final static byte TYPE_NO_ENCRYPTION = 0;

    public volatile Integer callId = null;
    public volatile Byte cookie = null;
    public volatile boolean isCalling = false;
    public volatile boolean isConnected = false;
    public volatile boolean isAmbienceListening = false;
    public volatile boolean isLateEntry = false;
    public volatile boolean isVideoCall = false;
    /**
     * 发起时是否请求话语权
     */
    public volatile boolean isReqToSend = true;
    public volatile boolean isCallOwner = true;
    /**
     * 用于在还未发起通话的时候取消通话
     */
    public volatile boolean isCancel = false;
    /**
     * 用于在半双工通话时记录上一次语音包的ssrc
     默认值为null
     */
    public Long audioSsrc = null;

    /**
     * 是否由于超时挂断
     */
    public volatile boolean isSignalTimeout = false;
    public volatile byte disconnectCause = 1;

    public volatile Integer caller = null;
    public volatile Integer callee = null;


    public volatile Integer txParty = null;
    public volatile Integer dbId = null;
    public volatile byte hookStatus = TYPE_NONE;
    public volatile byte commStatus = TYPE_NONE;
    public volatile byte txStatus = TYPE_NONE;
    public volatile byte callerStatus = TYPE_NONE;
    public volatile byte simDuplexStatus = TYPE_NONE;
    public volatile byte encryptStatus = TYPE_NO_ENCRYPTION;
    public volatile byte priority = -1;
    /**
     * 挂断原因
     */
    public volatile byte disconnectedCause = CAUSE_NOT_DEFINED_OR_UNKNOWN;

    //通话挂断原因
    public final static byte CAUSE_NOT_DEFINED_OR_UNKNOWN = 0;
    public final static byte USER_REQUESTED_DISCONNECT = 1;
    public final static byte CALLED_PARTY_BUSY = 2;
    public final static byte CALLED_PARTY_NOT_REACHABLE = 3;
    public final static byte CALLED_PARTY_NOT_SUPPORT_ENCRYPTION = 4;
    public final static byte CONGESTION_IN_INFRASTRUCTURE = 5;
    public final static byte REQUESTED_SERVICE_NOT_AVAILABLE = 8;
    public final static byte PRE_EMPTIVE_USE_OF_RESOURCE = 9;
    public final static byte INVALID_CALL_IDENTIFIER = 10;
    public final static byte CALL_REJECTED_BY_THE_CALLED_PARTY = 11;
    public final static byte EXPIRY_OF_TIMER = 13;
    public final static byte SWMI_REQUESTED_DISCONNECTION = 14;

    //通话priority
    public final static byte NORMAL_CALL = 0x00;
    public final static byte EMERGENCY_CALL = 0x0f;
    public final static byte NONE = -1;
    /**
     * 对方忙时抢占通话
     */
    public final static byte SEIZE_CALL = 0x0e;
    /**
     * 通话加密
     */
    public final static byte ENCRYPT_FALSE = 0x00;
    public final static byte ENCRYPT_TRUE = 0x01;

    public CallStatus()
    {
        commStatus = CallStatus.TYPE_NONE;
        callee = -1;
        caller = -1;
        callId = null;
        cookie = null;
        hookStatus = CallStatus.TYPE_NONE;
        isCalling = false;
        isConnected = false;
        isAmbienceListening = false;
        isLateEntry = false;
        isVideoCall = false;
        isCancel = false;
        disconnectCause = 1;
        isReqToSend = true;
        simDuplexStatus = CallStatus.TYPE_NONE;
        callerStatus = CallStatus.TYPE_NONE;
        txParty = -1;
        txStatus = CallStatus.TYPE_NONE;
        priority = -1;
        isCallOwner = true;
        disconnectedCause = CAUSE_NOT_DEFINED_OR_UNKNOWN;
        ignoredCallId.clear();
        encryptStatus = TYPE_NO_ENCRYPTION;
    }

    public synchronized void reset()
    {
        commStatus = CallStatus.TYPE_NONE;
        callee = -1;
        caller = -1;
        callId = null;
        cookie = null;
        hookStatus = CallStatus.TYPE_NONE;
        isCalling = false;
        isConnected = false;
        isAmbienceListening = false;
        isLateEntry = false;
        isVideoCall = false;
        isCancel = false;
        disconnectCause = 1;
        isReqToSend = true;
        simDuplexStatus = CallStatus.TYPE_NONE;
        callerStatus = CallStatus.TYPE_NONE;
        txParty = -1;
        txStatus = CallStatus.TYPE_NONE;
        priority = -1;
        isCallOwner = true;
        disconnectedCause = CAUSE_NOT_DEFINED_OR_UNKNOWN;
        ignoredCallId.clear();
        encryptStatus = TYPE_NO_ENCRYPTION;
        audioSsrc = null;
    }
}

