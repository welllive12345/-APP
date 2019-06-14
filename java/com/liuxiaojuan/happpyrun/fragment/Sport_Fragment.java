package com.liuxiaojuan.happpyrun.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.liuxiaojuan.happpyrun.BasicSetting_Activity;
import com.liuxiaojuan.happpyrun.R;
import com.liuxiaojuan.happpyrun.SportMap_Activity;

/**
 *  点击开发运动的 界面
 *  需要 查询数据库，查询出所有的跑步次数，跑步总公里，总时间
 */
public class Sport_Fragment extends Fragment  {

    // 开始运动按钮
    Button startBtn;
    TextView sport_mile;
    TextView sport_count;
    TextView sport_time;
    SharedPreferences sp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater
                .inflate(R.layout.fragment_sport, container, false);
        initView(view);
        return view;
    }

    public void initView(View view) {
        startBtn = (Button) view.findViewById(R.id.btnStart);
        sport_mile = (TextView)view.findViewById(R.id.tv_sport_mile);
        sport_count = (TextView)view.findViewById(R.id.tv_sport_count);
        sport_time = (TextView)view.findViewById(R.id.tv_sport_time);


        //初始化值
        sp = getActivity().getSharedPreferences("basic_info", Context.MODE_PRIVATE);
        if(sp.getString("totalDistance", "")!="" && sp.getString("totalCount", "")!="" &&
                sp.getString("totalDuration", "")!="") {
            sport_mile.setText(sp.getString("totalDistance", ""));
            sport_count.setText(sp.getString("totalCount", ""));
            sport_time.setText(sp.getString("totalDuration", ""));
        }

        startBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(sp.getString("BMI","")!="") {
                    Intent goSportMapIntent = new Intent(getActivity(),
                            SportMap_Activity.class);
                    startActivity(goSportMapIntent);
                }
                else{
                    Toast.makeText(getActivity(), "跑步前需要输入身高、体重计算运动消耗的卡路里~", Toast.LENGTH_LONG).show();
                    Intent goBasicSetIntent = new Intent(getActivity(), BasicSetting_Activity.class);
                    startActivity(goBasicSetIntent);
                }
            }
        });

    }


    @Override
    public void onStart() {
        super.onStart();
        //更新值
        SharedPreferences sp = getActivity().getSharedPreferences("basic_info", Context.MODE_PRIVATE);
        if(sp.getString("totalDistance", "")!="" && sp.getString("totalCount", "")!="" &&
                sp.getString("totalDuration", "")!="") {
            sport_mile.setText(sp.getString("totalDistance", ""));
            sport_count.setText(sp.getString("totalCount", ""));
            sport_time.setText(sp.getString("totalDuration", ""));
        }
    }


}
