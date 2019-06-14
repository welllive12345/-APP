package com.liuxiaojuan.happpyrun;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends Activity implements View.OnClickListener {
    private int recLen=3;
    Handler mHandler = new Handler();
    Button splash_man ;
    Button splash_woman ;
    TextView btn_skip;

    Timer timer = new Timer();
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initView();
        splash_man = (Button) findViewById(R.id.splash_man) ;
        splash_woman = (Button) findViewById(R.id.splash_woman);
        splash_man.setVisibility(View.VISIBLE );
        splash_woman.setVisibility(View.INVISIBLE);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                splash_man.setVisibility(View.INVISIBLE);
                splash_woman.setVisibility(View.VISIBLE);
            }
        },1500);

        final Intent intent = new Intent(SplashActivity.this,MainActivity.class );

        timer.schedule(task, 1000, 1000);//等待时间一秒，停顿时间一秒
        /**
         * 正常情况下不点击跳过
         */
        handler = new Handler();
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                //从闪屏界面跳转到首界面
                startActivity(intent);
                finish();
            }
        }, 3000);//延迟5S后发送handler信息

    }

    private void initView() {
        btn_skip =(TextView)findViewById(R.id.btn_skip);//跳过
        btn_skip.setOnClickListener(this);// 跳过监听
    }

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() { // UI thread
                @Override
                public void run() {
                    recLen--;
                    btn_skip.setText("跳过 " + recLen);
                    if (recLen < 0) {
                        timer.cancel();
                        btn_skip.setVisibility(View.GONE);//倒计时到0隐藏字体
                    }
                }
            });
        }
    };

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_skip:
                //从闪屏界面跳转到首界面
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                if (runnable != null) {
                    handler.removeCallbacks(runnable);
                }
                break;
            default:
                break;
        }
    }

}
