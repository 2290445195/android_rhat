package com.rhat.r_hat.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import java.util.ArrayList;
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


    //日记id
    private int id;
    //顶部图片的高度
    private int imagv_height;
    //标题布局的高度
    private int titleViewHeight;
    //标题布局与其父类布局的顶部距离
    private int titleViewTop;
    //TextView的高度
    private int tvheight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //隐藏标题栏
        getSupportActionBar().hide();

        setContentView(R.layout.activity_diary);
        init();

        intent = getIntent();
        itemPosition = intent.getIntExtra("itemPosition", 0);

        //获取日记
        new Thread(getDiary).start();

        imgbtn_edit.bringToFront();
        rl_main.bringToFront();
        rl_title2.bringToFront();
        rl_title2.setVisibility(View.GONE);
        sv.setOnScrollListener(this);

        imgbtn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent();
                intent.setClass(DiaryActivity.this, EditActivity.class);
                intent.putExtra("id", id);
                startActivity(intent);
                finish();
            }
        });

    }

    private void init(){
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
        context = getApplicationContext();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {
            //获取TextView的高度
            tvheight = tv_diary.getHeight();

            //获取ImageView的高度
            imgv_top.post(new Runnable() {

                @Override
                public void run() {
                    //ImageView的宽和高
                    imagv_height = imgv_top.getHeight();
                }
            });

            //获取Title的高度
            imgv_title.post(new Runnable() {

                @Override
                public void run() {
                    //ImageView的宽和高
                    titleViewHeight = imgv_title.getHeight() + 16 * 2;
                }
            });
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK ){
            intent = new Intent();
            intent.setClass(DiaryActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    /*事件监听*/
    //日记滑动
    @Override
    public void onScroll(int scrollY) {
        if(scrollY >= imagv_height){
            rl_title2.setVisibility(View.VISIBLE);
        }else if(scrollY <= imagv_height){
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
            // TODO
            //本地保存日记
            String jsonStr = null;
            if(!(jsonStr = dt.load(context, "diaryInfo", "diaryList")).equals("")){
                //把Json字符串转化为List
                diaryList = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr));
                diary = diaryList.get(itemPosition);
                id = diary.getId();
                tv_title1.setText(diary.getTitle());
                tv_title2.setText(diary.getTitle());
                tv_diary.setText(diary.getDiary());
            }
        }
    };

}
