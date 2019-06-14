package com.liuxiaojuan.happpyrun.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.liuxiaojuan.happpyrun.BasicSetting_Activity;
import com.liuxiaojuan.happpyrun.PlanSetting_Activity;
import com.liuxiaojuan.happpyrun.R;
import com.liuxiaojuan.happpyrun.TuLinTalk_Activity;
import com.liuxiaojuan.happpyrun.WeekRecord_Activity;
import com.liuxiaojuan.happpyrun.utils.ConstrantUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 个人信息页面
 * 当用户首次点击跑步的时侯，需要检测SharedPreference 里面是否存储了 运动标语， 身高、体重，并计算BMI，
 * 没有存储这些值的时侯，不能让他跳转到跑步页面
 * <p>
 * 点击头像区域，设置头像
 * 点击头像左边区域，设置运动标语， 身高，体重 ， 保存SharedPreference 里面后，计算BMI 值显示到界面
 * <p>
 * 点击跑步设置，一周记录，悦聊 分别跳转到对应页面
 */

public class Account_Fragment extends Fragment implements View.OnClickListener {

    // 三个Layout
    LinearLayout planSettingLayout;
    LinearLayout weekRecordLayout;
    LinearLayout talkLayout;
    ImageView man_pic;
    TextView editBasicinfo;
    TextView height_present;
    TextView weight_present;
    TextView slogan_present;
    TextView BMI_present;
    TextView level_present;
    String filePath;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container,
                false);


        initView(view);
        return view;
    }

    private void initView(View view) {
        planSettingLayout = view.findViewById(R.id.plan_setting_id);
        weekRecordLayout = view.findViewById(R.id.week_record_id);
        talkLayout = view.findViewById(R.id.talk_id);
        man_pic = view.findViewById(R.id.man_pic);
        editBasicinfo = view.findViewById(R.id.edit_basicinfo);
        height_present = view.findViewById(R.id.height_present);
        weight_present = view.findViewById(R.id.weight_present);
        slogan_present = view.findViewById(R.id.slogan_present);
        BMI_present = view.findViewById(R.id.BMI_present);
        level_present=view.findViewById(R.id.level_present) ;

        // 设置对应的监听事件
        planSettingLayout.setOnClickListener(this);
        weekRecordLayout.setOnClickListener(this);
        talkLayout.setOnClickListener(this);
        man_pic.setOnClickListener(this);
        editBasicinfo.setOnClickListener(this);

        SharedPreferences sp = getActivity().getSharedPreferences("basic_info", Context.MODE_PRIVATE);

        if (sp.getString("height", "") != "" && sp.getString("weight", "") != "" &&
                sp.getString("slogan", "") != "" && sp.getString("BMI", "") != "" ) {
            height_present.setText(sp.getString("height", "") + "cm");
            weight_present.setText(sp.getString("weight", "") + "kg");
            slogan_present.setText("“" + sp.getString("slogan", "") + "”");
            BMI_present.setText(sp.getString("BMI", "") + "BMI");
        }

        if(sp.getString("totalCount", "")!="") {
            if(Integer.parseInt(sp.getString("totalCount", "")) >5){
                level_present.setText("运动进阶者--- 乐跑日记");
            }
            else if(Integer.parseInt(sp.getString("totalCount", ""))<=15){
                level_present.setText("运动达人--- 乐跑日记");
            }
            if(Integer.parseInt(sp.getString("totalCount", ""))>=30){
                level_present.setText("运动王者--- 乐跑日记");
            }
        }

        if(sp.getString("headPath","")!=""){
            Log.i("TAG", "检测的头像存储的地址为"+sp.getString("headPath",""));
            File file = new File(sp.getString("headPath",""));
            if (file.exists()) {
                Log.i("TAG", "头像文件创建存在");
                Bitmap photo = BitmapFactory.decodeFile(sp.getString("headPath",""));
                man_pic.setImageBitmap(photo);
            }
        }
    }

