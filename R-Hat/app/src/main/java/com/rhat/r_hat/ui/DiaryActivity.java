package com.rhat.r_hat.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rhat.r_hat.R;
import com.rhat.r_hat.model.Diary;
import com.rhat.r_hat.tools.DataTools;
import com.rhat.r_hat.view.MyScrollView;
import com.rhat.r_hat.view.MyScrollView.OnScrollListener;

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
    private ImageView imgv_top;
    private ImageView imgv_title;
    private RelativeLayout rl_main;
    private RelativeLayout rl_title2;
    private Intent intent;
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
        rl_main = (RelativeLayout) findViewById(R.id.diary_rl_main);
        rl_title2 = (RelativeLayout) findViewById(R.id.diary_rl_title2);
        imgv_top = (ImageView) findViewById(R.id.diary_imgv_top1);
        imgv_title = (ImageView) findViewById(R.id.diary_imgv_weather1);
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
