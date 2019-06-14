package com.liuxiaojuan.happpyrun;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Pattern;

public class BasicSetting_Activity extends Activity {
//    private LayoutInflater layoutInflater;
    private EditText height_edit;
    private EditText weight_edit;
    private EditText slogan_edit;
    private Button basicSaveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_setting);
        height_edit = (EditText)findViewById(R.id.height_edit);
        weight_edit = (EditText) findViewById(R.id.weight_edit);
        slogan_edit = (EditText) findViewById(R.id.slogan_edit);
        basicSaveBtn = (Button) findViewById(R.id.basicInfo_save_button);
        Toast.makeText( getApplicationContext(),"身高、体重必须填数字,否则BMI将存为空值，无法开始跑步~", Toast.LENGTH_LONG).show();
        basicSaveBtn .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBasicInfo();
                finish();
            }
        });
    }

    private void saveBasicInfo() {
        String BMI="";
        if(isNumeric(height_edit.getText().toString())==true && isNumeric(weight_edit.getText().toString())==true ){
            BMI =String.format("%.2f", Double.parseDouble(weight_edit.getText().toString())/Math.pow(Double.parseDouble(height_edit.getText().toString())/100,2));
        }
        SharedPreferences.Editor editor = this.getSharedPreferences("basic_info", Context.MODE_PRIVATE).edit();
        editor.putString("height",height_edit.getText().toString());
        editor.putString("weight",weight_edit.getText().toString());
        editor.putString("slogan",slogan_edit.getText().toString());
        editor.putString("BMI",BMI);
        editor.apply();
    }


    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[1-9]{1,}[.]?[0-9]{1,}");
        return pattern.matcher(str).matches();
    }

}
