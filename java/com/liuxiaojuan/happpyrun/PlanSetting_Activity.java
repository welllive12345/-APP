package com.liuxiaojuan.happpyrun;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.liuxiaojuan.happpyrun.alarm.RemindService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *  运动计划设置页面
 *  弹框  设置每天跑步目标公里， 是否提醒， 提醒时间
 *
 */
public class PlanSetting_Activity extends AppCompatActivity implements View.OnClickListener{
    LinearLayout date_layout;
    LinearLayout time_layout;
    private EditText targetDailySteps_edit;
    private TextView date_tv;
    private TextView time_tv;
    private Switch whetherRemindBtn;
    private Button planSaveBtn;
    private String mTime;
    private String mDate;
    private Calendar calendar = Calendar.getInstance();
    private int mYear, mMonth, mDay;
    private int mHour;
    private Integer mMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paln_setting);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        date_layout = findViewById(R.id.notify_date);
        time_layout = findViewById(R.id.notify_time);
        targetDailySteps_edit = (EditText)findViewById(R.id.targetDailySteps_edit);
        time_tv = (TextView)  findViewById(R.id.remindTime_edit);
        date_tv = (TextView) findViewById(R.id.remindDate_edit);
        planSaveBtn = (Button) findViewById(R.id.planInfo_save_button);
        whetherRemindBtn = (Switch) findViewById(R.id.whetherRemindBtn);


        date_layout.setOnClickListener(this);
        time_layout.setOnClickListener(this);
        planSaveBtn.setOnClickListener(this);
        initView();

        Toast.makeText(this, "接下来2天需要我督促你吗？不需要提醒的话可以自己关掉哦~", Toast.LENGTH_LONG).show();

    }

    private void initView() {
        Log.i("TAG", "进入计划页面");
        SharedPreferences sp = this.getSharedPreferences("basic_info", Context.MODE_PRIVATE);
        if(sp.getString("targetDailySteps", "")!="" && sp.getString("remindTime", "")!="" &&
                sp.getString("whetherRemind", "")!=""&&sp.getString("remindDate", "")!="") {
            targetDailySteps_edit.setText(sp.getString("targetDailySteps", ""));
            date_tv.setText(sp.getString("remindDate", ""));
            time_tv.setText(sp.getString("remindTime", ""));
            whetherRemindBtn.setChecked(Boolean.parseBoolean(sp.getString("whetherRemind", "")));
            Log.i("TAG", "计划页是否提醒是"+Boolean.parseBoolean(sp.getString("whetherRemind", "")));
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.notify_date:
                alertDateDialog();
                break;
            case R.id.notify_time:
                alertTimeDialog();
                break;
            case R.id.planInfo_save_button:
                savePlanInfo();
                notifyService();
                finish();
                break;
            default:
                break;
        }

    }


    private void savePlanInfo() {
        final SharedPreferences.Editor editor = this.getSharedPreferences("basic_info", Context.MODE_PRIVATE).edit();
        editor.putString("whetherRemind", String.valueOf(whetherRemindBtn.isChecked()));
        editor.putString("targetDailySteps",targetDailySteps_edit.getText().toString());
        editor.putString("remindDate",date_tv.getText().toString());
        editor.putString("remindTime",time_tv.getText().toString());
        editor.apply();
    }

    public void notifyService() {
        mDate = date_tv.getText().toString();
        mTime = time_tv.getText().toString();
        Log.e("xx", "日期= " + mDate + "   时间= " + mTime);
        if (TextUtils.isEmpty(mDate)) {
            Toast.makeText(getApplicationContext(), "请选择提醒日期", Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(mTime)) {
            Toast.makeText(getApplicationContext(), "请选择提醒时间", Toast.LENGTH_SHORT).show();
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date;
        long value = 0;
        String str_date = mDate + " " + mTime;
        try {
            date = sdf.parse(str_date);
            value = date.getTime();
            System.out.println("当前设置时间:" + value);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.e("str_date=", str_date);
        Log.e("value=", value + "");
        long value2 = System.currentTimeMillis();
        System.out.println(value + " ----=---=  " + value2);
        Log.i("TAG", "当前系统时间为："+sdf.format(new Date(value2)));
        Log.i("TAG", "多长时间后提醒："+((value-value2)));
        if (value <= value2) {
            Toast.makeText(getApplicationContext(), "选择时间不能小于当前系统时间，提醒未保存！", Toast.LENGTH_SHORT).show();
            Intent intent1 = new Intent(this, RemindService.class);
            stopService(intent1);
            return;
        }
        Long delaytime = (value - value2);

        Intent intent = new Intent(this, RemindService.class);
        intent.putExtra("delayTime", delaytime);
        Log.i("TAG", "多少秒后提醒："+delaytime/1000d);

        if (whetherRemindBtn.isChecked()) { // switch 打开
            Toast.makeText(this, "跑步时间提醒设置成功", Toast.LENGTH_SHORT).show();
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //android8.0以上通过startForegroundService启动service
                startForegroundService(intent);
            }else{
                startService(intent);
            }
        } else { // switch 关闭   或者提醒后点击通知栏, 关闭switch, 通过点击按钮来关闭历史上的service, 否则周期到了后可能还会响通知
            Toast.makeText(this, "仅设置时间,但未打开提醒", Toast.LENGTH_SHORT).show();
            stopService(intent);
        }
    }


    public void alertDateDialog() {
        // 通过自定义控件AlertDialog实现
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = (LinearLayout) View.inflate(this,
                R.layout.date_dialog, null);
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);
        // 设置日期简略显示 否则详细显示 包括:星期\周
        datePicker.setCalendarViewShown(false);
        // 初始化当前日期
        calendar.setTimeInMillis(System.currentTimeMillis());
        datePicker.init(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), null);
        // 设置date布局
        builder.setView(view);
        builder.setTitle("设置日期信息");
        builder.setPositiveButton("确  定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        // 日期格式
                        StringBuffer sb = new StringBuffer();
                        sb.append(String.format("%d-%02d-%02d",
                                datePicker.getYear(),
                                datePicker.getMonth() + 1,
                                datePicker.getDayOfMonth()));
                        date_tv.setText(sb);
                        // 赋值后面闹钟使用
                        mYear = datePicker.getYear();
                        mMonth = datePicker.getMonth();
                        mDay = datePicker.getDayOfMonth();
                        dialog.cancel();
                    }
                });
        builder.setNegativeButton("取  消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    
    public void alertTimeDialog() {
        // 自定义控件
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = (LinearLayout) View.inflate(this,
                R.layout.time_dialog, null);
        final TimePicker timePicker = (TimePicker) view
                .findViewById(R.id.time_picker);
        // 初始化时间
        calendar.setTimeInMillis(System.currentTimeMillis());
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));


        // 设置time布局
        builder.setView(view);
        builder.setTitle("设置时间信息");
        builder.setPositiveButton("确  定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        mHour = timePicker.getCurrentHour();
                        mMinute = timePicker.getCurrentMinute();
                        // 时间小于10的数字 前面补0 如01:12:00
                        time_tv.setText(new StringBuilder()
                                .append(mHour < 10 ? "0" + mHour
                                        : mHour)
                                .append(":")
                                .append(mMinute < 10 ? "0" + mMinute
                                        : mMinute).append(":00"));
                        dialog.cancel();
                    }
                });
        builder.setNegativeButton("取  消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        dialog.cancel();
                    }
                });
        builder.create().show();

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
//        Log.i("TAG", "日历此时的小时为"+cal.get(Calendar.HOUR_OF_DAY)+"分钟为"+cal.get(Calendar.MINUTE ));
//
//        if (cal.get(Calendar.HOUR_OF_DAY)*60+cal.get(Calendar.MINUTE) - remind_minutes>= 0) {
//            Log.i("TAG", "加了一天");
//            //如果当前时间大于等于计划时间 就计算第二天的时间
//            cal.add(Calendar.DAY_OF_YEAR, 1);
//        } else {
//            Log.i("TAG", "没加");
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
