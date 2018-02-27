package com.qiaojim.qjviews;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.qiaojim.qjviews.QJExpandedListView.QJExpandedListViewActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        Button qjPageReloadView = findViewById(R.id.qj_page_reload_view);
        qjPageReloadView.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.qj_page_reload_view:
                Intent i1 = new Intent(this, QJExpandedListViewActivity.class);
                startActivity(i1);
                break;
        }
    }
}
