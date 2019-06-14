package com.liuxiaojuan.happpyrun;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;
import com.liuxiaojuan.happpyrun.bean.PathRecord;
import com.liuxiaojuan.happpyrun.ui.UIHelperUtil;
import com.liuxiaojuan.happpyrun.utils.MyDatabaseHelper;
import com.liuxiaojuan.happpyrun.utils.PathSmoothTool;
import com.liuxiaojuan.happpyrun.utils.StepUtils;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 开始跑步页面  ,分跑步模式和地图模式
 * <p>
 * 当开始跑步时 倒计时3秒，
 * 同时利用 PathRecord 设置 startTime 、记录路径的经纬度，
 * 可以通过记录的两个经纬度的距离计算跑了多少公里
 * 数据库里面保存:
 * 当前时间的时间戳，  距离， 时长， 开始时间， 结束时间， 开始位置的经纬度， 结束位置的经纬度，路线的经纬度
 * 根据体重计算的卡路里， 平均时速（公里/小时） 平均配速（分钟/公里）
 * <p>
 * tips:
 * onLocationChanged 可以每隔几秒钟获得一次当前位置的经纬度，你可以把它保存到List 里面，
 * 当跑完步把list 保存到PathRecord 类，从而保存到数据库
 */

public class SportMap_Activity extends AppCompatActivity implements
        LocationSource, AMapLocationListener, View.OnClickListener {


    private AMap aMap;  // 地图
    private MapView mapView;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private AMapLocation privLocation;

    private double weight;
    private double distance;
    private double sub_distance;

    Chronometer mChronometer;//计时器+显示实时数据-公里、分/公里
    TextView tv_distance;
    TextView tv_speed;

    MyCountTimer myCountTimer;//倒计时工具
    TextView countTime;
    Button backCountTime;

    TextView tv_modeText; // 地图模式与跑步模式控件

    RelativeLayout rlMap; // 控制地图布局显示与否
    boolean mode = true; // true 跑步模式, false 地图模式

    PathRecord pathRecord; // PathRecord 类记录本次运行的所有路径

    TextView tvSportComplate;//开始、暂停、结束的button
    TextView tvSportPause;
    TextView tvSportContinue;

    LocationManager locationManager;//创建获取此时位置的管理器
    List<String> providerList;//check获取位置的权限

    SharedPreferences sp;//查看数据库
    SharedPreferences.Editor editor;//修改数据库


    MyDatabaseHelper myDatabaseHelper;//数据库
    SQLiteDatabase myDatabase;

    private DecimalFormat decimalFormat = new DecimalFormat("0.00");//数据格式-2位小数

    //参考助教画线部分变量：
    private PolylineOptions polylineOptions;
    private PathSmoothTool mpathSmoothTool = null;
    private List<LatLng> mSportLatLngs = new ArrayList<>(0);

    //暂停、继续停止加点的flag：
    boolean flag = true;


    /**
     * 需要进行检测的权限数组
     */
    protected String[] needPermissions = {
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };
    private static final int PERMISSON_REQUESTCODE = 0;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_sport_map);

        // 初始化地图组件
        mapView = findViewById(R.id.stepMap);
        mapView.onCreate(savedInstanceState);//


        //初始化地图和页面：
        initView();
        initMap();

        //倒计时：
        myCountTimer.start();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                //计时器
                mChronometer.setBase(SystemClock.elapsedRealtime());
                mChronometer.start();
                Log.i("TAG", "计时器开始工作");

                //        往P类中传开始时间：
                Date startDate = new Date();
                pathRecord.setStartTime(startDate.getTime());//开始时间-毫秒
                Log.i("TAG", "开始时间" + startDate.getTime() + "");
            }
        }, 3000);


    }

    private void initView() {
        // 点击时隐藏|显示地图
        tv_modeText = findViewById(R.id.tv_mode);
        tv_modeText.setOnClickListener(this);


        rlMap = findViewById(R.id.rlMap);
        rlMap.setVisibility(View.GONE); // 刚开始不现实地图

        tvSportComplate = findViewById(R.id.tv_sport_complate);//完成button
        tvSportPause = findViewById(R.id.tv_sport_pause);
        tvSportContinue = findViewById(R.id.tv_sport_continue);
        tvSportComplate.setOnClickListener(this);
        tvSportPause.setOnClickListener(this);
        tvSportContinue.setOnClickListener(this);

        //刚开始没有继续按钮
        tvSportContinue.setVisibility(View.INVISIBLE);

        mChronometer = findViewById(R.id.cm_passtime);//实时数据-----计时器
        tv_distance = findViewById(R.id.tvMileage);
        tv_speed = findViewById(R.id.tvSpeed);


        //倒计时
        countTime = findViewById(R.id.countTime);//倒计时
        backCountTime = findViewById(R.id.backCountTime);//倒计时的棕色背景
        myCountTimer = new MyCountTimer(3100, 1000, countTime, backCountTime);

        //初始化：
        pathRecord = new PathRecord();

        //数据库初始化：
        myDatabaseHelper = new MyDatabaseHelper(this);
        myDatabase = myDatabaseHelper.getWritableDatabase();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);//创建获取此时位置的管理器
        providerList = locationManager.getProviders(true);


        sp = this.getSharedPreferences("basic_info", Context.MODE_PRIVATE);//查看数据库
        editor = this.getSharedPreferences("basic_info", Context.MODE_PRIVATE).edit();//修改数据库

        weight = Double.parseDouble(sp.getString("weight", ""));

        //初始化轨迹线：
        initPolyline();
    }


    /**
     * 每个按钮的点击事件
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_sport_pause:
                mChronometer.stop();
                Log.i("TAG", "暂停键被点击");
                //继续按钮出现、暂停和完成消失
                tvSportContinue.setVisibility(View.VISIBLE);
                tvSportPause.setVisibility(View.INVISIBLE);
                tvSportComplate.setVisibility(View.INVISIBLE);
                flag=false;
                break;
            case R.id.tv_sport_continue:
                mChronometer.setBase(SystemClock.elapsedRealtime() - convertStrTimeToLong(mChronometer.getText().toString()));
                mChronometer.start();
                tvSportContinue.setVisibility(View.INVISIBLE);
                tvSportPause.setVisibility(View.VISIBLE);
                tvSportComplate.setVisibility(View.VISIBLE);
                flag=true;
                Log.i("TAG", "继续键被点击");
                break;
            case R.id.tv_sport_complate:

                //往PathRecord类里传结束时间、结束位置、卡路里、时长、距离、时速、配速：

                Date stopDate = new Date();
                DateFormat df_ymd = DateFormat.getDateInstance();//输出年-月-日格式,eg:2016年6月6日
                pathRecord.setDateTag(df_ymd.format(stopDate));//跑步日期--DateTag
                pathRecord.setEndTime(stopDate.getTime());//结束时间-毫秒-可以直接转成Date类
                pathRecord.setCalorie(weight * distance * 1.036 / 1000d);//卡路里---double
                Long duration = convertStrTimeToLong(mChronometer.getText().toString()) / 1000;//时长-秒-从计时器获得的
                Log.i("TAG", "计算的duration是：" + duration);
                pathRecord.setDuration(duration);//秒
                pathRecord.setDistance(distance);//距离-米
                pathRecord.setSpeed((distance / duration * 3.6));//存平均时速--double--公里/小时

                if (distance > 0) {
                    pathRecord.setDistribution(duration / 60d / distance * 1000d);//配速---double----分钟/公里
                    pathRecord.setStartpoint(pathRecord.getPathline().get(0));
                    pathRecord.setEndpoint(pathRecord.getPathline().get(pathRecord.getPathline().size()-1));//存起点和终点：
                }

                Log.i("TAG", "PathRecord类被创建");
                Log.i("TAG", "本次Distribution是：" + String.valueOf(pathRecord.getDistribution()));
                Log.i("TAG", "本次速度是：" + String.valueOf(pathRecord.getSpeed()));
                Log.i("TAG", "本次距离是：" + String.valueOf(pathRecord.getDistance()));
                Log.i("TAG", StepUtils.getLatLngPathLineString(pathRecord.getPathline()));

                if (distance > 0) {

                    //修改数据库里的总公里、时长、次数---首页显示
                    double totalDistance_new;//公里
                    int totalCount_new;//总跑步次数
                    double totalDuration_new;//分钟

                    if (sp.getString("totalDistance", "") != "" && sp.getString("totalCount", "") != "" &&
                            sp.getString("totalDuration", "") != "") {
                        totalDistance_new = Double.parseDouble(sp.getString("totalDistance", "")) + distance / 1000d;
                        totalCount_new = Integer.parseInt(sp.getString("totalCount", "")) + 1;
                        totalDuration_new = Double.parseDouble(sp.getString("totalDuration", "")) + duration / 60d;
                        Log.i("TAG", "1111");
                    } else {
                        totalDistance_new = distance / 1000d;
                        totalCount_new = 1;
                        totalDuration_new = duration / 60d;
                        Log.i("TAG", "2222");
                    }
                    Log.i("TAG", "本次时长为：" + duration + "; 新的总时长是：" + totalDuration_new);

                    editor.putString("totalDistance", decimalFormat.format(totalDistance_new));
                    editor.putString("totalDuration", decimalFormat.format(totalDuration_new));
                    editor.putString("totalCount", String.valueOf(totalCount_new));
                    editor.apply();

                    pathRecord.setId(totalCount_new);//ID-本次跑步次数
                    Log.i("TAG", "本次ID是：" + pathRecord.getId());

                    //往数据库传数据：
                    insertModel(pathRecord);
                    Log.i("TAG", "此处为insertModel语句");
                    Log.i("TAG", "DateTag是：" + df_ymd.format(stopDate));
                } else {
                    Toast.makeText(this, "距离为0，没有记录到跑步数据~", Toast.LENGTH_SHORT).show();
                }

                //停止计时器
                mChronometer.stop();


                //前往结束页面-----并传值：
                Intent intent = new Intent(this, SportResult_Activity.class);
                intent.putExtra("SportResult", pathRecord);
                startActivity(intent);
                finish();
                break;


            case R.id.tv_mode:
                if (mode) {
                    //mode=true, 位于跑步模式，点击 = 想改变模式到地图模式；
                    rlMap.setVisibility(View.VISIBLE);
                    tv_modeText.setText("跑步模式");
                    //改变文字view左边的图片；
                    UIHelperUtil.setLeftDrawable(tv_modeText, ContextCompat.getDrawable(this, R.mipmap.run_mode));
                    mode = false;
                } else {
                    rlMap.setVisibility(View.GONE);
                    tv_modeText.setText("地图模式");
                    UIHelperUtil.setLeftDrawable(tv_modeText, ContextCompat.getDrawable(this, R.mipmap.map_mode));
                    mode = true;
                }
                break;
            default:
                break;
        }
    }

    /**
     * 地图开始
     */
    private void initMap() {
        if (aMap == null) {
            aMap = mapView.getMap();
            // 缩放级别
            aMap.moveCamera(CameraUpdateFactory.zoomTo(20));
            aMap.getUiSettings().setZoomControlsEnabled(false);
            aMap.getUiSettings().setCompassEnabled(true);// 设置显示指南针
            setUpMap();
        }
    }


    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        // 设置定位回调
        // 自定义系统定位小蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.strokeColor(Color.TRANSPARENT); // 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0)); // 设置圆形的填充颜色
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
    }


    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(this);
            // 初始化定位参数
            mLocationOption = new AMapLocationClientOption();
            // 设置定位监听 属性
            // 可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
            // mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationClient.setLocationListener(this);
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位间隔 5s
            mLocationOption.setInterval(5000);
            mLocationClient.setLocationOption(mLocationOption);
            // 设置定位监听
            mLocationClient.startLocation();
        }
    }

    // 停止定位
    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;

    }

    // 定位成功后 会 每 5秒钟 回调函数
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            // code 12 is no premission
//            Log.i("TAG", aMapLocation.getErrorCode() + "");
            if (aMapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(aMapLocation);// 显示系统圆点
                Log.i("TAG", "进入位置检测函数，flag是"+flag);
                // 每5秒打印一次 经度纬度 地MyLocationStyle 址 城市
                updateLocationLine(aMapLocation);
                if(flag ) {
                    Log.i("TAG", "进入flag-记录经纬度点的内部" );
                    pathRecord.addpoint(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude()));
                    distance += sub_distance;
                    Log.i("TAG", "添加位置" + aMapLocation.getLatitude() + "   :   " + aMapLocation.getLongitude());
                    // 根据这些经纬度可以在地图上每五秒画点线，最终画出轨迹
                }
