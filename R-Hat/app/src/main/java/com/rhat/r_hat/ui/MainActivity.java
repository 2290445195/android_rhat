package com.rhat.r_hat.ui;

import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;

import com.google.gson.JsonArray;
import com.rhat.r_hat.R;
import com.rhat.r_hat.model.Diary;
import com.rhat.r_hat.tools.DataTools;
import com.rhat.r_hat.tools.HttpUtils;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends ListActivity {
    private DataTools dt = new DataTools();
    private Context context;
    private Diary diary = new Diary();
    private List<Diary> diaryList;
    private Diary diaryCache;
    private Intent intent = new Intent();
    private ListView lv;
    private ImageButton imgbtn_new;
    private ImageButton imgbtn_me;
    private ImageButton imgbtn_sync;
    private SimpleAdapter adapter;
    private RelativeLayout rl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        init();

        //检查是否未保存
        new Thread(checkCache).start();

        /*
        *日记列表
        */
        //定义一个动态数组(应该定义一个getDate()方法)
        ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
        String jsonStr = null;
        if (!(jsonStr = dt.load(context, "diaryInfo", "diaryList")).equals("")) {
            //把Json字符串转化为List
            diaryList = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr));
            for (int i = 0; i < diaryList.size(); i++) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                diary = diaryList.get(i);
                String diaryStr = "";
                int end = 0;
                if (!diary.getDiary().equals("")) {
                    diaryStr = diary.getDiary();
                }
                if (diaryStr.length() >= 10) {
                    end = 10;
                } else {
                    end = diaryStr.length();
                }
                map.put("diary_title", diary.getTitle());
                map.put("diary_date", diary.getDate());
                map.put("diary_str", diaryStr.substring(0, end));
                listItem.add(map);
            }
        }
        //设置适配器
        adapter = new SimpleAdapter(MainActivity.this, listItem, R.layout.item_main, new String[] {"diary_title","diary_date","diary_str"}, new int[] {R.id.item_main_title,R.id.item_main_date,R.id.item_main_str});
        setListAdapter(adapter);

        /*事件监听*/
        imgbtn_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setClass(MainActivity.this, NewActivity.class);
                startActivity(intent);
                finish();
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                intent.setClass(MainActivity.this, DiaryActivity.class);
                intent.putExtra("itemPosition", position);
                startActivity(intent);
                finish();
            }
        });
    }

    protected void init(){
        imgbtn_new = (ImageButton) findViewById(R.id.main_btn_new);
        lv = (ListView) findViewById(android.R.id.list);
        context = getApplicationContext();
    }

    /**对话框**/
    //提示对话框
    private void cacheDlg(){
        Looper.prepare();
        LayoutInflater factory = LayoutInflater.from(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("温馨提示");
        builder.setMessage("您存在未保存的内容，是否继续？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                if(diaryList.isEmpty()){
                    intent.setClass(MainActivity.this, DraftActivity.class);
                    dialog.dismiss();
                    startActivity(intent);
                    finish();
                }else{
                    if (diaryCache.getId() == diaryList.size()) {
                        intent.setClass(MainActivity.this, CacheActivity.class);
                        dialog.dismiss();
                        startActivity(intent);
                        finish();
                    }else{
                        intent.setClass(MainActivity.this, DraftActivity.class);
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
        builder.show();
        Looper.loop();
    }

    /**
     * 子线程
     */
    //检查日记
    Runnable checkCache = new Runnable() {

        @Override
        public void run() {
            // TODO
            String jsonStr = null;
            String type = "";
            if(!(jsonStr = dt.load(context, "diaryCache", "diaryNew")).equals("")){
                //把Json字符串转化为List
                diaryCache = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr)).get(0);
                if(!(jsonStr = dt.load(context, "diaryInfo", "diaryList")).equals("")){
                    //把Json字符串转化为List
                    diaryList = dt.jsonArrayToDiaryList(dt.jsonToJsonArray(jsonStr));
                }else{
                    diaryList = new ArrayList<Diary>();
                }
                cacheDlg();
            }
        }
    };

}
