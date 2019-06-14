package com.liuxiaojuan.happpyrun;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.liuxiaojuan.happpyrun.alarm.RemindService;
import com.liuxiaojuan.happpyrun.bean.PathRecord;
import com.liuxiaojuan.happpyrun.fragment.Account_Fragment;
import com.liuxiaojuan.happpyrun.fragment.Sport_Fragment;
import com.liuxiaojuan.happpyrun.fragment.StepData_Fragment;
import com.liuxiaojuan.happpyrun.utils.MyDatabaseHelper;
import com.liuxiaojuan.happpyrun.utils.StepUtils;

/**
 *  app 主界面
 *  用于放三个Fragment 子布局
 */
public class MainActivity extends AppCompatActivity {

    // 定义一个FragmentTabHost对象
    private FragmentTabHost mTabHost;
    // 定义一个布局DefaultPassword
    private LayoutInflater layoutInflater;
    // 定义数组来存放3个菜单的 Fragment界面
    private Class fragmentArray[] = { Sport_Fragment.class,
            StepData_Fragment.class, Account_Fragment.class };
    // 定义数组来存放导航图标
    private int imageViewArray[] = { R.drawable.account_change_icon,
            R.drawable.data_change_icon, R.drawable.account_change_icon };
    // Tab 选项卡的文字
    private String textViewArray[] = { "sports", "data", "account" };
    SharedPreferences sp;
//    Long remindTime;

    MyDatabaseHelper myDatabaseHelper;//数据库
    SQLiteDatabase myDatabase;


    //check数据：



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
//        isCall();//设置提醒

        //cehck数据：
//        insertModel( new PathRecord());
    }

    private void initView() {
        layoutInflater = LayoutInflater.from(this);
        // 实例化TabHost对象,得到Tabhost
        mTabHost =   findViewById(R.id.id_tabhost);
        mTabHost.setup(this, getSupportFragmentManager(),
                R.id.id_nav_table_content);



        // 得到fragment的个数
        int count = fragmentArray.length;
        for (int i = 0; i < count; i++) {
            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(textViewArray[i])
                    .setIndicator(getTabItemView(i));
            mTabHost.addTab(tabSpec, fragmentArray[i], null);
        }

        //初始化值
        sp = this.getSharedPreferences("basic_info", Context.MODE_PRIVATE);
    }

    // 给Tab 按钮设置图标和文字
    private View getTabItemView(int index) {
        View view = layoutInflater.inflate(R.layout.item_bottom_nav, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.nav_icon_iv);
        imageView.setImageResource(imageViewArray[index]);

        TextView textView = (TextView) view.findViewById(R.id.nav_text_tv);
        textView.setText(textViewArray[index]);
        return view;
    }


    // 监听时间变化提醒用户锻炼

    private void isCall() {
        Log.i("TAG", "进入监视提醒1级");
        Boolean remind = Boolean.parseBoolean(sp.getString("whetherRemind", ""));
        Log.i("TAG", "需要提醒："+remind);
        Intent intent = new Intent(this, RemindService.class);
        if (remind) {
//            remindNotify();
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //android8.0以上通过startForegroundService启动service
                startForegroundService(intent);
            }else{
                startService(intent);
            }
            Log.i("TAG", "进入Service");
        }
        else{
            stopService(intent);
        }
    }

    // 通知栏提醒
    private void remindNotify() {
        Log.i("TAG", "进入提醒2级");
        String targetSteps = sp.getString( "targetDailySteps", "");
        Log.i("TAG", "目标步数为："+sp.getString( "targetDailySteps", ""));
        //设置点击跳转
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Notification .Builder notification = new Notification.Builder(MainActivity .this ) ;
        notification .setAutoCancel(true) ;
        notification .setSmallIcon(R.mipmap.inform_icon) ;
        notification .setContentTitle("StepDemo:运动提醒");
        notification .setContentText("快来完成今天的小目标（"+targetSteps+"步）吧~") ;
        notification .setDefaults(Notification.DEFAULT_SOUND |Notification .DEFAULT_VIBRATE );//设置声音和振动
        notification. setWhen(System .currentTimeMillis()) ;
        //创建启动详细页面的intent对象：
        Intent intent = new Intent(this,MainActivity.class ) ;
        //延时Intent-点击通知栏再跳转：
        PendingIntent pi = PendingIntent.getActivity(MainActivity .this,0,intent, PendingIntent.FLAG_CANCEL_CURRENT) ;
        notification.setContentIntent(pi) ;
//        notificationManager.notify(NOTIFYID,notification .build());//发送通知
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel("ccb", "CCB", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
            notification.setChannelId("ccb");
            notificationManager.notify(0,notification.build());}
        else{
            notificationManager.notify(0,notification.build());
        }

        Log.i("TAG", "通知发送完毕1");
    }

    //转换计划的时间为毫秒
    protected long convertPlanTimeToLong(String strTime) {
        // TODO Auto-generated method stub
        String []timeArry=strTime.split(":");
        long longTime=Integer.parseInt(timeArry[0])*1000*60*60+Integer.parseInt(timeArry[1])*1000*60;
        return longTime;
    }


    //测试数据：
    private void insertModel(PathRecord pathRecord1 ) {
        myDatabaseHelper = new MyDatabaseHelper(this);
        myDatabase = myDatabaseHelper.getWritableDatabase();
        Log.i("TAG", "数据库stepDemoData.db被创建？");

        //Add:
        ContentValues contentValues = new ContentValues();

        //共12个属性：
        contentValues.put("id", pathRecord1 .getId());
        contentValues.put("startTime", pathRecord1 .getStartTime());
        contentValues.put("startPoint", StepUtils.amapLocationToString(pathRecord1 .getStartpoint()));//位置点-经纬度用,分隔
        contentValues.put("endTime", pathRecord1 .getEndTime());
        contentValues.put("endPoint", StepUtils.amapLocationToString(pathRecord1 .getEndpoint()));
        contentValues.put("duration", pathRecord1 .getDuration());
        contentValues.put("distance", pathRecord1 .getDistance());
        contentValues.put("calories", pathRecord1 .getCalorie());
        contentValues.put("speed", pathRecord1.getSpeed());
        contentValues.put("distribution", pathRecord1 .getDistribution());
        contentValues.put("pathLinePoints", StepUtils.getLatLngPathLineString(pathRecord1 .getPathline()));//位置点之间用;分隔、经纬度用,分隔
        contentValues.put("dateTag", pathRecord1 .getDateTag());

        Log.i("TAG", "contentValues赋值完毕");

        long rowNumber = myDatabase.insert("sportData",null,contentValues);
        Log.i("TAG", "数据被插入");

        if(rowNumber != -1){
            Toast.makeText(this,"运动数据已保存~",Toast.LENGTH_SHORT ).show();//注意table名称和Database名称不一样

        }

    }
}