//                  drawLines(aMapLocation);//一边定位一边连线
                Log.i("TAG", "111111111" );
                Log.i("TAG", "2222222" );
                //更新组件显示：
                double time = convertStrTimeToLong(mChronometer.getText().toString()) / 1000;//运动计时秒数
                Log.i("TAG", "更新组件显示----1" );
                tv_speed.setText(decimalFormat.format((time / 60d) / (distance / 1000d)));//配速：分/公里
                Log.i("TAG", "更新组件显示----2"+distance );
                tv_distance.setText(decimalFormat.format(distance / 1000d));
                Log.i("TAG", "更新组件显示----3" );
                privLocation = aMapLocation;
                Log.i("TAG", "更新组件显示----4" +privLocation);
            }
        } else {
            String errText = "定位失败" + aMapLocation.getErrorCode() + ":" + aMapLocation.getErrorInfo();
            Log.e("AmapError", errText);
        }
    }

    /**
     * 绘制轨迹
     */

//    public void drawLines(AMapLocation curLocation) {
//
//        if (null == privLocation) {
//            return;
//        }
//        PolylineOptions options = new PolylineOptions();
//        //上一个点的经纬度
//        options.add(new LatLng(privLocation.getLatitude(), privLocation.getLongitude()));
//        //当前的经纬度
//        options.add(new LatLng(curLocation.getLatitude(), curLocation.getLongitude()));
//        options.width(20).geodesic(true).color(0xAAFF0000);
//        aMap.addPolyline(options);
//        //距离的计算
//        distance = AMapUtils.calculateLineDistance(new LatLng(privLocation.getLatitude(),
//                privLocation.getLongitude()), new LatLng(curLocation.getLatitude(),
//                curLocation.getLongitude()));
//
//    }


