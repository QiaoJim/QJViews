package com.qiaojim.qjviews.QJExpandedListView;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Author: QiaoJim
 * Date:  2018/1/14
 * Email: qiaojim@qq.com
 * Desc:
 */
public class QJHeaderView extends LinearLayout {

    private Context context;
    private ProgressBar progressBar;
    private TextView textView;

    public QJHeaderView(Context context) {
        super(context);
        initFields(context, null);
    }

    public QJHeaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initFields(context, attrs);
    }

    public QJHeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initFields(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
//        initChildViews();
    }

    /*
    * 添加子view*/
    private void initChildViews() {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);

        addProgressBar();
        addTextView();
    }

    private void addTextView() {
        textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        addView(textView);
    }

    private void addProgressBar() {
        progressBar = new ProgressBar(context);
        LayoutParams params = new LayoutParams(50, 50);
        params.rightMargin = dp2pix(context, 8);
        progressBar.setLayoutParams(params);
        stopAnimation();
        addView(progressBar);
    }

    /*
    * 初始化成员变量*/
    private void initFields(Context context, AttributeSet attributeSet) {
        this.context = context;

        initChildViews();
    }

    public void setText(String text) {
        textView.setText(text);
    }

    public void setTextColor(int color) {
        textView.setTextColor(color);
    }

    public void setHeaderBackgroundColor(int color) {
        setBackgroundColor(color);
    }

    public void setTextSize(float size) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
    }

    public void startAnimation() {
        progressBar.setVisibility(VISIBLE);
    }

    public void stopAnimation() {
        progressBar.setVisibility(GONE);
    }

    public void clear() {
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = 0;
        setLayoutParams(params);
        stopAnimation();
    }

    private int dp2pix(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public void setBarSize(int refreshBarSize) {
        LayoutParams params = new LayoutParams(refreshBarSize, refreshBarSize);
        params.rightMargin = dp2pix(context, 8);
        progressBar.setLayoutParams(params);
    }
}
