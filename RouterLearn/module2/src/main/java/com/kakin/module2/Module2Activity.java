package com.kakin.module2;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.kakin.router_annotation.Route;

/**
 * Module2Activity
 * Created by kakin on 2019/7/14.
 */
@Route(path = "/test/module2Activity")
public class Module2Activity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module2);
    }

    public void mainJump(View view) {
        if (BuildConfig.isModule) {
            Toast.makeText(this, "跳轉到app模塊", Toast.LENGTH_SHORT).show();
        } else  {
            Toast.makeText(this, "當前為組件化模式，不支持該功能", Toast.LENGTH_SHORT).show();
        }
    }

    public void module1Jump(View view) {
        if (BuildConfig.isModule) {
            Toast.makeText(this, "跳轉到module1模塊", Toast.LENGTH_SHORT).show();
        } else  {
            Toast.makeText(this, "當前為組件化模式，不支持該功能", Toast.LENGTH_SHORT).show();
        }
    }
}
