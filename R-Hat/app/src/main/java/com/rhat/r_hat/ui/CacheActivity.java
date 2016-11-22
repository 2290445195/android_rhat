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
import java.util.List;

public class CacheActivity extends AppCompatActivity {
    private int size = 0;
    private ImageButton imgbtn_save;
    private Diary diary = new Diary();
    private List<Diary> diaryList;
    private DataTools dt = new DataTools();
    private Context context;
    private EditText et_title;
    private EditText et_diary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //隐藏标题栏
        getSupportActionBar().hide();

        setContentView(R.layout.activity_update);

        init();

        //获取日记
        new Thread(getDiary).start();

        imgbtn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 开启一个子线程，进行本地储存日记，等待有返回结果，使用handler通知UI
                new Thread(diaryLocalSave).start();
            }
        });

        et_title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                // 开启一个子线程，进行日记缓存
                new Thread(diaryEditSave).start();
            }
        });

        et_diary.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                // 开启一个子线程，进行日记缓存
                new Thread(diaryEditSave).start();
            }
        });

    }

    private void init(){
        //获取程序上下文
        context = getApplicationContext();
        //获取控件对象
        et_title = (EditText) findViewById(R.id.update_et_title);
        et_diary = (EditText) findViewById(R.id.update_et_diary);
        imgbtn_save = (ImageButton) findViewById(R.id.new_imgbtn_save);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(CacheActivity.this);
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
                intent.setClass(CacheActivity.this, MainActivity.class);
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

    //子线程用来更新UI的类
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String result = data.getString("result");
            String value[] = result.split("_");
            Log.i("mylog", "请求结果为-->" + result);
            // TODO
            // UI界面的更新等相关操作
            if("save".equals(value[0])){
                Intent intent = new Intent();
                switch (Integer.parseInt(value[1])){
                    case 1:
                        intent.setClass(CacheActivity.this, MainActivity.class);
                        startActivity(intent);
                        Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                    case 0:
                        intent.setClass(CacheActivity.this, MainActivity.class);
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
            //创建一个字符串来存放Json字符串
            String jsonStr = null;
            //从"diaryInfo"中读取key为"diaryList"的值，这是日记列表，如果不为空时
            if(!(jsonStr = dt.load(context, "diaryInfo", "diaryList")).equals("")){
                //把Json字符串转化为List，读取日记列表
                diaryList = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr));
                //保存日记列表的长度
                size = diaryList.size();
            }
            //从"diaryCache"中读取key为"diaryNew"的值，这是日记缓冲区，如果不为空时
            if(!(jsonStr = dt.load(context, "diaryCache", "diaryNew")).equals("")){
                //把Json字符串转化为List，读取日记缓存列表
                diaryList = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr));
                //获取缓存的日记对象
                diary = diaryList.get(0);
                //显示日记内容到UI
                et_title.setText(diary.getTitle());
                et_diary.setText(diary.getDiary());
            }
        }
    };
    //修改日记
    Runnable diaryEditSave = new Runnable() {

        @Override
        public void run() {
            //创建一个字符串来存放Json字符串
            String jsonStr = null;
            //从"diaryCache"中读取key为"diaryNew"的值，如果缓存不为空时
            if(!(jsonStr = dt.load(context, "diaryCache", "diaryNew")).equals("")){
                String title = null;
                //把Json字符串转化为List，读取日记缓存列表
                diaryList = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr));
                //获取缓存的日记对象
                diary = diaryList.get(0);
                //如果日记对象的标题为空，给他一个默认的标题：日记+日记篇数
                if(et_title.getText().toString().equals("")){
                    title = "日记" + size;
                }else{
                    title = et_title.getText().toString();
                }
                //给日记对象赋值
                diary.setTitle(title);
                diary.setDiary(et_diary.getText().toString());
                //把新内容修改到日记列表对象
                diaryList.set(0, diary);
                //把日记列表对象保存到"diaryCache"里，key为"diaryNew"
                dt.save(context, "diaryCache", "diaryNew", dt.listToJsonArray(diaryList));
                Log.v("jsonStr", dt.load(context, "diaryCache", "diaryNew"));
            }else{  //从"diaryCache"中读取key为"diaryNew"的值，如果缓存为空时
                //新建一个日记列表
                diaryList = new ArrayList<>();
                //给一个日记对象赋值
                diary.setTitle(et_title.getText().toString());
                diary.setDiary(et_diary.getText().toString());
                //把日记对象添加到日记列表
                diaryList.add(diary);
                //把日记列表对象保存到"diaryCache"里，key为"diaryNew"
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
                    //遍历日记列表
                    for(int i = 0;i < diaryList.size();i++){
                        //通过比较日记缓存列表和日记列表的日记对象的id，确定修改的是那一篇日记
                        if(diaryList.get(i).getId() == diary.getId()){
                            diaryList.set(i, diary);
                            break;
                        }
                    }
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
