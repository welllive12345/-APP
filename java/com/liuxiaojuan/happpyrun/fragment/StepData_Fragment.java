package com.liuxiaojuan.happpyrun.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.CalendarView;
import com.liuxiaojuan.happpyrun.R;
import com.liuxiaojuan.happpyrun.SportRecordDetails_Activity;
import com.liuxiaojuan.happpyrun.adapter.SportCalendarAdapter;
import com.liuxiaojuan.happpyrun.bean.PathRecord;
import com.liuxiaojuan.happpyrun.calendarview.custom.FullyLinearLayoutManager;
import com.liuxiaojuan.happpyrun.utils.MyDatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.liuxiaojuan.happpyrun.SportRecordDetails_Activity.SPORT_DATA;

/**
 * sport data  page
 * 1. 根据日期 查询数据库， 在日历上标记，下方显示 日历某一天的跑步记录
 * 2. 点击某一条记录，跳转到跑步详情页面
 */
public class StepData_Fragment extends Fragment {


    TextView mTextYear;
    TextView mTextMongthDay;
    TextView mTextLunar;

    TextView mTextCurrentDay;

    // 日历控件
    CalendarView mCalendarView;
    RecyclerView mRecycleView;


    private SportCalendarAdapter sportCalendarAdapter;


    //"跑步成绩"----线性布局
    LinearLayout sport_record_listLayout;

    private int mYear;
    private List<PathRecord> sportList = new ArrayList<>(0);

