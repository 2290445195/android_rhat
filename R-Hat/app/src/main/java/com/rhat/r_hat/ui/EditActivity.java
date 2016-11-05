package com.rhat.r_hat.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;

import com.rhat.r_hat.R;

public class EditActivity extends AppCompatActivity {
    private ListView lv;
    private ImageButton imgbtn_new;
    private ImageButton imgbtn_me;
    private ImageButton imgbtn_sync;
    private SimpleAdapter adapter;
    private RelativeLayout rl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //隐藏标题栏
        getSupportActionBar().hide();

        setContentView(R.layout.activity_update);

    }

}
