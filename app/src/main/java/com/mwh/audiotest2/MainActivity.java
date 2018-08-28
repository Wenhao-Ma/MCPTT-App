package com.mwh.audiotest2;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.mwh.audiotest2.FSM.Message;
import com.mwh.audiotest2.FSM.states.VoiceFSM;

public class MainActivity extends AppCompatActivity {

    private Button bt_start, bt_stop, bt_ack, bt_rej, bt_ptt;
    private CheckBox checkMode1, checkMode2;
    private TextView calltext, calltext2, ring;
    private EditText IPtext;
    Thread receiver, UIHandler;

    private VoiceFSM fsm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bt_start = (Button) findViewById(R.id.bt_start);
        bt_stop = (Button) findViewById(R.id.bt_stop);
        bt_ack = (Button) findViewById(R.id.bt_ack);
        bt_rej = (Button) findViewById(R.id.bt_rej);
        bt_ptt = (Button) findViewById(R.id.bt_ptt);
        checkMode1 = (CheckBox) findViewById(R.id.Mode1);
        checkMode2 = (CheckBox) findViewById(R.id.Mode2);
        calltext = (TextView) findViewById(R.id.callText);
        calltext2 = (TextView) findViewById(R.id.callText2);
        ring = (TextView) findViewById(R.id.ring);
        IPtext = (EditText) findViewById(R.id.editText);

        fsm = new VoiceFSM("1");
        fsm.setPort(4001, 4001);
        fsm.setDeplux(Message.FULL_DUPLEX);
        fsm.setMode(Message.AUTO);
        fsm.addState("StateP0");
        fsm.addState("StateP1");
        fsm.addState("StateP2");
        fsm.addState("StateP3");
        fsm.addState("StateP4");
        fsm.addState("StateP5");
        fsm.setState("StateP0");

        //开启信令、语音接收线程
        receiver = new Receiver(4001, fsm);
        receiver.start();

        //开启UI控制线程
        UIHandler = new UIMessageListener(fsm, handler);
        UIHandler.start();

        bt_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ip = IPtext.getText().toString();
                fsm.setIP(ip);
                fsm.onReceive(new byte[]{Message.COMMAND_MESSAGE, Message.INITIATE_PRIVATE_CALL});
                Log.e("user","INITIATE_PRIVATE_CALL");
                bt_start.setVisibility(View.INVISIBLE);
                bt_stop.setVisibility(View.VISIBLE);
            }
        });

        bt_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fsm.onReceive(new byte[]{Message.COMMAND_MESSAGE, Message.RELEASE_CALL});
                fsm.onReceive(new byte[]{Message.COMMAND_MESSAGE, Message.CANCEL_PRIVATE_CALL});
                bt_start.setVisibility(View.VISIBLE);
                bt_stop.setVisibility(View.INVISIBLE);
            };
        });

        bt_ack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fsm.onReceive(new byte[]{Message.COMMAND_MESSAGE, Message.ACCEPT_INCOMING_PRIVATE_CALL});
            }
        });

        bt_rej.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fsm.onReceive(new byte[]{Message.COMMAND_MESSAGE, Message.REJECT_INCOMING_PRIVATE_CALL});
            }
        });

        bt_ptt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fsm.onReceive(new byte[]{Message.COMMAND_MESSAGE, Message.PTT});
            }
        });

        checkMode1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    fsm.setMode(Message.AUTO);
                } else {
                    fsm.setMode(Message.MANUAL);
                }
            }
        });

        checkMode2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    fsm.setDeplux(Message.HALF_DUPLEX);
                } else {
                    fsm.setDeplux(Message.FULL_DUPLEX);
                }
            }
        });

    }


    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 1) {
                calltext.setText("等待用户接通");
                calltext.setVisibility(View.VISIBLE);
            }
            if (msg.what == 2) {
                calltext.setText("正在与用户"+fsm.IP()+"通话");
                calltext2.setVisibility(View.INVISIBLE);
                if (fsm.isCaller() && fsm.reqDeplux() == Message.HALF_DUPLEX) {
                    calltext2.setText("占有话语权");
                    calltext2.setVisibility(View.VISIBLE);
                    bt_ptt.setVisibility(View.INVISIBLE);
                }
                if (!fsm.isCaller() && fsm.reqDeplux() == Message.HALF_DUPLEX) {
                    calltext2.setText("对方占有话语权");
                    bt_ptt.setVisibility(View.VISIBLE);
                    calltext2.setVisibility(View.VISIBLE);
                }
                ring.setVisibility(View.INVISIBLE);
                bt_start.setVisibility(View.INVISIBLE);
                bt_stop.setVisibility(View.VISIBLE);
                bt_ack.setVisibility(View.INVISIBLE);
                bt_rej.setVisibility(View.INVISIBLE);
            }
            if (msg.what == 3) {
                calltext.setText("通话结束");
                calltext2.setVisibility(View.INVISIBLE);
                ring.setVisibility(View.INVISIBLE);
                bt_start.setVisibility(View.VISIBLE);
                bt_stop.setVisibility(View.VISIBLE);
                bt_ack.setVisibility(View.INVISIBLE);
                bt_rej.setVisibility(View.INVISIBLE);
                bt_ptt.setVisibility(View.INVISIBLE);
            }
            if (msg.what == 4) {
                calltext.setVisibility(View.INVISIBLE);
                calltext2.setVisibility(View.INVISIBLE);
            }
            if (msg.what == 5) {
                calltext.setText("新来电：用户" + fsm.IP());
                calltext.setVisibility(View.VISIBLE);
            }
            if (msg.what == 6) {
                if (fsm.reqDeplux() == Message.HALF_DUPLEX) {
                    calltext.setText("对讲机：用户" + fsm.IP());
                } else {
                    calltext.setText("新来电：用户" + fsm.IP());
                }
                calltext.setVisibility(View.VISIBLE);
                calltext2.setText("接听/拒绝？");
                calltext2.setVisibility(View.VISIBLE);
                bt_start.setVisibility(View.INVISIBLE);
                bt_stop.setVisibility(View.INVISIBLE);
                bt_ack.setVisibility(View.VISIBLE);
                bt_rej.setVisibility(View.VISIBLE);
            }
            if (msg.what == 7) {
                ring.setVisibility(View.VISIBLE);
            }
        }
    };
}
