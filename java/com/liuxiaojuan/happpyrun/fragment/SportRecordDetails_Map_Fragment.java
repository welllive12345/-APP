package com.liuxiaojuan.happpyrun.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.utils.SpatialRelationUtil;
import com.amap.api.maps.utils.overlay.SmoothMoveMarker;
import com.liuxiaojuan.happpyrun.R;
import com.liuxiaojuan.happpyrun.bean.PathRecord;
import com.liuxiaojuan.happpyrun.utils.PathSmoothTool;
import com.liuxiaojuan.happpyrun.utils.StepUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/***
 *  运动地图记录详情页
 *  1. 在地图上根据传来的PathRecord 值的经纬度等信息，画出轨迹等
 */
public class SportRecordDetails_Map_Fragment extends Fragment implements LocationSource {

    // 地图
    private AMap aMap;
    private MapView mapView;
    private PathRecord pathRecord;
    // 获取轨迹坐标点
    List<LatLng> mOriginList;

    private TextView tv_Distance_map;
    private TextView tv_Duration_map;
    private DecimalFormat decimalFormat = new DecimalFormat("0.00");

    //画地图：
    private PolylineOptions polylineOptions;
    private PathSmoothTool mpathSmoothTool = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sport_details_map, container,
                false);

        // 初始化地图组件
        mapView =  view.findViewById(R.id.detalsMap);
        mapView.onCreate(savedInstanceState);

        //底部文字设置
        tv_Distance_map = view.findViewById(R.id.tv_Distance_map) ;
        tv_Duration_map  = view.findViewById(R.id.tv_Duration_map ) ;



        initSendData();
        initMap();
        return view;
    }

    private void initSendData() {

        // 得到转到这个 fragmnet 的值
        Bundle receiverBundle = getArguments();
        if (receiverBundle != null) {
            pathRecord = receiverBundle.getParcelable("SPORT_DATA");
        }

        mOriginList = pathRecord.getPathline();

        tv_Distance_map.setText(decimalFormat.format(pathRecord .getDistance()/1000d)+"公里");
        tv_Duration_map.setText(StepUtils.formatseconds(pathRecord .getDuration()));

    }

    /**
     * 地图开始
     */
    private void initMap() {
        if (aMap == null) {
            aMap = mapView.getMap();
            // 缩放级别
            aMap.moveCamera(CameraUpdateFactory.zoomTo(19));
            aMap.getUiSettings().setZoomControlsEnabled(false);
            aMap.getUiSettings().setCompassEnabled(true);// 设置显示指南针
            setUpMap();
            initPolyline();
        }
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        // 设置定位回调
        // 自定义系统定位小蓝点
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {


    }

    @Override
    public void deactivate() {

    }


//    public void initLines() {
//        PathSmoothTool mpathSmoothTool = new PathSmoothTool();
//
//        //设置平滑处理的等级
//        mpathSmoothTool.setIntensity(4);
//        List<LatLng> pathoptimizeList = mpathSmoothTool.pathOptimize(mOriginList);
//
//        //绘制轨迹，移动地图显示
//        if (mOriginList != null && mOriginList.size() > 0) {
//            aMap.addPolyline(new PolylineOptions().addAll(pathoptimizeList).color(Color.GREEN));
//            aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(getLatLngBounds(mOriginList), 200));
//        }
//    }


    private void initPolyline() {
        polylineOptions = new PolylineOptions();
        polylineOptions.color(getResources().getColor(R.color.colorAccent));
        polylineOptions.width(20f);
        polylineOptions.useGradient(true);

        mpathSmoothTool = new PathSmoothTool();
        mpathSmoothTool.setIntensity(4);


        PathSmoothTool mpathSmoothTool = new PathSmoothTool();
        //设置平滑处理的等级
        mpathSmoothTool.setIntensity(4);
        List<LatLng> pathoptimizeList = new ArrayList<>(mpathSmoothTool.pathOptimize(mOriginList ));

        if (!pathoptimizeList.isEmpty()) {
            LatLngBounds bounds = getLatLngBounds(pathoptimizeList);
            aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));

            aMap.addPolyline(new PolylineOptions().
                    addAll(pathoptimizeList).width(10).color(Color.GREEN));
        } else {
            LatLngBounds bounds = getLatLngBounds(mOriginList );
            aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));

            aMap.addPolyline(new PolylineOptions().
                    addAll(mOriginList ).width(10).color(Color.GREEN));
        }
        SmoothMoveMarker smoothMarker = new SmoothMoveMarker(aMap);
// 设置滑动的图标
        smoothMarker.setDescriptor(BitmapDescriptorFactory.fromResource(R.mipmap.run_man_result));
        LatLng drivePoint = mOriginList .get(0);
        Pair<Integer, LatLng> pair = SpatialRelationUtil.calShortestDistancePoint(mOriginList , drivePoint);
        mOriginList .set(pair.first, drivePoint);
        List<LatLng> subList = mOriginList .subList(pair.first, mOriginList.size());
        // 设置滑动的轨迹左边点
        smoothMarker.setPoints(subList);
// 设置滑动的总时间
        smoothMarker.setTotalDuration(40);
// 开始滑动
        smoothMarker.startSmoothMove();

    }


    /**
     * 根据自定义内容获取缩放bounds
     */
    private LatLngBounds getLatLngBounds(List<LatLng> pointList) {
        LatLngBounds.Builder b = LatLngBounds.builder();
        for (int i = 0; i < pointList.size(); i++) {
            LatLng p = pointList.get(i);
            b.include(p);
        }
        return b.build();
    }
}
