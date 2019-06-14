package com.liuxiaojuan.happpyrun.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.liuxiaojuan.happpyrun.R;
import com.liuxiaojuan.happpyrun.utils.ListData;

import java.util.List;

public class TextAdapter extends BaseAdapter {
    private List<ListData> lists;
    private Context mContext;
    private RelativeLayout layout;

    public TextAdapter(List<ListData> lists,Context mContext){
        this.lists = lists;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return lists.size();
    }

    @Override
    public Object getItem(int i) {
        return lists.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(mContext );
        if(lists.get(i).getFlag() == ListData.RECIEVE ){
            layout = (RelativeLayout) inflater.inflate(R.layout.tulintalk_left,null);
        }
        if(lists.get(i).getFlag() == ListData.SEND ){
            layout = (RelativeLayout) inflater.inflate(R.layout.tulintalk_right ,null);
        }
        TextView tv = (TextView)layout.findViewById(R.id.tv);
        tv.setText(lists.get(i).getContent());
        return layout;
    }
}