//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        //更新值
//        SharedPreferences sp = getActivity().getSharedPreferences("basic_info", Context.MODE_PRIVATE);
//        if(sp.getString("height", "")!="" && sp.getString("weight", "")!="" &&
//                sp.getString("slogan", "")!="" && sp.getString("BMI", "")!="") {
//            height_present.setText(sp.getString("height", "") + "cm");
//            weight_present.setText(sp.getString("weight", "") + "kg");
//            slogan_present.setText("“" + sp.getString("slogan", "") + "”");
//            BMI_present.setText(sp.getString("BMI", "") + "BMI");
//        }
//    }

    @Override
    public void onStart() {
        super.onStart();
        //更新值
        SharedPreferences sp = getActivity().getSharedPreferences("basic_info", Context.MODE_PRIVATE);
        if (sp.getString("height", "") != "" && sp.getString("weight", "") != "" &&
                sp.getString("slogan", "") != "" && sp.getString("BMI", "") != "") {
            height_present.setText(sp.getString("height", "") + "cm");
            weight_present.setText(sp.getString("weight", "") + "kg");
            slogan_present.setText("“" + sp.getString("slogan", "") + "”");
            BMI_present.setText(sp.getString("BMI", "") + "BMI");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.man_pic:
                setAvatar();
                break;
            case R.id.edit_basicinfo:
                Intent goBasicSet = new Intent(getContext(), BasicSetting_Activity.class);
                startActivity(goBasicSet);
                break;
            case R.id.plan_setting_id:
                Intent goPlan = new Intent(getContext(), PlanSetting_Activity.class);
                startActivity(goPlan);
                break;
            case R.id.week_record_id:
                Intent goWeek = new Intent(getContext(), WeekRecord_Activity.class);
                startActivity(goWeek);
                break;
            case R.id.talk_id:
                Intent goTalk = new Intent(getContext(), TuLinTalk_Activity.class);
                startActivity(goTalk);
                break;
            default:
                break;
        }
    }

    //上传头像
    private void setAvatar() {
        Intent intent1 = new Intent(Intent.ACTION_PICK);
        intent1.setType("image/*");
        intent1.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent1.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent2.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent2.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        File file = new File(Environment.getExternalStorageDirectory(),   ConstrantUtils.HEAD_PICTURE);
//        cameraPath = file.getAbsolutePath();
        Uri uri;
        if (Build.VERSION.SDK_INT >= 24) {

            uri = FileProvider.getUriForFile(getContext(), "com.liuxiaojuan.happpyrun.fileProvider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        intent2.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent2.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        Intent chooser = Intent.createChooser(intent1, "选择头像");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{intent2});
        startActivityForResult(chooser, 101);// --------------->101
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("dsdsdsdsdsdddddddddddddd");

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            System.out.println(requestCode + "   ____dsdsdsdsdsdddddddddddddd");
            if (requestCode == 101) {// 获得未裁剪的照片 --------------->101
                if (data ==  null) { // 从相机传来的
                  //  crop(Uri.fromFile(ConstrantUtils.bgFile));// 裁剪
                    File bgPath = ConstrantUtils.bgFile;
                    crop(bgPath);
                }else{
                    crop(data.getData()); // 从相册传来的
                }
            }
            if (requestCode == 102) {// 裁剪点击确定后执行 --------------->102

                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap photo = extras.getParcelable("data");
                    saveMyBitmap(photo);
                    File bgPath = ConstrantUtils.bgFile;
                    if (Build.VERSION.SDK_INT >= 24) {
                        filePath = FileProvider.getUriForFile(getContext(), "com.liuxiaojuan.happpyrun.fileProvider", bgPath).getPath();
                    } else {
                        filePath = Uri.fromFile(bgPath).getPath();
                    }
                    SharedPreferences.Editor editor = getContext().getSharedPreferences("basic_info", Context.MODE_PRIVATE).edit();
                    Log.i("TAG", "头像存储的地址为"+filePath.replace("/root_path",""));
                    editor.putString("headPath", filePath.replace("/root_path",""));
                    editor.apply();
                    man_pic.setImageBitmap(photo);
                }
            }
        }
    }

    /**
     * 调用安卓的图片剪裁程序对用户选择的头像进行剪裁
     * <p>
     * //         * @param filePath
     * 用户选取的头像在SD上的地址
     */
    private void crop(Uri uri) {
        // 隐式intent
        Intent intent = new Intent("com.android.camera.action.CROP");

        // 设置剪裁数据 150*150
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", true);
        intent.putExtra("return-data", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        startActivityForResult(intent, 102);// --------------->102
    }

    private void crop(File file) { // 相机

        // 隐式intent
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        // 设置剪裁数据 150*150
        Uri uri = FileProvider.getUriForFile(getContext(), "com.liuxiaojuan.happpyrun.fileProvider", file);
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", true);
        intent.putExtra("return-data", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        startActivityForResult(intent, 102);// --------------->102
    }

    public void saveMyBitmap(Bitmap mBitmap)  {
        File f = new File(Environment.getExternalStorageDirectory(),ConstrantUtils.HEAD_PICTURE);
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
