package com.rhat.r_hat.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rhat.r_hat.R;
import com.rhat.r_hat.model.Diary;
import com.rhat.r_hat.tools.DataTools;
import com.rhat.r_hat.view.MyScrollView;
import com.rhat.r_hat.view.MyScrollView.OnScrollListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class DiaryActivity extends AppCompatActivity implements OnScrollListener {
    private Diary diary = new Diary();
    private List<Diary> diaryList;
    private DataTools dt = new DataTools();
    private Context context;
    private int itemPosition = 0;
    private TextView tv_title1;
    private TextView tv_title2;
    private TextView tv_diary;
    private MyScrollView sv;
    private ImageButton imgbtn_edit;
    private ImageButton imgbtn_del;
    private ImageView imgv_top;
    private ImageView imgv_date1;
    private ImageView imgv_date2;
    private RelativeLayout rl_main;
    private RelativeLayout rl_title2;
    private Intent intent;
    //声明日记日期需要的变量
    private final Calendar ca = Calendar.getInstance();
    private int mYear, mMonth, mDay;
    //顶部图片的高度
    private int imagv_height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //隐藏标题栏
        getSupportActionBar().hide();

        setContentView(R.layout.activity_diary);

        //初始化各变量
        init();

        //获取日记
        new Thread(getDiary).start();

        //bringToFront方法的作用：把控件放到最顶层
        imgbtn_edit.bringToFront();
        rl_main.bringToFront();
        rl_title2.bringToFront();
        rl_title2.setVisibility(View.GONE);
        sv.setOnScrollListener(this);

        imgbtn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent是安卓四大组件之间用来通信的类
                intent = new Intent();
                //设置Intent的跳转目标，第一个参数为本Activity,第二个为跳转目标的Activity
                intent.setClass(DiaryActivity.this, EditActivity.class);
                //在Intent里以（key, value）的形式存放数据
                intent.putExtra("itemPosition", itemPosition);
                //开始跳转
                startActivity(intent);
                //结束本页面
                finish();
            }
        });

        imgbtn_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delDlg();
            }
        });

        imgv_date1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dateDlg();
            }
        });

        imgv_date2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dateDlg();
            }
        });

    }

    private void init(){
        //获取程序上下文
        context = getApplicationContext();
        //获取上一个Activity传过来的intent
        intent = getIntent();
        //获取首页列表传过来的点击位置
        itemPosition = intent.getIntExtra("itemPosition", 0);
        //获取控件对象
        tv_title1 = (TextView) findViewById(R.id.diary_tv_title1);
        tv_title2 = (TextView) findViewById(R.id.diary_tv_title2);
        tv_diary = (TextView) findViewById(R.id.diary_tv_diary);
        sv = (MyScrollView) findViewById(R.id.diary_sv_parent);
        imgbtn_edit = (ImageButton) findViewById(R.id.diary_imgbtn_edit);
        imgbtn_del = (ImageButton) findViewById(R.id.diary_imgbtn_del);
        rl_main = (RelativeLayout) findViewById(R.id.diary_rl_main);
        rl_title2 = (RelativeLayout) findViewById(R.id.diary_rl_title2);
        imgv_top = (ImageView) findViewById(R.id.diary_imgv_top1);
        imgv_date1 = (ImageView) findViewById(R.id.diary_imgv_date1);
        imgv_date2 = (ImageView) findViewById(R.id.diary_imgv_date2);
        //初始化时期
        mYear = ca.get(Calendar.YEAR);
        mMonth = ca.get(Calendar.MONTH) + 1;
        mDay = ca.get(Calendar.DAY_OF_MONTH);
    }

    //程序窗口获得焦点时，布局已经绘制完成
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {
            //获取ImageView的高度
            imgv_top.post(new Runnable() {

                @Override
                public void run() {
                    //ImageView的宽和高
                    imagv_height = imgv_top.getHeight();
                }
            });
        }

    }

    //手机按键事件
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //对应手机返回键
        if (keyCode == KeyEvent.KEYCODE_BACK ){
            //新建一个Intent类对象，Intent为安卓四大组件的通信类
            intent = new Intent();
            //设置Intent类的跳转
            intent.setClass(DiaryActivity.this, MainActivity.class);
            //开始跳转
            startActivity(intent);
            //关闭本页面
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    /*事件监听*/
    //日记滑动，这里重写了ScrollView控件，实时获取scrollY
    @Override
    public void onScroll(int scrollY) {
        //判断Y轴的滚动距离
        if(scrollY >= imagv_height){    //如果滚动距离大于顶部图片的距离
            //显示顶部标题栏
            rl_title2.setVisibility(View.VISIBLE);
        }else if(scrollY <= imagv_height){  //如果滚动距离小于顶部图片的距离
            //隐藏顶部标题栏
            rl_title2.setVisibility(View.INVISIBLE);
        }
    }

    //日期选择对话框
    private void dateDlg(){
        //获取自定义对话框布局里的时间选择器控件
        LayoutInflater factory = LayoutInflater.from(this);
        View datePickerView = factory.inflate(R.layout.dlg_datepicker, null);
        DatePicker dp = (DatePicker) datePickerView.findViewById(R.id.dlg_dp);
        //初始化时间选择器：前三个参数是指时间选择器默认显示的时间，这里显示系统的时间，第四个参数是监听器
        dp.init(ca.get(Calendar.YEAR), ca.get(Calendar.MONTH), ca.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                //当时间选择器的时间改变时，保存选择的时间
                mYear = year;
                mMonth = monthOfYear + 1;
                mDay = dayOfMonth;
            }
        });
        //创建一个对话框对象
        DatePickerDialog.Builder builder = new DatePickerDialog.Builder(DiaryActivity.this);
        //设置对话框的信息
        builder.setView(datePickerView);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                //拼凑时间
                String date = mYear+"-"+mMonth+"-"+mDay;
                //创建一个字符串用来保存Json字符串
                String jsonStr = null;
                //从"diaryInfo"中读取key为"diaryList"的值
                if(!(jsonStr = dt.load(context, "diaryInfo", "diaryList")).equals("")){
                    //把Json字符串转化为List
                    diaryList = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr));
                    //根据首页列表传过来的点击位置，来获取日记列表对应的日记
                    diary = diaryList.get(itemPosition);
                    //设置日记的时间
                    diary.setDate(date);
                    diaryList.set(itemPosition, diary);
                    dt.save(context, "diaryInfo", "diaryList", dt.listToJsonArray(diaryList));
                    Toast.makeText(context, "修改时间成功", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
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
    //确认删除对话框
    private void delDlg(){
        //创建一个对话框对象
        AlertDialog.Builder builder = new AlertDialog.Builder(DiaryActivity.this);
        builder.setTitle("温馨提示");
        builder.setMessage("确定要删除这篇日记？");
        //设置对话框的信息
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                //创建一个字符串用来保存Json字符串
                String jsonStr = null;
                //从"diaryInfo"中读取key为"diaryList"的值
                if(!(jsonStr = dt.load(context, "diaryInfo", "diaryList")).equals("")){
                    //把Json字符串转化为List
                    diaryList = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr));
                    //根据首页列表传过来的点击位置，来获取日记列表对应的日记
                    diaryList.remove(itemPosition);
                    dt.save(context, "diaryInfo", "diaryList", dt.listToJsonArray(diaryList));
                    intent = new Intent();
                    intent.setClass(DiaryActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
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
    //获取日记
    Runnable getDiary = new Runnable() {

        @Override
        public void run() {
            //创建一个字符串用来保存Json字符串
            String jsonStr = null;
            //从"diaryInfo"中读取key为"diaryList"的值
            if(!(jsonStr = dt.load(context, "diaryInfo", "diaryList")).equals("")){
                //把Json字符串转化为List
                diaryList = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr));
                //根据首页列表传过来的点击位置，来获取日记列表对应的日记
                diary = diaryList.get(itemPosition);
                //把日记内容显示到UI
                tv_title1.setText(diary.getTitle());
                tv_title2.setText(diary.getTitle());
                tv_diary.setText(diary.getDiary());
            }
        }
    };

}
