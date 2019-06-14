package com.liuxiaojuan.happpyrun;

import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.TextView;

public class MyCountTimer extends CountDownTimer {

    public static final int TIME_COUNT = 3100;//倒计时总时间为31S，时间防止从29s开始显示（以倒计时30s为例子）
    private TextView btn;
    private Button backBtn;

    /**
     * 参数 millisInFuture         倒计时总时间（如30s,60S，120s等）
     * 参数 countDownInterval    渐变时间（每次倒计1s）
     * 参数 btn               点击的按钮(因为Button是TextView子类，为了通用我的参数设置为TextView）
     * 参数 endStrRid   倒计时结束后，按钮对应显示的文字
     */
    public MyCountTimer(long millisInFuture, long countDownInterval, TextView btn,Button backBtn) {
        super(millisInFuture, countDownInterval);
        this.btn = btn;
        this.backBtn = backBtn;
        btn.setVisibility(View.VISIBLE);
        backBtn.setVisibility(View.VISIBLE);
    }

    /**
     * 参数上面有注释
     */
    public MyCountTimer(TextView btn,Button backBtn) {
        super(TIME_COUNT, 1000);
        this.btn = btn;
        this.backBtn = backBtn;
        btn.setVisibility(View.VISIBLE);
        backBtn.setVisibility(View.VISIBLE);
    }

    /**
     * 计时完毕时触发
     */
    @Override
    public void onFinish() {
        
        btn.setVisibility(View.GONE);
        backBtn.setVisibility(View.GONE);
    }

    /**
     * 计时过程显示
     */
    @Override
    public void onTick(long millisUntilFinished) {
        btn.setEnabled(false);
        //每隔一秒修改一次UI
        btn.setText(millisUntilFinished / 1000+"");

        // 设置透明度渐变动画
        final AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);

        //设置动画持续时间
        alphaAnimation.setDuration(1000);
        btn.startAnimation(alphaAnimation);

        // 设置缩放渐变动画
        final ScaleAnimation scaleAnimation =new ScaleAnimation(0.5f, 2f, 0.5f,2f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(1000);
        btn.startAnimation(scaleAnimation);
    }
}
