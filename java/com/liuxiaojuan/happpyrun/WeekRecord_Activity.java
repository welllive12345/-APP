package com.liuxiaojuan.happpyrun;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.liuxiaojuan.happpyrun.utils.MyDatabaseHelper;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.formatter.ColumnChartValueFormatter;
import lecho.lib.hellocharts.formatter.SimpleColumnChartValueFormatter;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.LineChartView;

/**
 *  一周跑步记录
 *  按天显示 柱状图、折线图等，这个页面大家可以自定义， 不需要非按照我的那个图片的样式
 *  （比如一条折线显示每天跑步的公里，一条显示每天跑步的时间，一条显示跑步的卡路里 等等，）
 *  要求：只要显示出数据库里跑步记录的一些分析即可
 */
public class WeekRecord_Activity extends AppCompatActivity {


    private LineChartView lineChart;
    private ColumnChartView ColumnChart;
    private TextView weekSteps;
    private TextView avSteps;
    private TextView weekDuration;
    private TextView avDuration;


    SimpleDateFormat sdf ;//输出格式,eg:2017年10月17日 11时10分21秒 星期二
    DateFormat df;
    String todayWeek;
    String todayDate;
    ArrayList<String> dateList = new ArrayList<String>() ;
    ArrayList<String> weekList = new ArrayList<String>() ;//获取本周周几的排序--X轴的标注
    ArrayList<Integer> distanceList = new ArrayList<Integer>();//每天的总距离
    ArrayList<Integer> durationList = new ArrayList<Integer>();
    ArrayList<Integer> caloriesList = new ArrayList<Integer>();



    //创建数据库：
    MyDatabaseHelper myDatabaseHelper;
    SQLiteDatabase myDatabase;


    //格式：
    private DecimalFormat intFormat = new DecimalFormat("#");
    private DecimalFormat decimalFormat = new DecimalFormat("0.0");

