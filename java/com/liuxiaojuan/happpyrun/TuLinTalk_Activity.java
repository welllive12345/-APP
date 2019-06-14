package com.liuxiaojuan.happpyrun;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.liuxiaojuan.happpyrun.adapter.TextAdapter;
import com.liuxiaojuan.happpyrun.utils.HttpData;
import com.liuxiaojuan.happpyrun.utils.ListData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 *   调用图灵API 和 图灵机器聊天, 参考ppt 备注
 *   制作类似QQ对话框的界面
 */
public class TuLinTalk_Activity extends Activity implements HttpGetDataListener, View.OnClickListener {
    //异步请求对象：
    private List<ListData> lists;
    private ListView lv;
    private EditText sendtext;
    private Button sendBtn;
    private String content_str;
    private TextAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tulin_talk);
        initView();
    }

    private void initView(){
        lv = (ListView) findViewById(R.id.lv);
        sendtext = (EditText) findViewById(R.id.sendText );
        sendBtn = (Button) findViewById(R.id.sendBtn );
        lists = new ArrayList<ListData>();
        sendBtn.setOnClickListener(this);
        adapter = new TextAdapter(lists,this);
        lv.setAdapter(adapter);
        ListData listData;
        listData = new ListData("hi~我是小A，欢迎来到乐跑圈，一起加油进阶吧！",ListData.RECIEVE );
        lists.add(listData) ;
    }

    @Override
    public void getDataUrl(String data){
        parseText(data);
    }


   //返回图灵机器人的数据：
    public void parseText(String str){
        try{
            JSONObject jb = new JSONObject(str);
            ListData listData;
            listData = new ListData(jb.getString("text"),ListData.RECIEVE );
            lists.add(listData);
            //每次返回数据都要重新适配：
            adapter.notifyDataSetChanged();
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        //获取自己输入内容
        content_str = sendtext.getText().toString();
        sendtext.setText("");
        ListData listData;
        listData = new ListData(content_str,ListData.SEND);
        //将自己输入的内容导进去
        lists.add(listData);
        //每次返回数据都要重新适配：
        adapter.notifyDataSetChanged();
        try {
            HttpData httpData = (HttpData) new HttpData(
                    "http://www.tuling123.com/openapi/api?key=d1eadd767c3e473092349bef87bbdbff&info=" + URLEncoder.encode(content_str, "UTF-8"), this).execute();
        }
        catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }
        }
}
