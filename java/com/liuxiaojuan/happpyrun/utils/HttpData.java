package com.liuxiaojuan.happpyrun.utils;

import android.os.AsyncTask;

import com.liuxiaojuan.happpyrun.HttpGetDataListener;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

   //机器人-获取异步数据：

public class HttpData extends AsyncTask<String, Void, String> {

    private HttpClient mHttpClient;
    private HttpGet mHttpGet;
    private HttpResponse mHttpResponse;
    private HttpEntity mHttpEntity;
    private InputStream in;
    private HttpGetDataListener listener;

    private String url;

    public HttpData(String url, HttpGetDataListener listener){
        this.url = url;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String...params){
        //主要执行任务的方法

        //参数含义是可以输入多个String类型

        try{
            mHttpClient = new DefaultHttpClient() ;
            mHttpGet = new HttpGet(url);
            mHttpResponse = mHttpClient .execute(mHttpGet);
            mHttpEntity = mHttpResponse .getEntity() ;
            in  = mHttpEntity .getContent() ;

            //读字符到缓存，缓存满了到内存
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = null;

            //可更改的字符串
            StringBuffer sb = new StringBuffer() ;
            while((line = br.readLine())!=null){
                sb.append(line);
            }
            return sb.toString();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    @Override

    //=任务执行结果作为此方法参数返回
    protected void onPostExecute(String result){
        super.onPostExecute(result);
        listener.getDataUrl(result);
    }
}