    private List<PointValue> lPointValues = new ArrayList<PointValue>();
    private List<AxisValue> mAxisXValues = new ArrayList<AxisValue>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week_record);
        initView();
        initLineChart();//初始化
        initColumnChart (durationList, Color.parseColor("#008577"),ColumnChart);
    }

    private void initView() {

        lineChart = (LineChartView)findViewById(R.id.line_chart);
        ColumnChart = (ColumnChartView) findViewById(R.id.Column_chart);
        weekSteps =(TextView)findViewById(R.id.tv_weekSteps);
        avSteps =(TextView)findViewById(R.id.tv_avSteps);
        weekDuration =(TextView)findViewById(R.id.tv_weekDuration);
        avDuration=(TextView)findViewById(R.id.tv_avDuration );

        //创建数据库：
        myDatabaseHelper = new MyDatabaseHelper(this);
        myDatabase = myDatabaseHelper.getWritableDatabase();


        df = DateFormat.getDateTimeInstance(DateFormat.FULL,DateFormat.FULL);

        //得到本周的日期和星期
        Long currentTime =System.currentTimeMillis();
        for(int i = 6;i >=0 ;i--) {
            dateList.add(df.format(new Date(currentTime-1000*60*60*24*i)).split(" ")[0].split("星期")[0]);
            Log.i("TAG", "本次秒数是"+(currentTime-1000*60*60*24*i));
            Log.i("TAG", "本次日期是"+df.format(new Date(currentTime-1000*60*60*24*i)).split(" ")[0].split("星期")[0]);
            weekList.add(df.format(new Date(currentTime-1000*60*60*24*i)).split(" ")[0].split("日")[1]);
            Log.i("TAG", "本次星期是"+df.format(new Date(currentTime-1000*60*60*24*i)).split(" ")[0].split("日")[1]);
        }
        if(weekList.size() >0) {
            weekList.set(weekList.size() - 1, "今天");
            weekList.set(weekList.indexOf("星期"),"星期天");
        }

        //得到本周的相关数据
        getData(dateList);

        //得到总数据和平均数据：
        Integer totalDistance = 0;
        Integer totalDuration = 0;
        for(int i = 0 ;i < distanceList.size();i++){
            totalDistance +=distanceList.get(i);
        }
        for(int i = 0 ;i < durationList.size();i++){
            totalDuration +=durationList.get(i);
        }
        weekSteps.setText(String.valueOf(totalDistance));
        Log.i("TAG", "总步数是"+totalDistance);
        weekDuration.setText(totalDuration/60+"时"+totalDuration%60+"分");
        Log.i("TAG", "总时间是"+totalDuration/60+"时"+totalDuration%60+"分");
        avSteps.setText(String.valueOf(totalDistance/7));
        Log.i("TAG", "平均步数是"+totalDistance/7);
        avDuration.setText(decimalFormat.format(totalDuration/7d)+"分") ;
        Log.i("TAG", decimalFormat.format(totalDuration/7d)+"分");



    }


    //初始化折线图：
    private void initLineChart(){
        for (int i = 0; i < weekList.size(); i++) {
            mAxisXValues.add(new AxisValue(i).setLabel(weekList.get(i)));
        }
        for (int i = 0; i < distanceList.size() ; i++) {
            lPointValues.add(new PointValue(i, distanceList .get(i)));
        }
        Line line = new Line(lPointValues).setColor(Color.parseColor("#FFCD41"));  //折线的颜色（橙色）
        List<Line> lines = new ArrayList<Line>();
        line.setShape(ValueShape.CIRCLE);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
        line.setCubic(false);//曲线是否平滑，即是曲线还是折线
        line.setFilled(false);//是否填充曲线的面积
//        line.setHasLabels(true);//曲线的数据坐标是否加上备注
        line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        line.setHasLines(true);//是否用线显示。如果为false 则没有曲线只有点显示
        line.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);

        //坐标轴
        Axis axisX = new Axis(); //X轴
        axisX.setHasTiltedLabels(false);  //X坐标轴字体是斜的显示还是直的，true是斜的显示
        axisX.setTextColor( Color.parseColor("#708090"));  //设置字体颜色
        //axisX.setName("date");  //表格名称
        axisX.setTextSize(10);//设置字体大小
//        axisX.setMaxLabelChars(8); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisXValues.length
        axisX.setValues(mAxisXValues);  //填充X轴的坐标名称
        Log.i("TAG", "X坐标轴是"+mAxisXValues);
        data.setAxisXBottom(axisX); //x 轴在底部
        //data.setAxisXTop(axisX);  //x 轴在顶部
        axisX.setHasLines(true); //x 轴分割线

        // Y轴是根据数据的大小自动设置Y轴上限(在下面我会给出固定Y轴数据个数的解决方案)
        Axis axisY = new Axis();  //Y轴
        axisY.setName("步数");//y轴标注
        axisY.setTextColor( Color.parseColor("#708090"));
        axisY.setTextSize(12);//设置字体大小
        data.setAxisYLeft(axisY);  //Y轴设置在左边
        //data.setAxisYRight(axisY);  //y轴设置在右边


        //设置行为属性，支持缩放、滑动以及平移
        lineChart.setInteractive(true);
        lineChart.setZoomType(ZoomType.HORIZONTAL);
        lineChart.setMaxZoom((float) 2);//最大方法比例
        lineChart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChart.setLineChartData(data);
        lineChart.setVisibility(View.VISIBLE);
        /**注：下面的7，10只是代表一个数字去类比而已
         * 当时是为了解决X轴固定数据个数。见（http://forum.xda-developers.com/tools/programming/library-hellocharts-charting-library-t2904456/page2）;
         */
