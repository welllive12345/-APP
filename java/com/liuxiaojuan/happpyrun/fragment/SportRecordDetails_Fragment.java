package com.liuxiaojuan.happpyrun.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.liuxiaojuan.happpyrun.R;
import com.liuxiaojuan.happpyrun.bean.PathRecord;
import com.liuxiaojuan.happpyrun.utils.StepUtils;

import java.text.DecimalFormat;

/***
 *  运动记录详情页 --- 不带地图的页面
 *  1. 根据传来的PathRecord 值的显示一些信息，见PPT 该页面
 */
public class SportRecordDetails_Fragment extends Fragment {
    private TextView distance;
    private TextView duration;
    private TextView speed;
    private TextView calories;
    private TextView distribution;
    private PathRecord pathRecord;
    private DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private DecimalFormat intFormat = new DecimalFormat("#");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sport_details, container,
                false);

        initSendData(view);
        initView(view);
        return view;
    }

    public void initSendData(View view){
        Bundle receiverBundle = getArguments();
        if (receiverBundle != null) {
            pathRecord = receiverBundle.getParcelable("SPORT_DATA");
        }
    }

    public void initView(View view) {
        distance = (TextView)view.findViewById(R.id.tvDistance ) ;
        duration = (TextView)view.findViewById(R.id.tvDuration  ) ;
        speed = (TextView)view.findViewById(R.id.tvSpeed ) ;
        calories = (TextView)view.findViewById(R.id.tvCalorie) ;
        distribution = (TextView)view.findViewById(R.id.tvDistribution  ) ;

        distance .setText(decimalFormat.format(pathRecord .getDistance()/1000d));
        duration . setText(StepUtils .formatseconds(pathRecord .getDuration() ) );
        speed.setText(decimalFormat .format(pathRecord .getSpeed() ) );
        calories .setText(intFormat.format(pathRecord .getCalorie() ) );
        distribution .setText(decimalFormat .format(pathRecord .getDistribution() ) ) ;

    }
}
