package com.liuxiaojuan.happpyrun.alarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.liuxiaojuan.happpyrun.PlanSetting_Activity;
import com.liuxiaojuan.happpyrun.R;

import java.util.Timer;
import java.util.TimerTask;

public class RemindService extends Service {
    static Timer timer = null;
//    SharedPreferences sp;
//    String remindTime;
    private NotificationManager notificationManager;
    private String notificationId = "channelId";
    private String notificationName = "channelName";

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate() {
//        sp = this.getSharedPreferences("basic_info", Context.MODE_PRIVATE);
//        remindTime = sp.getString("remindTime", "");

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //创建NotificationChannel
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(notificationId, notificationName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }


        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentText("跑步小助手："); // 下拉通知啦内容
        builder.setContentTitle("后台运行中。。。。。。");// 下拉通知栏标题
        builder.setAutoCancel(true);// 点击弹出的通知后,让通知将自动取消
        builder.setSmallIcon(R.mipmap.running);// 设置通知图标

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(notificationId);
        }
        startForeground(1, builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
//        Long secondsNextEarlyMorning = getSecondsNextEarlyMorning(remindTime);
//        Intent i = new Intent(this, AlarmReceiver.class);
//        PendingIntent pi = PendingIntent.getBroadcast(this, count++, i, PendingIntent.FLAG_UPDATE_CURRENT);
//        manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + secondsNextEarlyMorning, pi);

        long period = 24 * 60 * 60 * 1000; // 24小时一个周期
        Long delay = intent.getLongExtra("delayTime", 0);
        Log.i("TAG", "接收到的延迟时间秒数为："+delay/1000d);
        if (null == timer) {
            timer = new Timer();
        }

        System.out.println("run32322332task" +delay);

        timer.schedule(new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                System.out.println("ewwwwwwwwwwwwwwwwwwwwwwwww");
                startForeground(1,getNotification());
            }
        },delay, period);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 发送自定义通知栏
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private Notification getNotification() {
        Log.i("TAG", "进入提醒2级");

        //设置点击跳转
        Notification .Builder notification = new Notification.Builder(this);
        notification .setSmallIcon(R.mipmap.inform_icon) ;
        notification .setContentTitle("StepDemo:运动提醒");
        notification .setContentText("快来完成你为今天定下的小目标吧~") ;
        notification .setDefaults(Notification.DEFAULT_SOUND |Notification .DEFAULT_VIBRATE );//设置声音和振动

        //创建启动详细页面的intent对象：
        Intent intent1 = new Intent(this, PlanSetting_Activity.class) ;
        //延时Intent-点击通知栏再跳转：
        PendingIntent pi = PendingIntent.getActivity(this,0,intent1, PendingIntent.FLAG_CANCEL_CURRENT) ;
        notification.setContentIntent(pi);
        notification .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification.setChannelId(notificationId);
        }
        Log.i("TAG", "通知发送完毕1");
        System.out.println("eweweweweweweweew");

        return notification.build();
    }














    /**
     * 获取时间差
     */
//    public static Long getSecondsNextEarlyMorning(String remindTime1) {
//        int hour;
//        int minute;
//
//        //从String获取定时时间：
//        if(remindTime1.split(":")[0].length() >1 && remindTime1.split(":")[0].charAt(0)=='0'){
//            hour = Integer.parseInt(String.valueOf(remindTime1.split(":")[0].charAt(1)));
//        }
//        else{
//            hour= Integer.parseInt(remindTime1.split(":")[0]);
//        }
//        if(remindTime1.split(":")[1].length() >1 && remindTime1.split(":")[1].charAt(0)=='0'){
//            minute = Integer.parseInt(String.valueOf(remindTime1.split(":")[1].charAt(1)));
//        }
//        else{
//            minute = Integer.parseInt(remindTime1.split(":")[1]);
//        }
//        Log.i("TAG", "需要提醒的小时是"+hour+"分钟是"+minute);
//
//        int remind_minutes = hour*60+minute;
//
//        Calendar cal = Calendar.getInstance();
//
//        if (cal.get(Calendar.HOUR_OF_DAY)*60+Calendar.MINUTE - remind_minutes>= 0) {
//            //如果当前时间大于等于计划时间 就计算第二天的时间
//            cal.add(Calendar.DAY_OF_YEAR, 1);
//        } else {
//            cal.add(Calendar.DAY_OF_YEAR, 0);
//        }
//        cal.set(Calendar.HOUR_OF_DAY, hour);
//        cal.set(Calendar.SECOND, 0);
//        cal.set(Calendar.MINUTE, minute);
//        cal.set(Calendar.MILLISECOND, 0);
//        Long seconds = (cal.getTimeInMillis() - System.currentTimeMillis());
//        Log.i("TAG", "过多少秒提醒"+seconds/1000d);
//        return seconds.longValue();
//    }

}

