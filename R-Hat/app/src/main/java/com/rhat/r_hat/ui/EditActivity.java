package com.rhat.r_hat.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.rhat.r_hat.R;
import com.rhat.r_hat.model.Diary;
import com.rhat.r_hat.tools.DataTools;
import com.rhat.r_hat.view.MyScrollView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EditActivity extends AppCompatActivity {
    private Intent intent;
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

    }

    private void init(){
        //获取控件对象
        context = getApplicationContext();
        intent = getIntent();
        id = intent.getIntExtra("id", -1);
        et_title = (EditText) findViewById(R.id.update_et_title);
        et_diary = (EditText) findViewById(R.id.update_et_diary);
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
            // TODO
            //本地保存日记
            String jsonStr = null;
            if(!(jsonStr = dt.load(context, "diaryInfo", "diaryList")).equals("")){
                //把Json字符串转化为List
                diaryList = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr));
                for(int i = 0;i < diaryList.size();i++){
                    if((diary = diaryList.get(i)).getId() == id){
                        break;
                    }
                }
                et_title.setText(diary.getTitle());
                et_diary.setText(diary.getDiary());
            }
        }
    };
    //添加日记
    Runnable diaryNewSave = new Runnable() {

        @Override
        public void run() {
            String jsonStr = null;
            Date date = new Date();
            int id = 1;
            int size = 0;
            if(!(jsonStr = dt.load(context, "diaryCache", "diaryNew")).equals("")){
                //把Json字符串转化为List
                diaryList = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr));
                diary = diaryList.get(0);
                //最后一篇日记ID加1
                diary.setId(id);
                String title = null;
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
                diary.setId(id);
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
                    if (!(jsonStr2 = dt.load(context, "diaryInfo", "diaryList")).equals("")) {
                        diaryList = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr2));
                        diary = list.get(0);
                        diaryList.add(diary);
                        dt.save(context, "diaryInfo", "diaryList", dt.listToJsonArray(diaryList));
                        dt.dalete(context, "diaryCache", "diaryNew");
                        Log.v("jsonStr", dt.load(context, "diaryInfo", "diaryList"));
                    } else {
                        diaryList = new ArrayList<Diary>();
                        diary = list.get(0);
                        diaryList.add(diary);
                        dt.save(context, "diaryInfo", "diaryList", dt.listToJsonArray(diaryList));
                        dt.dalete(context, "diaryCache", "diaryNew");
                        Log.v("jsonStr", dt.load(context, "diaryInfo", "diaryList"));
                    }
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
