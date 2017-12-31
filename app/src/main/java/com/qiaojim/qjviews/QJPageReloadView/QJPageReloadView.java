package com.qiaojim.qjviews.QJPageReloadView;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: QiaoJim
 * Date:  2017/12/31
 * Email: qiaojim@qq.com
 * Desc:
 */
public class QJPageReloadView extends LinearLayout {

    private final String TAG = "QJPageReloadView";
    // 竖直方向滑动的最小的y轴分辨率
    private final int MIN_DELTA_Y = 4;

    private Context context;

    private TextView headerView;
    private ListView listView;
    private TextView footerView;

    //touch 事件的坐标
    private float preX;
    private float preY;
    private float curX;
    private float curY;

    //记录touch事件的类型，上滑或下拉
    private boolean moveDown = false;
    private boolean moveUp = false;
    private int curAction = QJViewAction.ACTION_UNDEFINED;

    public QJPageReloadView(Context context) {
        super(context);
        initFields(context, null);
    }

    public QJPageReloadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initFields(context, attrs);
    }

    public QJPageReloadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initFields(context, attrs);
    }

    private void initFields(Context context, AttributeSet attrs) {
        this.context = context;
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initChildView();
    }

    /*实例子view*/
    private void initChildView() {
        headerView = (TextView) getChildAt(0);
        listView = (ListView) getChildAt(1);
        footerView = (TextView) getChildAt(2);

        headerView.setText("header");
        footerView.setText("footer");

        List<String> dataList = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            dataList.add("" + i);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_expandable_list_item_1,
                dataList);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }

    /*
    * 1.列表到达顶部，且继续下拉，则拦截事件，加载下拉刷新view，返回true
    * 2.列表到达底部，且继续上滑，则拦截事件，加载加载更多view，返回true
    * 3.其他不拦截，给子listview处理。返回false，交给子 view 的 dispatchTouchEvent()*/
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercept = false;
        boolean listViewScrolling = false;

        /*
        * 滑动事件判定*/
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                curX = preX = ev.getX();
                curY = preY = ev.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                listViewScrolling = true;
                curX = ev.getX();
                curY = ev.getY();

                if (curY - preY > MIN_DELTA_Y)
                    moveDown = true;
                else if (preY - curY > MIN_DELTA_Y)
                    moveUp = true;

                break;
        }
        Log.e(TAG, "======== onInterceptTouchEvent ==========\n下滑手势：" + moveDown + "        上滑手势：" + moveUp);


        if (lisViewArriveTop() && moveDown){
            curAction = QJViewAction.ACTION_REFRESH;
            intercept = true;
        }
        else if (listViewArriveBottom() && moveUp){
            curAction = QJViewAction.ACTION_LOAD_MORE;
            intercept = true;
        }else {
            curAction=QJViewAction.ACTION_UNDEFINED;
            if (listViewScrolling)
                resetHeaderAndFooterView();
        }


        Log.e(TAG, "======== onInterceptTouchEvent ==========\n拦截：" + intercept);
        // 重置上滑和下拉标记
        resetActionTag();
        // 改变pre的 X，Y 坐标
        resetPreCoordinate();

        return intercept;
    }

    private void resetHeaderAndFooterView() {
        headerView.setText("header");
        footerView.setText("footer");
    }

    private void resetPreCoordinate() {
        preX = curX;
        preY = curY;
    }

    private void resetActionTag() {
        moveDown = moveUp = false;
    }

    /*
    * 1.列表到达顶部，处理下滑
    * 2.列表到达底部，处理上滑*/
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                break;
            case MotionEvent.ACTION_MOVE:

                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                break;
        }

        if (curAction==QJViewAction.ACTION_REFRESH) {
            initRefreshView();
            handled = true;
        } else if (curAction==QJViewAction.ACTION_LOAD_MORE) {
            initLoadMoreView();
            handled = true;
        }

        return handled;
    }

    /*
    * 显示下拉刷新view*/
    private void initRefreshView() {
        headerView.setText("下拉刷新");
    }

    /*
    * 显示加载更多view*/
    private void initLoadMoreView() {
        footerView.setText("加载更多");
    }

    /*
    * 列表是否到达顶部*/
    private boolean lisViewArriveTop() {
        boolean atTop = false;
        if (listView != null) {
            View firstVisibleItemView = listView.getChildAt(0);
            if (firstVisibleItemView != null && firstVisibleItemView.getTop() == 0) {
                atTop = true;
            }
        }

//        Log.e(TAG, "======== lisViewArriveTop ==========\n到达顶部：" + atTop);
        return atTop;
    }

    /*
    * 列表是否到达底部*/
    private boolean listViewArriveBottom() {
        boolean atBottom = false;
        if (listView != null) {
            View lastVisibleItemView = listView.getChildAt(listView.getChildCount() - 1);
            if (lastVisibleItemView != null && lastVisibleItemView.getBottom() == listView.getHeight()) {
                atBottom = true;
            }
        }

//        Log.e(TAG, "======== lisViewArriveTop ==========\n到达底部：" + atBottom);
        return atBottom;
    }

    private static class QJViewAction {
        public static int ACTION_UNDEFINED = 0;
        public static int ACTION_REFRESH = 1;
        public static int ACTION_LOAD_MORE= 2;
    }
}
