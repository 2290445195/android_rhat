package com.rhat.r_hat.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
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
import java.util.Date;
import java.util.List;

public class DraftActivity extends AppCompatActivity {
    private Context context;
    private ImageButton imgbtn_save;
    private EditText et_title;
    private EditText et_diary;
    private Diary diary = new Diary();
    private List<Diary> diaryList;
    private DataTools dt = new DataTools();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //隐藏标题栏
        getSupportActionBar().hide();

        setContentView(R.layout.activity_update);

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
                new Thread(diaryNewSave).start();
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
                new Thread(diaryNewSave).start();
            }
        });
    }

    //初始化
    private void init(){
        //获取程序上下文
        context = getBaseContext();
        //获取控件对象
        imgbtn_save = (ImageButton) findViewById(R.id.new_imgbtn_save);
        et_title = (EditText) findViewById(R.id.update_et_title);
        et_diary = (EditText) findViewById(R.id.update_et_diary);
    }

    //手机按键事件
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //对应手机返回键
        if (keyCode == KeyEvent.KEYCODE_BACK ){
            backDlg();
        }
        return super.onKeyDown(keyCode, event);
    }

    //确认返回对话框
    private void backDlg(){
        //创建一个对话框对象
        AlertDialog.Builder builder = new AlertDialog.Builder(DraftActivity.this);
        builder.setTitle("温馨提示");
        builder.setMessage("确定要放弃更改？");
        //设置对话框的信息
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                //清空日记缓冲区
                dt.dalete(context, "diaryCache", "diaryNew");
                //新建一个Intent类对象，Intent为安卓四大组件的通信类
                Intent intent = new Intent();
                //设置Intent类的跳转
                intent.setClass(DraftActivity.this, MainActivity.class);
                //开始跳转
                startActivity(intent);
                //关闭本页面
                finish();
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

    //Handler类用来接受线程发送的消息，来更新UI
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
                        intent.setClass(DraftActivity.this, MainActivity.class);
                        startActivity(intent);
                        Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                    case 0:
                        intent.setClass(DraftActivity.this, MainActivity.class);
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
            //新建一个字符串，用来保存Json字符串
            String jsonStr = null;
            //从"diaryCache"中读取key为"diaryNew"的值,diaryCache为日记缓冲区
            if(!(jsonStr = dt.load(context, "diaryCache", "diaryNew")).equals("")){
                //把Json字符串转化为List，这是日记缓冲列表
                diaryList = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr));
                //获取日记列表里的日记对象
                diary = diaryList.get(0);
                //更新UI
                et_title.setText(diary.getTitle());
                et_diary.setText(diary.getDiary());
            }
        }
    };
    //添加日记
    Runnable diaryNewSave = new Runnable() {

        @Override
        public void run() {
            //创建一个字符串用来保存Json字符串
            String jsonStr = null;
            //声明日记id变量
            int id = 1;
            //声明日记列表长度变量
            int size = 0;
            //从"diaryInfo"中读取key为"diaryList"的值,diaryInfo为日记列表，不为空时
            if(!(jsonStr = dt.load(context, "diaryInfo", "diaryList")).equals("")){
                //把Json字符串转化为List
                diaryList = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr));
                //id等于日记列表最后一篇日记的id + 1
                id = diaryList.get(diaryList.size() - 1).getId() + 1;
                //size等于日记列表的长度加1
                size = diaryList.size() + 1;
            }
            //从"diaryCache"中读取key为"diaryNew"的值,diaryCache为日记缓冲区，如果不为空时
            if(!(jsonStr = dt.load(context, "diaryCache", "diaryNew")).equals("")){
                //把Json字符串转化为List
                diaryList = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr));
                //获取日记缓冲区第一篇日记
                diary = diaryList.get(0);
                //设置日记缓冲区里日记的id
                diary.setId(id);
                //声明一个空字符串，用来保存标题
                String title = null;
                //如果标题栏为空时
                if(et_title.getText().toString().equals("")){
                    //保存时添加一个默认的标题：日记+第几篇
                    title = "日记" + size;
                }else{  //如果不为空
                    //设置标题栏
                    title = et_title.getText().toString();
                }
                //设置日记对象的标题栏
                diary.setTitle(title);
                //设置日记对象的正文
                diary.setDiary(et_diary.getText().toString());
                //更新日记缓冲区第一篇日记的内容
                diaryList.set(0, diary);
                //把日记缓冲区的日记存储在文件中，文件名为"diaryCache"，key为"diaryNew"
                dt.save(context, "diaryCache", "diaryNew", dt.listToJsonArray(diaryList));
            }else{  //如果日记缓冲区为空
                //新建一个日记列表
                diaryList = new ArrayList<>();
                //设置新日记的id
                diary.setId(id);
                //设置新日记的标题
                diary.setTitle(et_title.getText().toString());
                //设置新日记的正文
                diary.setDiary(et_diary.getText().toString());
                //把新日记对象添加到新日记列表
                diaryList.add(diary);
                //把新日记列表保存到日记缓冲区，文件名为"diaryCache"，key为"diaryNew"
                dt.save(context, "diaryCache", "diaryNew", dt.listToJsonArray(diaryList));
            }
        }
    };
    //保存日记
    Runnable diaryLocalSave = new Runnable() {

        @Override
        public void run() {
            //声明发送给Handle的消息，成功为1，失败为0，未知错误为-1
            String value = "save_1";
            //如果标题栏和正文都为空
            if(et_title.getText().toString().equals("") && et_diary.getText().toString().equals("")){
                //把给Handle发送的消息设置成0
                value = "save_0";
            }else {
                //声明两个字符串用来保存Json字符串
                String jsonStr1 = null;
                String jsonStr2 = null;
                //读取日记缓冲区，文件名为"diaryCache"，key为"diaryNew"，如果不为空时
                if (!(jsonStr1 = dt.load(context, "diaryCache", "diaryNew")).equals("")) {
                    //声明一个列表变量
                    List<Diary> list = new ArrayList<Diary>();
                    //把Json字符串转化为List，这是日记缓冲区列表
                    list = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr1));
                    //读取日记列表，文件名为"diaryInfo"，key为"diaryList"，如果不为空时
                    if (!(jsonStr2 = dt.load(context, "diaryInfo", "diaryList")).equals("")) {
                        //Json字符串转换成列表，这是日记列表
                        diaryList = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr2));
                        //获取日记换冲区第一篇日记
                        diary = list.get(0);
                        //把获取的日记缓冲区第一篇日记添加到日记列表
                        diaryList.add(diary);
                        //保存日记列表，文件名为"diaryInfo"，key为"diaryList"
                        dt.save(context, "diaryInfo", "diaryList", dt.listToJsonArray(diaryList));
                        //删除日记缓冲区
                        dt.dalete(context, "diaryCache", "diaryNew");
                    } else {    //如果为空时
                        //声明一个新的列表对象
                        diaryList = new ArrayList<Diary>();
                        //获取日记缓冲区第一篇日记
                        diary = list.get(0);
                        //把获取的日记缓冲区的第一篇日记添加到新声明的列表里
                        diaryList.add(diary);
                        //保存日记列表，文件名为"diaryInfo"，key为"diaryList"
                        dt.save(context, "diaryInfo", "diaryList", dt.listToJsonArray(diaryList));
                        //删除日记缓冲区
                        dt.dalete(context, "diaryCache", "diaryNew");
                    }
                }
            }
            //向Handle发送消息，通知它更新UI
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("result", value);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

}