//    参考助教----平滑轨迹：
    private void initPolyline() {
        polylineOptions = new PolylineOptions();
        polylineOptions.color(getResources().getColor(R.color.colorAccent));
        polylineOptions.width(20f);
        polylineOptions.useGradient(true);

        mpathSmoothTool = new PathSmoothTool();
        mpathSmoothTool.setIntensity(4);
    }

    private void updateLocationLine(AMapLocation aMapLocation) {
        Log.i("TAG", "333333333333---进入加线函数" );
        mSportLatLngs.clear();
        //轨迹平滑优化
        Log.i("TAG", "进入加线函数----11111111" );
        mSportLatLngs = new ArrayList<>(mpathSmoothTool.pathOptimize(pathRecord.getPathline()));
        Log.i("TAG", "进入加线函数----22222222" );
        if (!mSportLatLngs.isEmpty()) {
            Log.i("TAG", "进入加线函数----333333333333333" );
            polylineOptions.add(mSportLatLngs.get(mSportLatLngs.size() - 1));
            Log.i("TAG", "进入加线函数----4" );
            if (mListener != null)
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude()), 18));
            Log.i("TAG", "进入加线函数----5" );
        }
        aMap.addPolyline(polylineOptions);
        Log.i("TAG", "进入加线函数----6" );

        if (null == privLocation) {
            return;
        }
        sub_distance = AMapUtils.calculateLineDistance(new LatLng(privLocation.getLatitude(),
                privLocation.getLongitude()), new LatLng(aMapLocation.getLatitude(),
                aMapLocation.getLongitude()));
        Log.i("TAG", "进入加线函数----7"+sub_distance);
    }


    /**
     * 向数据库中传递数据
     */
    private void insertModel(PathRecord pathRecord1) {
        myDatabaseHelper = new MyDatabaseHelper(this);
        myDatabase = myDatabaseHelper.getWritableDatabase();
        Log.i("TAG", "数据库stepDemoData.db被创建？");

        //Add:
        ContentValues contentValues = new ContentValues();

        //共12个属性：
        contentValues.put("id", pathRecord1.getId());
        contentValues.put("startTime", pathRecord1.getStartTime());
        contentValues.put("startPoint", StepUtils.amapLocationToString(pathRecord1.getStartpoint()));//位置点-经纬度用,分隔
        contentValues.put("endTime", pathRecord1.getEndTime());
        contentValues.put("endPoint", StepUtils.amapLocationToString(pathRecord1.getEndpoint()));
        contentValues.put("duration", pathRecord1.getDuration());
        contentValues.put("distance", pathRecord1.getDistance());
        contentValues.put("calories", pathRecord1.getCalorie());
        contentValues.put("speed", pathRecord1.getSpeed());
        contentValues.put("distribution", pathRecord1.getDistribution());
        contentValues.put("pathLinePoints", StepUtils.getLatLngPathLineString(pathRecord1.getPathline()));//位置点之间用;分隔、经纬度用,分隔
        contentValues.put("dateTag", pathRecord1.getDateTag());

        Log.i("TAG", "contentValues赋值完毕");

        long rowNumber = myDatabase.insert("sportData", null, contentValues);
        Log.i("TAG", "数据被插入");

        if (rowNumber != -1) {
            Toast.makeText(this, "运动数据已保存~", Toast.LENGTH_SHORT).show();//注意table名称和Database名称不一样

        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        setNeedCheckPermission();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        deactivate();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 退出销毁Map
        mapView.onDestroy();
    }


    /**
     * 判断是否需要检测，防止不停的弹框
     */
    private boolean isNeedCheck = true;

    /**
     * 检查手机是否开启定位权限，没开启会提醒开启
     */
    private void setNeedCheckPermission() {

        if (Build.VERSION.SDK_INT >= 23
                && getApplicationInfo().targetSdkVersion >= 23) {
            if (isNeedCheck) {
                checkPermissions(needPermissions);
            }
        }
    }

    /**
     * 检查定位权限
     *
     * @param permissions
     * @since 2.5.0
     */
    private void checkPermissions(String... permissions) {
        try {
            if (Build.VERSION.SDK_INT >= 23
                    && getApplicationInfo().targetSdkVersion >= 23) {
                List<String> needRequestPermissonList = findDeniedPermissions(permissions);
                if (null != needRequestPermissonList
                        && needRequestPermissonList.size() > 0) {
                    String[] array = needRequestPermissonList.toArray(new String[needRequestPermissonList.size()]);
                    Method method = getClass().getMethod("requestPermissions", new Class[]{String[].class,
                            int.class});

                    method.invoke(this, array, PERMISSON_REQUESTCODE);
                }
            }
        } catch (Throwable e) {
        }
    }

    /**
     * 获取权限集中需要申请权限的列表
     *
     * @param permissions
     * @return
     * @since 2.5.0
     */
    private List<String> findDeniedPermissions(String[] permissions) {
        List<String> needRequestPermissonList = new ArrayList<String>();
        if (Build.VERSION.SDK_INT >= 23
                && getApplicationInfo().targetSdkVersion >= 23) {
            try {
                for (String perm : permissions) {
                    Method checkSelfMethod = getClass().getMethod("checkSelfPermission", String.class);
                    Method shouldShowRequestPermissionRationaleMethod = getClass().getMethod("shouldShowRequestPermissionRationale",
                            String.class);
                    if ((Integer) checkSelfMethod.invoke(this, perm) != PackageManager.PERMISSION_GRANTED
                            || (Boolean) shouldShowRequestPermissionRationaleMethod.invoke(this, perm)) {
                        needRequestPermissonList.add(perm);
                    }
                }
            } catch (Throwable e) {

            }
        }
        return needRequestPermissonList;
    }

    //计时器工具-----返回计数值的毫秒数
    protected long convertStrTimeToLong(String strTime) {
        // TODO Auto-generated method stub
        String[] timeArry = strTime.split(":");
        long longTime = 0;
        if (timeArry.length == 2) {//如果时间是MM:SS格式
            longTime = Integer.parseInt(timeArry[0]) * 1000 * 60 + Integer.parseInt(timeArry[1]) * 1000;
        } else if (timeArry.length == 3) {//如果时间是HH:MM:SS格式
            longTime = Integer.parseInt(timeArry[0]) * 1000 * 60 * 60 + Integer.parseInt(timeArry[1])
                    * 1000 * 60 + Integer.parseInt(timeArry[0]) * 1000;
        }
        return longTime;
    }
}
