package com.qiaojim.qjviews.QJPageReloadView;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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
    private BaseAdapter adapter;
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

    //回调接口
    private QJPageReloadViewListener qjPageReloadViewListener;

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

    /*
    * 实例子view*/
    private void initChildView() {
        headerView = (TextView) getChildAt(0);
        listView = (ListView) getChildAt(1);
        footerView = (TextView) getChildAt(2);
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

        //滑动事件判定
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                curX = preX = ev.getX();
                curY = preY = ev.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                curX = ev.getX();
                curY = ev.getY();

                if (curY - preY > MIN_DELTA_Y) {
                    listViewScrolling = true;
                    moveDown = true;
                } else if (preY - curY > MIN_DELTA_Y) {
                    listViewScrolling = true;
                    moveUp = true;
                }

                break;
        }
//        Log.e(TAG, "======== onInterceptTouchEvent ==========\n下滑手势：" + moveDown + "        上滑手势：" + moveUp);

        if (lisViewArriveTop() && moveDown) {
            curAction = QJViewAction.ACTION_REFRESH;
            intercept = true;
        } else if (listViewArriveBottom() && moveUp) {
            curAction = QJViewAction.ACTION_LOAD_MORE;
            intercept = true;
        } else {
            curAction = QJViewAction.ACTION_UNDEFINED;
            if (listViewScrolling)
                resetHeaderAndFooterView();
        }


//        if (intercept) {
//            Log.e(TAG, "======== onInterceptTouchEvent ==========\n拦截：" + intercept);
//        }

        // 重置上滑和下拉标记
        resetActionTag();
        // 改变pre的 X，Y 坐标
        resetPreCoordinate();

        return intercept;
    }

    private void resetHeaderAndFooterView() {
        headerView.setVisibility(GONE);
        footerView.setVisibility(GONE);
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

                if (curAction == QJViewAction.ACTION_REFRESH) {
                    if (qjPageReloadViewListener != null)
                        qjPageReloadViewListener.onRefresh();
                    initRefreshView();
                    handled = true;
                } else if (curAction == QJViewAction.ACTION_LOAD_MORE) {
                    if (qjPageReloadViewListener != null)
                        qjPageReloadViewListener.onLoadMore();
                    initLoadMoreView();
                    handled = true;
                }
                break;
        }

        return handled;
    }

    /*
    * 显示下拉刷新view*/
    private void initRefreshView() {
        headerView.setVisibility(VISIBLE);
        headerView.setText("下拉刷新");
    }

    /*
    * 显示加载更多view*/
    private void initLoadMoreView() {
        footerView.setVisibility(VISIBLE);
        footerView.setText("加载更多");
    }

    /*
    * 列表是否到达顶部*/
    private boolean lisViewArriveTop() {
        boolean atTop = false;
        if (listView != null) {
            atTop = !(listView.getChildCount() > 0 &&
                    (listView.getFirstVisiblePosition() > 0
                    || listView.getChildAt(0).getTop() < listView.getPaddingTop()));
        }

        Log.e(TAG, "======== lisViewArriveTop ==========\n到达顶部：" + atTop+ "     Item数量：" + listView.getAdapter().getCount());
        return atTop;
    }

    /*
    * 列表是否到达底部*/
    private boolean listViewArriveBottom() {
        boolean atBottom = false;
        if (listView != null) {
            int lastChildBottom = listView.getChildAt(listView.getChildCount() - 1).getBottom();
            atBottom = listView.getLastVisiblePosition() == listView.getAdapter().getCount() - 1
                    && lastChildBottom >= listView.getMeasuredHeight();
        }

        Log.e(TAG, "======== lisViewArriveBottom ==========\n到达底部：" + atBottom + "     Item数量：" + listView.getAdapter().getCount());
        return atBottom;
    }

    /*
    * 动作常量*/
    private static class QJViewAction {
        public static int ACTION_UNDEFINED = 0;
        public static int ACTION_REFRESH = 1;
        public static int ACTION_LOAD_MORE = 2;
    }

    /*
    * 回调接口定义*/
    public interface QJPageReloadViewListener {
        /*
        * 下拉刷新回调*/
        void onRefresh();

        /*
        * 加载更多回调*/
        void onLoadMore();

    }

    /**
     * 设置回调接口
     *
     * @param listener
     */
    public void setQJPageReloadViewListener(QJPageReloadViewListener listener) {
        this.qjPageReloadViewListener = listener;
    }

    /**
     * 设置listview的适配器
     *
     * @param adapter
     */
    public void setAdapter(BaseAdapter adapter) {
        this.adapter = adapter;
        listView.setAdapter(adapter);
    }

    /**
     * 刷新listview的数据
     */
    public void update() {
        adapter.notifyDataSetChanged();
    }
}