    //创建数据库：
    MyDatabaseHelper myDatabaseHelper;
    SQLiteDatabase myDatabase;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stepdata, container,
                false);

        initView(view);
        return view;
    }

    private void initView(View view) {
        mCalendarView = view.findViewById(R.id.calendarView);
        mYear = mCalendarView.getCurYear();

        mTextYear = view.findViewById(R.id.tv_year);
        mTextYear.setText(String.valueOf(mYear));

        String monthAndDay = mCalendarView.getCurMonth() + "月" + mCalendarView.getCurDay() + "日";
        mTextMongthDay = view.findViewById(R.id.tv_month_day);
        mTextMongthDay.setText(monthAndDay);

        mTextLunar = view.findViewById(R.id.tv_lunar);
        mTextLunar.setText("今日");

        mTextCurrentDay = view.findViewById(R.id.tv_current_day);
        mTextCurrentDay.setText(String.valueOf(mCalendarView.getCurDay()));

        sport_record_listLayout = view.findViewById(R.id.sport_record_list);

        mCalendarView.setWeekStarWithSun();

        //空出来等着插入Adapter的地方
        mRecycleView = view.findViewById(R.id.recyclerView);

        mRecycleView.setLayoutManager(new FullyLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });

        mRecycleView.addItemDecoration(new SpaceItemDecoration(getResources().getDimensionPixelSize(R.dimen.line)));

        //Adapter:
        sportCalendarAdapter = new SportCalendarAdapter(R.layout.item_sport_calendar, sportList);
        mRecycleView.setAdapter(sportCalendarAdapter);

        //创建数据库：
        myDatabaseHelper = new MyDatabaseHelper(getContext());
        myDatabase = myDatabaseHelper.getWritableDatabase();

        // 加载数据
        loadSportData();

        // 每一条跑步记录的点击事件----传值
        sportCalendarAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Intent intent = new Intent();
                intent.setClass(getContext(), SportRecordDetails_Activity.class);
                intent.putExtra(SPORT_DATA, sportList.get(position));
                startActivity(intent);
            }
        });
        // 日历点击事件
        mCalendarView.setOnCalendarSelectListener(new CalendarView.OnCalendarSelectListener() {

            @Override
            public void onCalendarOutOfRange(Calendar calendar) {

            }

            @Override
            public void onCalendarSelect(Calendar calendar, boolean isClick) {
                // 点击时加载数据
                setCalendarSportData();
                setRecycleViewSportData(calendar.getYear(), calendar.getMonth(), calendar.getDay());
            }
        });

    }


    /**
     * 查询数据库里当天的跑步记录
     * 1. 查询所有跑步数据，设置日历标记
     * 2  根据年月日参数，查询某一天的数据，设置recycleview 的条目
     */
    private void loadSportData() {

        setCalendarSportData();
        setRecycleViewSportData(mCalendarView.getCurYear(), mCalendarView.getCurMonth(), mCalendarView.getCurDay());
    }

    public void setCalendarSportData() {
        // 给日历做有记录的标记, 需要查询出有跑步记录的年月日
        Map<String, Calendar> map = new HashMap<>();

        //SQL数据库方法：
        String[] list = checkWholeDatabase();//所有跑步数据记录，每条记录变量用,分隔
        if(list.length>0) {
            Log.i("TAG---SQL整体查询转换的的list", "list[0]" + list[0] + ",list[-1]" + list[list.length - 1]);
        }
        StringBuffer dateSet = new StringBuffer();//用来查重复的日期值

        for (int i = 0; i < list.length; i++) {
            String[] data_single = list[i].split("#");//1条数据
            String date = data_single[data_single.length - 1];//1条数据的日期
            if (dateSet.toString().indexOf(date) == -1) {//该日期没出现过

                if (date != "" && date.split("年")[0] != "" && date.split("年")[1].split("月")[0] != "" && date.split("年")[1].split("月")[1].split("日")[0] != "") {
                    int year = Integer.parseInt(date.split("年")[0]);
                    int month = Integer.parseInt(date.split("年")[1].split("月")[0]);
                    int day = Integer.parseInt(date.split("年")[1].split("月")[1].split("日")[0]);
                    Log.i("TAG---SQL整体检验：", "日期为" + year + "-" + month + "-" + day);
                    map.put(getSchemeCalendar(year, month, day, 0xFFCC0000, "记").toString(),
                            getSchemeCalendar(year, month, day + 1, 0xFFCC0000, "记"));
                }

                dateSet.append(date);
                dateSet.append("/");

            }
        }


        //SP数据库方法：-------每一个String只是一个日期
//        String[] list_sp = checkWholeDate_SP().split("/") ;
//        Log.i("TAG", "list_sp[0]"+list_sp[0]+",list_sp[-1]"+list_sp[list_sp.length -1]);
//        for(int i = 0; i < list_sp.length ; i++) {
//            if(list_sp[i]!=""&&list_sp[i].split("年")[0]!=""&&list_sp[i].split("年")[1].split("月")[0]!=""&&list_sp[i].split("年")[1].split("月")[1].split("日")[0]!="") {
//                int year = Integer.parseInt(list_sp[i].split("年")[0]);
//                int month = Integer.parseInt(list_sp[i].split("年")[1].split("月")[0] );
//                int day = Integer.parseInt(list_sp[i].split("年")[1].split("月")[1].split("日")[0]);
//                Log.i("TAG", "日期为"+year+"-"+month+"-"+day);
//                map.put(getSchemeCalendar(year, month, day, 0xFFCC0000, "记").toString(),
//                        getSchemeCalendar(year, month, day + 1, 0xFFCC0000, "记"));
//            }
//        }

        //check方法：
//        map.put(getSchemeCalendar(2019, 6, 4, 0xFFCC0000, "记").toString(),
//                getSchemeCalendar(2019, 6, 5, 0xFFCC0000, "记"));
        //此方法在巨大的数据量上不影响遍历性能，推荐使用
        mCalendarView.setSchemeDate(map);
    }

    public void setRecycleViewSportData(int year, int month, int day) {
        System.out.println(year + "----23323232 " + month + " ____" + day);

        sportList.clear(); // 清除之前的数据

        // todo 查询数据库----done

        sportCalendarAdapter.notifyDataSetChanged();// 更新显示

        PathRecord pathRecord = new PathRecord();

//           SQL数据库方法：
        String[] list = checkSingleData(year, month, day);
        for (int i = 0; i < list.length; i++) {
            pathRecord = new PathRecord(list[i].split("#"));//String[]的构造方法
            sportList.add(pathRecord);
        }
        //  SP数据库方法：
//        pathRecord = new PathRecord(checkSingleData_SP(year,month,day).split("#") ) ;
//        sportList.add(pathRecord);

        System.out.println(sportList.size() + " ----=-=-323232322");
        if (sportList.size() > 0) {
            sport_record_listLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 对日历进行设置，当然也可以操作其它属性
     *
     * @param year
     * @param month
     * @param day
     * @param color
     * @param text
     * @return
     */
    private Calendar getSchemeCalendar(int year, int month, int day, int color, String text) {

        Calendar calendar = new Calendar();
        calendar.setYear(year);
        calendar.setMonth(month);
        calendar.setDay(day);
        calendar.setSchemeColor(color);//如果单独标记颜色、则会使用这个颜色
        calendar.setScheme(text);
        calendar.addScheme(new Calendar.Scheme());
        calendar.addScheme(0xFF008800, text);
        return calendar;
    }


    //recyclerView设置间距
    protected class SpaceItemDecoration extends RecyclerView.ItemDecoration {

        private int mSpace;

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.right = mSpace;
            outRect.left = mSpace;
            outRect.bottom = mSpace;
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = mSpace;
            } else {
                outRect.top = 0;
            }
        }

        SpaceItemDecoration(int space) {
            this.mSpace = space;
        }
    }


    /**
     * 求所有的数据值，返回为String[]的每个元素 是 ,分隔的String对象
     */
    public String[] checkWholeDatabase() {

        Cursor cursor = myDatabase.query("sportData", null, null, null, null, null, null);
        String[] list = new String[cursor.getCount()];

        int j = 0;
        if (cursor.moveToFirst()) {
            do {
                StringBuffer singleColumn = new StringBuffer();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    singleColumn.append(cursor.getString(i));
                    singleColumn.append("#");//手动加一条数据中的分隔符，区别于,和;
                }
                list[j] = singleColumn.toString();
                j++;
                //查完一条之后调用cursor.moveToNext()把cursor的位置移动到下一条
            } while (cursor.moveToNext());
        }
        cursor.close();
        if(list.length>=1) {
            Log.i("TAG", "SQL所有数据-最后一条数据查询结果为：" + list[list.length - 1]);
        }
        return list;
    }


    /**
     * @param year
     * @param month
     * @param day
     * @return 求某一天的数据值，返回为String[]的每个元素 是 ,分隔的String对象
     */
    public String[] checkSingleData(int year, int month, int day) {
        String dateString = year + "年" + month + "月" + day + "日";
        Cursor cursor = myDatabase.query("sportData", null, "dateTag like ?", new String[]{dateString}, null, null, null);

        String[] list = new String[cursor.getCount()];
        int j = 0;
        if (cursor.moveToFirst()) {
            do {
                StringBuffer singleColumn = new StringBuffer();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    singleColumn.append(cursor.getString(i));
                    singleColumn.append("#");//手动加一条数据中的分隔符，区别于,和;
                }
                list[j] = singleColumn.toString();
                j++;
                //查完一条之后调用cursor.moveToNext()把cursor的位置移动到下一条
            } while (cursor.moveToNext());
        }
        cursor.close();
        if (list == null || list.length == 0) {
            Toast.makeText(getContext(), "No Data!", Toast.LENGTH_SHORT).show();
        }
        return list;
    }


//    public String checkWholeDate_SP() {//返回不同日期值
//        SharedPreferences sp = getActivity().getSharedPreferences("basic_info", Context.MODE_PRIVATE);
//        String dateString;
//        StringBuffer datalist = new StringBuffer();
//        for (int i = 1; i < 13; i++) {
//            for (int j = 1; j < 32; j++) {
//                dateString = mYear + "年" + i + "月" + j + "日";
//                if (sp.getString(dateString, "") != "") {
//                    datalist.append(dateString);
//                    datalist.append("/");
//                }
//            }
//        }
//        Log.i("TAG", "整体查询语句结果为：" + datalist.toString());
//        return datalist.toString();
//    }
//
//
//    public String checkSingleData_SP(int year, int month, int day) {
//        SharedPreferences sp = getActivity().getSharedPreferences("basic_info", Context.MODE_PRIVATE);
//        String dateString = year + "年" + month + "月" + day + "日";
//        Log.i("TAG", "单句查询语句结果为：" + sp.getString(dateString, ""));
//        return sp.getString(dateString, "");
//    }
}
