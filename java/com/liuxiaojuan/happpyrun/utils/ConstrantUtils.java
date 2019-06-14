package com.liuxiaojuan.happpyrun.utils;

import android.os.Environment;

import java.io.File;

/**
 *  保存 app 的 一些常量
 */
public class ConstrantUtils {

    public String APP_NAME = "乐跑圈";
    public String AUTHOR = "xxx";


    public static final String HEAD_PICTURE = "head_portrait.jpg";
    //存放头像的File路径
    public static File bgFile = new File(Environment.getExternalStorageDirectory(),HEAD_PICTURE);
}
