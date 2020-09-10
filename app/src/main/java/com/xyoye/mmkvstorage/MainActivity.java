package com.xyoye.mmkvstorage;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.xyoye.mmkv.MMKVStorage;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //获取数据，得到默认值3
        int intData = MMKVStorage.getMmkvInt();
        Log.d(TAG, "默认值：" + intData);

        //更新数据
        MMKVStorage.putMmkvInt(123);
        Log.d(TAG, "更新数据：123");

        //获取到新的数据为123
        int newIntData = MMKVStorage.getMmkvInt();
        Log.d(TAG, "获取到的新数据：" + newIntData);
    }
}