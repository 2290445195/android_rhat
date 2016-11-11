package com.rhat.r_hat.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.rhat.r_hat.R;
import com.rhat.r_hat.model.Diary;
import com.rhat.r_hat.tools.DataTools;

import java.util.ArrayList;
import java.util.List;

public class EditActivity extends AppCompatActivity {
    private Intent intent;
    private int itemPosition = 0;
    private ImageButton imgbtn_save;
    private Diary diary = new Diary();
    private List<Diary> diaryList;
    private DataTools dt = new DataTools();
    private Context context;
    private EditText et_title;
    private EditText et_diary;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //隐藏标题栏
        getSupportActionBar().hide();

        setContentView(R.layout.activity_update);

        //初始化
        init();

        //获取日记
        new Thread(getDiary).start();

        //保存按钮的点击事件
        imgbtn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 开启一个子线程，进行本地储存日记，等待有返回结果，使用handler通知UI
                new Thread(diaryLocalSave).start();
            }
        });

        //标题栏编辑框的内容改变事件
        et_title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                // 开启一个子线程，进行本地储存日记，等待有返回结果，使用handler通知UI
                new Thread(diaryEditSave).start();
            }
        });

        //正文编辑框的内容改变事件
        et_diary.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                // 开启一个子线程，进行本地储存日记，等待有返回结果，使用handler通知UI
                new Thread(diaryEditSave).start();
            }
        });

    }

    //初始化
    private void init(){
        //获取程序上下文
        context = getApplicationContext();
        //获取上一个Activity传过来的Intent
        intent = getIntent();
        //获取控件对象
        itemPosition = intent.getIntExtra("itemPosition", 0);
        et_title = (EditText) findViewById(R.id.update_et_title);
        et_diary = (EditText) findViewById(R.id.update_et_diary);
        imgbtn_save = (ImageButton) findViewById(R.id.new_imgbtn_save);
    }

    //手机按键事件
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //对应手机返回键
        if (keyCode == KeyEvent.KEYCODE_BACK ){
            //新建一个Intent类对象，Intent为安卓四大组件的通信类
            intent = new Intent();
            //设置Intent类的跳转
            intent.setClass(EditActivity.this, MainActivity.class);
            //开始跳转
            startActivity(intent);
            //关闭本页面
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    //Handle类用来接收线程发送的消息，然后更新UI
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String result = data.getString("result");
            String value[] = result.split("_");
            Log.i("mylog", "请求结果为-->" + result);
            // UI界面的更新等相关操作
            if("save".equals(value[0])){
                Intent intent = new Intent();
                switch (Integer.parseInt(value[1])){
                    case 1:
                        intent.setClass(EditActivity.this, MainActivity.class);
                        startActivity(intent);
                        Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                    case 0:
                        intent.setClass(EditActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    case -1:break;
                    default:
                }
            }
        }
    };

    /**
     * 子线程
     */
    //获取日记
    Runnable getDiary = new Runnable() {

        @Override
        public void run() {
            //**Looper用来代替Handle（不用回答）
            Looper.prepare();
            //新建一个字符串，用来保存Json字符串
            String jsonStr = null;
            //从"diaryInfo"中读取key为"diaryList"的值,"diaryInfo"为日记列表，如果不为空时
            if(!(jsonStr = dt.load(context, "diaryInfo", "diaryList")).equals("")){
                //把Json字符串转化为List，这是日记列表
                diaryList = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr));
                //根据首页传过来的列表点击位置，来获取日记列表里对应位置的日记对象
                diary = diaryList.get(itemPosition);
                //更新UI显示的内容
                et_title.setText(diary.getTitle());
                et_diary.setText(diary.getDiary());
            }
            Looper.loop();
        }
    };
    //修改日记
    Runnable diaryEditSave = new Runnable() {

        @Override
        public void run() {
            //创建一个字符串来存放Json字符串
            String jsonStr = null;
            //声明size变量，用来记录这是日记列表里的第几篇日记
            int size = itemPosition + 1;
            //从"diaryCache"中读取key为"diaryNew"的值，"diaryCache"为日记缓冲区，如果不为空时
            if(!(jsonStr = dt.load(context, "diaryCache", "diaryNew")).equals("")){
                //声明一个字符串
                String title = null;
                //把Json字符串转化为List
                diaryList = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr));
                //获取日记缓冲区第一篇日记
                diary = diaryList.get(0);
                //如果标题栏为空，设置一个默认标题：日记+第几篇
                if(et_title.getText().toString().equals("")){
                    title = "日记" + size;
                }else{  //如果不为空
                    //获取标题栏内容
                    title = et_title.getText().toString();
                }
                //更新日记对象的标题
                diary.setTitle(title);
                //更新日记对象的正文
                diary.setDiary(et_diary.getText().toString());
                //更新日记列表对应的日记
                diaryList.set(0, diary);
                //保存日记列表到缓冲区，文件名为"diaryCache"，key为"diaryNew"
                dt.save(context, "diaryCache", "diaryNew", dt.listToJsonArray(diaryList));
                Log.v("jsonStr", dt.load(context, "diaryCache", "diaryNew"));
            }else{  //如果为空
                //新建一个日记列表
                diaryList = new ArrayList<>();
                //设置日记对象的标题
                diary.setTitle(et_title.getText().toString());
                //设置日记对象的正文
                diary.setDiary(et_diary.getText().toString());
                //把日记对象添加到列表里
                diaryList.add(diary);
                //保存日记列表到缓冲区，文件名为"diaryCache"，key为"diaryNew"
                dt.save(context, "diaryCache", "diaryNew", dt.listToJsonArray(diaryList));
                Log.v("jsonStr", dt.load(context, "diaryCache", "diaryNew"));
            }
        }
    };
    //保存日记
    Runnable diaryLocalSave = new Runnable() {

        @Override
        public void run() {
            //新建一个字符串，用来保存发送给Handle的消息，1为成功，0为失败，-1为未知错误
            String value = "save_1";
            //如果标题和正文都为空，则把value设置为0
            if(et_title.getText().toString().equals("") && et_diary.getText().toString().equals("")){
                value = "save_0";
            }else { //如果不为空
                //新建两个字符串，用来保存Json字符串
                String jsonStr1 = null;
                String jsonStr2 = null;
                //从"diaryCache"中读取key为"diaryNew"的值，如果不为空
                if (!(jsonStr1 = dt.load(context, "diaryCache", "diaryNew")).equals("")) {
                    //创建一个列表对象
                    List<Diary> list = new ArrayList<Diary>();
                    //把Json字符串转化为List，读取日记缓存列表
                    list = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr1));
                    //从"diaryInfo"中读取key为"diaryList"的值
                    jsonStr2 = dt.load(context, "diaryInfo", "diaryList");
                    //把Json字符串转化为List，读取日记列表
                    diaryList = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr2));
                    //获取日记缓存列表的日记对象
                    diary = list.get(0);
                    //更新日记列表里对应位置的日记对象
                    diaryList.set(itemPosition, diary);
                    //把日记列表对象保存到"diaryInfo"里，key为"diaryList"
                    dt.save(context, "diaryInfo", "diaryList", dt.listToJsonArray(diaryList));
                    //删除日记缓存列表，文件名"diaryCache"，key为"diaryNew"
                    dt.dalete(context, "diaryCache", "diaryNew");
                    Log.v("jsonStr", dt.load(context, "diaryInfo", "diaryList"));
                }
            }
            //向Handle发送消息
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("result", value);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

}
