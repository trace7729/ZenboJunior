package com.example.ttsapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.SpeakConfig;
import com.robot.asus.robotactivity.RobotActivity;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends RobotActivity {
    private static TextView mTextTv;
    ImageButton mVoiceBtn;

    public MainActivity() {
        super(robotCallback, robotListenCallback);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextTv = findViewById(R.id.textTv);
        mVoiceBtn = findViewById(R.id.voiceBtn);

        mVoiceBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                robotAPI.robot.speakAndListen("Speech to Text", new SpeakConfig().timeout(20));
            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        robotAPI.robot.stopSpeakAndListen();
    }
    public static RobotCallback robotCallback = new RobotCallback() {
        @Override
        public void onResult(int cmd, int serial, RobotErrorCode err_code, Bundle result) {
            super.onResult(cmd, serial, err_code, result);
        }

        @Override
        public void onStateChange(int cmd, int serial, RobotErrorCode err_code, RobotCmdState state) {
            super.onStateChange(cmd, serial, err_code, state);
        }

        @Override
        public void initComplete() {
            super.initComplete();

        }
    };

    public static RobotCallback.Listen robotListenCallback = new RobotCallback.Listen() {
        @Override
        public void onFinishRegister() {
        }
        @Override
        public void onVoiceDetect(JSONObject jsonObject) {
        }
        @Override
        public void onSpeakComplete(String s, String s1) {
        }
        @Override
        public void onEventUserUtterance(JSONObject jsonObject) {
            String text;
            text = "onEventUserUtterance: " + jsonObject.toString();
            Log.d("Utterance:", text);
        }

        @Override
        public void onResult(JSONObject jsonObject) {
            JSONObject object;
            String text;
            text = jsonObject.toString();
            mTextTv.setText(text);
                try {
                    object = jsonObject.getJSONObject("event_slu_query");
                    text = object.getString("correctedSentence");
                    mTextTv.setText(text);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        }
        @Override
        public void onRetry(JSONObject jsonObject) {
        }
    };
}
