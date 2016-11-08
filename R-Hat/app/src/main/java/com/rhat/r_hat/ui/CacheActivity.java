package com.rhat.r_hat.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //隐藏标题栏
        getSupportActionBar().hide();

        setContentView(R.layout.activity_update);

        init();

        //获取日记
        Log.v("debug", "cache");
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
                // 开启一个子线程，进行本地储存日记，等待有返回结果，使用handler通知UI
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
                // 开启一个子线程，进行本地储存日记，等待有返回结果，使用handler通知UI
                new Thread(diaryEditSave).start();
            }
        });

    }

    private void init(){
        //获取控件对象
        context = getApplicationContext();
        et_title = (EditText) findViewById(R.id.update_et_title);
        et_diary = (EditText) findViewById(R.id.update_et_diary);
        imgbtn_save = (ImageButton) findViewById(R.id.new_imgbtn_save);
    }

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
            // TODO
            String jsonStr = null;
            if(!(jsonStr = dt.load(context, "diaryInfo", "diaryList")).equals("")){
                //把Json字符串转化为List
                diaryList = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr));
                size = diaryList.size();
            }
            if(!(jsonStr = dt.load(context, "diaryCache", "diaryNew")).equals("")){
                //把Json字符串转化为List
                diaryList = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr));
                diary = diaryList.get(0);
                et_title.setText(diary.getTitle());
                et_diary.setText(diary.getDiary());
            }
        }
    };
    //修改日记
    Runnable diaryEditSave = new Runnable() {

        @Override
        public void run() {
            String jsonStr = null;
            if(!(jsonStr = dt.load(context, "diaryCache", "diaryNew")).equals("")){
                String title = null;
                //把Json字符串转化为List
                diaryList = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr));
                diary = diaryList.get(0);
                if(et_title.getText().toString().equals("")){
                    title = "日记" + size;
                }else{
                    title = et_title.getText().toString();
                }
                diary.setTitle(title);
                diary.setDiary(et_diary.getText().toString());
                diaryList.set(0, diary);
                dt.save(context, "diaryCache", "diaryNew", dt.listToJsonArray(diaryList));
                Log.v("jsonStr", dt.load(context, "diaryCache", "diaryNew"));
            }else{
                //新建一个日记列表
                diaryList = new ArrayList<>();
                diary.setTitle(et_title.getText().toString());
                diary.setDiary(et_diary.getText().toString());
                diaryList.add(diary);
                dt.save(context, "diaryCache", "diaryNew", dt.listToJsonArray(diaryList));
                Log.v("jsonStr", dt.load(context, "diaryCache", "diaryNew"));
            }
        }
    };
    //保存日记
    Runnable diaryLocalSave = new Runnable() {

        @Override
        public void run() {
            // TODO
            String value = "save_1";
            if(et_title.getText().toString().equals("") && et_diary.getText().toString().equals("")){
                value = "save_0";
            }else {
                //本地保存日记
                String jsonStr1 = null;
                String jsonStr2 = null;
                if (!(jsonStr1 = dt.load(context, "diaryCache", "diaryNew")).equals("")) {
                    //把Json字符串转化为List
                    List<Diary> list = new ArrayList<Diary>();
                    list = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr1));
                    jsonStr2 = dt.load(context, "diaryInfo", "diaryList");
                    diaryList = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr2));
                    diary = list.get(0);
                    for(int i = 0;i < diaryList.size();i++){
                        if(diaryList.get(i).getId() == diary.getId()){
                            diaryList.set(i, diary);
                            break;
                        }
                    }
                    dt.save(context, "diaryInfo", "diaryList", dt.listToJsonArray(diaryList));
                    dt.dalete(context, "diaryCache", "diaryNew");
                    Log.v("jsonStr", dt.load(context, "diaryInfo", "diaryList"));
                }
            }
            //向Handle返回消息
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("result", value);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

}
