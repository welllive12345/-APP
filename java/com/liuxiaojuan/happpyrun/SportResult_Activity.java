package com.liuxiaojuan.happpyrun;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.PolylineOptions;
import com.liuxiaojuan.happpyrun.bean.PathRecord;
import com.liuxiaojuan.happpyrun.utils.PathSmoothTool;
import com.liuxiaojuan.happpyrun.utils.ScreenShotHelper;
import com.liuxiaojuan.happpyrun.utils.StepUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 运动结果页面，点击完成时跳到该页面
 *  评分运行规则：依次判断 距离大于0 ★；运动时间大于40分钟 ★★；速度在3~6km/h之间 ★★★
 */
public class SportResult_Activity extends AppCompatActivity implements LocationSource,  View.OnClickListener, AMap.OnMapScreenShotListener {

    TextView textView;
    private PathRecord pathRecord;
    // 获取轨迹坐标点
    List<LatLng> mOriginList;
    // 地图
    private AMap aMap;
    private MapView mapView;
    ImageView first_star;
    ImageView second_star;
    ImageView third_star;
    TextView result_distance;
    TextView result_duration;
    TextView result_calories;
    private DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private DecimalFormat intFormat = new DecimalFormat("#");

    // 整个容器最大的布局
    private ViewGroup mViewGroupContainer;
    // 除地图外的布局
    private View mScreemShotView;


    //画地图：
    private PolylineOptions polylineOptions;
    private PathSmoothTool mpathSmoothTool = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sport_result);

        // 初始化地图组件
        mapView =  findViewById(R.id.resultMap);
        mapView.onCreate(savedInstanceState);

        initView();
        initMap();
    }


    private void initView() {

        // 接收跳转到这个界面的pathRecord值
        if (getIntent().hasExtra("SportResult")) {
            pathRecord = getIntent().getParcelableExtra("SportResult");
        }

        mOriginList = pathRecord .getPathline();

        //分享按钮
        textView = findViewById(R.id.tv_shared);
        textView.setOnClickListener(this);

        //截图容器：
        mViewGroupContainer = findViewById(R.id.root_pic_id);
        mScreemShotView = findViewById(R.id.top_id);

        //数值和星星：
        first_star = (ImageView )findViewById(R.id.star_first  ) ;
        second_star = (ImageView )findViewById(R.id.star_second   ) ;
        third_star = (ImageView )findViewById(R.id.star_third ) ;
        result_distance = (TextView)findViewById(R.id.tv_result_distance ) ;
        result_duration = (TextView)findViewById(R.id.tv_result_time) ;
        result_calories = (TextView)findViewById(R.id.tv_result_calorie  ) ;

        //赋值：
        result_distance .setText(decimalFormat.format(pathRecord .getDistance()/1000d));
        result_duration. setText(StepUtils.formatseconds(pathRecord .getDuration() ) );
        result_calories.setText(intFormat.format(pathRecord .getCalorie() ) );

        //点亮星星：
        if(pathRecord .getDistance()>0){
            first_star .setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.big_star ));
            if(pathRecord .getDuration() / 60 > 40){
                second_star .setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.big_star ));
                if(pathRecord .getSpeed() >= 3 && pathRecord .getSpeed() <= 6 ){
                    third_star .setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.big_star ));
                }
            }
        }

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
        List<LatLng> pathoptimizeList = new ArrayList<>(mpathSmoothTool.pathOptimize(mOriginList));
        Log.i("TAG","提取到的路径信息是:"+StepUtils.getLatLngPathLineString(mOriginList));

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
    }

    /**
     * 根据自定义内容获取缩放bounds
     */
    private LatLngBounds getLatLngBounds(List<LatLng> pointList) {
        LatLngBounds.Builder b = LatLngBounds.builder();
        if(pointList !=null) {
            for (int i = 0; i < pointList.size(); i++) {
                LatLng p = pointList.get(i);
                b.include(p);
            }
        }
        return b.build();
    }
    

    /**
     * 下面代码为点击分享朋友圈
     * 1 .首先根据页面布局，选择特定区域的View的id (root view id) , 转bitmap
     * 2. 压缩图片，以免图片过大，内存溢出
     * 3. 把图片保存成手机上的文件，得到Uri 路径，
     * 4. 利用安卓自带的应用程序之间的分享功能，进行分享到朋友圈，微信等
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_shared:

                // 地图截屏
                aMap.getMapScreenShot(this);
                // Bitmap bitmap = getDiskBitmap(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "test1.png");
                // 得到地图截屏后的结果文件
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "test1.png");
                if (file != null && file.exists() && file.isFile()) {
                    //由文件得到uri
                    Uri imageUri;
                    if (Build.VERSION.SDK_INT >= 24) {
                        imageUri = FileProvider.getUriForFile(getApplicationContext(), "com.liuxiaojuan.happpyrun" + ".fileProvider", file);
                    } else {
                        imageUri = Uri.fromFile(file);
                    }
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                    shareIntent.setType("image/*");
                    startActivity(Intent.createChooser(shareIntent, "分享图片"));
                }
                break;
            default:
                break;
        }
    }


    public static Bitmap getMapAndView(Bitmap bitmap, ViewGroup viewGroup, MapView mapView, View... views) {
        int width = viewGroup.getWidth();
        int height = viewGroup.getHeight();
        final Bitmap screenBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(screenBitmap);
        canvas.drawBitmap(bitmap, mapView.getLeft(), mapView.getTop(), null);
        for (View view : views) {
            view.setDrawingCacheEnabled(true);
            canvas.drawBitmap(view.getDrawingCache(), view.getLeft(), view.getTop(), null);
        }
        return screenBitmap;
    }

    /**
     * 将布局转化为bitmap这里传入的是你要截的布局的根View
     */
    public Bitmap getBitmapByView(View view) {
        view.destroyDrawingCache();
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        return view.getDrawingCache(true);
    }

    /**
     * 压缩图片
     */

    private Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 10, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 400) {  //循环判断如果压缩后图片是否大于400kb,大于继续压缩（这里可以设置大些）
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    /**
     * 把bitmap转化为file
     */
    public File bitMap2File(Bitmap bitmap) {
        String path = "";
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            path = Environment.getExternalStorageDirectory() + File.separator;//保存到sd根目录下
        }

        File f = new File(path, "share" + ".jpg");
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            bitmap.recycle();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return f;
        }
    }

    // 地图截屏回调函数
    @Override
    public void onMapScreenShot(Bitmap bitmap) {
        ScreenShotHelper.saveScreenShot(bitmap, mViewGroupContainer, mapView, mScreemShotView);
        Toast.makeText(getApplicationContext(), "SD卡下查看截图后的文件", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onMapScreenShot(Bitmap bitmap, int i) {

    }

    private Bitmap getDiskBitmap(String pathString) {
        Bitmap bitmap = null;
        try {
            File file = new File(pathString);
            if (file.exists()) {
                bitmap = BitmapFactory.decodeFile(pathString);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return bitmap;
    }
}
