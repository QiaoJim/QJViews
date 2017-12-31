package com.qiaojim.qjviews.QJPageReloadView;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.qiaojim.qjviews.R;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class QJPageReloadActivity extends AppCompatActivity {

    private final String TAG="QJPageReloadActivity";

    private QJPageReloadView qjPageReloadView;
    private QJPageReloadView.QJPageReloadViewListener listener;
    private LinkedList<String> dataList = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qj_page_reload);

        initListener();
        initView();
    }

    private void initListener() {
        listener = new QJPageReloadView.QJPageReloadViewListener() {
            @Override
            public void onRefresh() {
                Log.e(TAG, "======== onRefresh()回调 ==========");

                for (int i = -1; i > -11; i--) {
                    dataList.addFirst("" + i);
                }
                qjPageReloadView.update();
            }

            @Override
            public void onLoadMore() {
                Log.e(TAG, "======== onLoadMore()回调 ==========");

                for (int i = 50; i < 60; i++) {
                    dataList.add("" + i);
                }
                qjPageReloadView.update();
                qjPageReloadView.listViewArriveBottom();
            }
        };
    }

    private void initView() {

        for (int i = 0; i < 50; i++) {
            dataList.add("" + i);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_expandable_list_item_1,
                dataList);

        qjPageReloadView = findViewById(R.id.qj_page_reload_view);
        qjPageReloadView.setAdapter(adapter);
        qjPageReloadView.setQJPageReloadViewListener(listener);

    }
}
