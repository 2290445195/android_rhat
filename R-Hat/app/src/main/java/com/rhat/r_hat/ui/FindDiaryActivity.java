package com.rhat.r_hat.ui;

import android.app.DatePickerDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.rhat.r_hat.R;
import com.rhat.r_hat.model.Diary;
import com.rhat.r_hat.tools.DataTools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class FindDiaryActivity extends ListActivity {
    private DataTools dt = new DataTools();
    private Context context;
    private Diary diary = new Diary();
    private List<Diary> diaryList;
    //定义一个列表，作为数据源
    private ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
    private Diary diaryCache;
    private Intent intent = new Intent();
    private ListView lv;
    private ImageButton imgbtn_new;
    private SimpleAdapter adapter;
    private final int DATE_DIALOG = 1;
    private final Calendar ca = Calendar.getInstance();
    private int mYear, mMonth, mDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //初始化
        init();

        //检查是否未保存
        new Thread(checkCache).start();

        /*
        *日记列表
        */
        //声明一个字符串，用来保存Json字符串
        String jsonStr = null;
        //从"diaryInfo"中读取key为"diaryList"的值，如果不为空
        if (!(jsonStr = dt.load(context, "diaryInfo", "diaryList")).equals("")) {
            //把Json字符串转化为List，读取日记列表
            diaryList = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr));
            //给数据源赋值
            for (int i = 0; i < diaryList.size(); i++) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                //循环获取日记列表里的日记
                diary = diaryList.get(i);
                //声明一个字符串，用来保存日记正文
                String diaryStr = "";
                //声明一个整型变量，用来保存摘要显示的最大字数
                int end = 0;
                //获取日记对象的正文，如果不为空时
                if (!diary.getDiary().equals("")) {
                    //把日记正文赋值给字符串
                    diaryStr = diary.getDiary();
                }
                //判断日记正文的长度，如果大于10时
                if (diaryStr.length() >= 10) {
                    //摘要的最大显示字数为10
                    end = 10;
                } else {    //如果小于10时
                    //摘要的最大显示字数为正文长度
                    end = diaryStr.length();
                }
                //把日记对象的标题放到map里
                map.put("diary_title", diary.getTitle());
                //把日记对象的日期放到map里
                map.put("diary_date", diary.getDate());
                //把日记对象的摘要放到map里
                map.put("diary_str", diaryStr.substring(0, end));
                //把map添加到首页列表视图的数据源里
                listItem.add(map);
            }
        }

        //设置适配器，第一个参数为列表视图所处的Activity，第二个参数为数据源，第三个参数为列表每一项的布局，第四个参数为数据源里每一项map对应的key，第五个参数为第四个参数的key对应的value要显示在哪里，即对应布局的id
        adapter = new SimpleAdapter(FindDiaryActivity.this, listItem, R.layout.item_main, new String[] {"diary_title","diary_date","diary_str"}, new int[] {R.id.item_main_title,R.id.item_main_date,R.id.item_main_str});
        setListAdapter(adapter);

        /*事件监听*/
        //新建日记按钮点击事件
        imgbtn_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //跳转
                intent.setClass(FindDiaryActivity.this, NewActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //列表的点击事件
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                intent.setClass(FindDiaryActivity.this, DiaryActivity.class);
                intent.putExtra("itemPosition", position);
                startActivity(intent);
                finish();
            }
        });

    }

    //当页面获得焦点时，即页面已经绘制完毕
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //列表的滚动监听
        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                if(lv.getChildAt(0) != null)
                if(lv.getChildAt(0).getTop() <= 0){
                    dateDlg();
                }
            }
        });
    }

    //初始化
    protected void init(){
        //获取程序上下文
        context = getApplicationContext();
        //获取控件对象
        imgbtn_new = (ImageButton) findViewById(R.id.main_btn_new);
        lv = (ListView) findViewById(android.R.id.list);
        //初始化时期
        mYear = ca.get(Calendar.YEAR);
        mMonth = ca.get(Calendar.MONTH);
        mDay = ca.get(Calendar.DAY_OF_MONTH);
    }

    //日期选择器的监听器
    private DatePickerDialog.OnDateSetListener mdateListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
        }
    };

    /**对话框**/
    //提示对话框
    private void cacheDlg(){
        Looper.prepare();
        //创建一个对话框对象
        AlertDialog.Builder builder = new AlertDialog.Builder(FindDiaryActivity.this);
        //设置对话框的信息
        builder.setTitle("温馨提示");
        builder.setMessage("您存在未保存的内容，是否继续？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                //如果日记列表为空
                if(diaryList.isEmpty()){
                    //跳转
                    intent.setClass(FindDiaryActivity.this, DraftActivity.class);
                    //关闭对话框
                    dialog.dismiss();
                    startActivity(intent);
                    finish();
                }else{  //如果不为空
                    if (diaryCache.getId() == diaryList.size()) {
                        intent.setClass(FindDiaryActivity.this, CacheActivity.class);
                        dialog.dismiss();
                        startActivity(intent);
                        finish();
                    }else{
                        intent.setClass(FindDiaryActivity.this, DraftActivity.class);
                        dialog.dismiss();
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dt.dalete(context, "diaryCache", "diaryNew");
                dialog.dismiss();
            }
        });
        //显示对话框
        builder.show();
        Looper.loop();
    }
    //提示对话框
    private void dateDlg(){
        LayoutInflater factory = LayoutInflater.from(this);
        final View datePickerView = factory.inflate(R.layout.dlg_datepicker, null);
        //创建一个对话框对象
        AlertDialog.Builder builder = new AlertDialog.Builder(FindDiaryActivity.this);
        //设置对话框的信息
        builder.setTitle("选择时间");
        builder.setView(datePickerView);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                //拼凑时间
                String date = mYear+"-"+mMonth+"-"+mDay;
                //创建一个字符串来存放Json字符串
                String jsonStr = null;
                //清空列表数据源
                listItem.clear();
                jsonStr = dt.load(context, "diaryInfo", "diaryList");
                diaryList = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr));
                Log.v("jsondebug", jsonStr);
                //遍历日记列表
                for(int i = 0;i < diaryList.size();i++){
                    if(diaryList.get(i).getDate() != null)
                    if(diaryList.get(i).getDate().equals(date)){
                        HashMap<String, Object> map = new HashMap<String, Object>();
                        //把日记对象的标题放到map里
                        map.put("diary_title", diary.getTitle());
                        //把日记对象的日期放到map里
                        map.put("diary_date", diary.getDate());
                        //把日记对象的摘要放到map里
                        map.put("diary_str", diary.getDiary());
                        //把map添加到首页列表视图的数据源里
                        listItem.add(map);
                    }
                }
                dialog.dismiss();
                //设置适配器
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        //显示对话框
        builder.show();
    }

    /**
     * 子线程
     */
    //检查日记
    Runnable checkCache = new Runnable() {

        @Override
        public void run() {
            //创建一个字符串来存放Json字符串
            String jsonStr = null;
            //从"diaryCache"中读取key为"diaryNew"的值，"diaryCache"为日记缓冲区，如果不为空时
            if(!(jsonStr = dt.load(context, "diaryCache", "diaryNew")).equals("")){
                //把Json字符串转化为List
                diaryCache = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr)).get(0);
                //从"diaryInfo"中读取key为"diaryList"的值，"diaryInfo"为日记列表，如果不为空时
                if(!(jsonStr = dt.load(context, "diaryInfo", "diaryList")).equals("")){
                    //把Json字符串转化为List
                    diaryList = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr));
                }else{
                    //新建一个日记列表
                    diaryList = new ArrayList<Diary>();
                }
                cacheDlg();
            }
        }
    };

}
