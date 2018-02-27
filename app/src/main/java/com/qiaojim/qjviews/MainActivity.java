package com.qiaojim.qjviews;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.qiaojim.qjviews.QJPageReloadView.QJHeaderView;
import com.qiaojim.qjviews.QJPageReloadView.QJPageReloadActivity;

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
                Intent i1 = new Intent(this, QJPageReloadActivity.class);
                startActivity(i1);
                break;
        }
    }
}