//        Viewport v = new Viewport(lineChart.getMaximumViewport());
//        v.left = 0;
//        v.right= 7;
//        lineChart.setCurrentViewport(v);
    }

    //初始化柱状图：
    private void initColumnChart(List<Integer> columnDatas, int columnColor, ColumnChartView charView) {
        // 使用的 7列，每列1个subcolumn。
        int numSubcolumns = 1;
        int numColumns = 7;
        //定义一个圆柱对象集合
        List<Column> columns = new ArrayList<Column>();
        //子列数据集合
        List<SubcolumnValue> values;

        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        //遍历列数numColumns
        for (int i = 0; i < numColumns; ++i) {
            values = new ArrayList<SubcolumnValue>();
            //遍历每一列的每一个子列
            for (int j = 0; j < numSubcolumns; ++j) {
                //为每一柱图添加颜色和数值
                Integer f = columnDatas.get(i);
                values.add(new SubcolumnValue(f, columnColor));
            }
            //创建Column对象
            Column column = new Column(values);
            ColumnChartValueFormatter chartValueFormatter = new SimpleColumnChartValueFormatter(0);
            column.setFormatter(chartValueFormatter);
            //是否有数据标注
            column.setHasLabels(true);
            //是否是点击圆柱才显示数据标注
            column.setHasLabelsOnlyForSelected(false);
            columns.add(column);
            //给x轴坐标设置描述
            if(weekList.size() > i) {
                axisValues.add(new AxisValue(i).setLabel(weekList.get(i)));
            }
        }
        //创建一个带有之前圆柱对象column集合的ColumnChartData
        ColumnChartData data = new ColumnChartData(columns);
        data.setValueLabelTextSize(8);
        data.setValueLabelBackgroundColor(Color.parseColor("#00000000"));
//        data.setValueLabelTypeface(Typeface.DEFAULT);// 设置数据文字样式
        data.setValueLabelBackgroundEnabled(true);
        data.setValueLabelBackgroundAuto(false);
        //定义x轴y轴相应参数
        Axis axisX = new Axis();
        Axis axisY = new Axis().setHasLines(true);
        axisY.setName("分钟");//轴名称
        axisY.hasLines();//是否显示网格线
        axisY.setTextColor(Color.parseColor("#708090"));//颜色
        axisY.setTextSize(12);
        axisX.hasLines();
        axisX.setTextColor(Color.parseColor("#708090"));
        axisX.setValues(axisValues);
        axisX.setTextSize(10);
        axisX.setHasSeparationLine(false);
        //把X轴Y轴数据设置到ColumnChartData 对象中
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);
        //给表填充数据，显示出来
        charView.setInteractive(false);
        charView.setColumnChartData(data);


        //设置行为属性，支持缩放、滑动以及平移
        charView.setInteractive(true);
        charView.setZoomType(ZoomType.HORIZONTAL);
        charView.setMaxZoom((float) 2);//最大方法比例
        charView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        charView.setVisibility(View.VISIBLE);
    }

    public void getData(ArrayList<String> dateTags) {
        for(int k = 0;k <dateTags.size() ; k++) {
            Integer todayDistance = 0;
            Integer todayDuration = 0;
            Integer todayCalorie = 0;
            Cursor cursor = myDatabase.query("sportData", null, "dateTag like ?", new String[]{dateTags.get(k)}, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    todayDuration += Integer.parseInt(intFormat.format(Double.parseDouble(cursor.getString(5))));
                    todayDistance +=Integer.parseInt(intFormat.format(Double.parseDouble(cursor.getString(6))));
                    todayCalorie += Integer .parseInt(intFormat.format(Double.parseDouble(cursor.getString(7))));

                    //查完一条之后调用cursor.moveToNext()把cursor的位置移动到下一条
                } while (cursor.moveToNext());
            }
            cursor.close();
            distanceList.add(Integer .parseInt(intFormat.format(todayDistance)));//米
            durationList.add(Integer .parseInt(intFormat.format(todayDuration/60d)));//分钟
            caloriesList.add(Integer.parseInt(intFormat.format(todayCalorie)));//卡路里
        }
    }

    }


