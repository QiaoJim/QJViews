package com.qiaojim.qjviews.QJPageReloadView;

import android.content.Context;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
    // 下拉刷新的最小下拉的高度
    private final int REFRESH_MIN_HEIGHT = 200;

    private Context context;

    private TextView headerView;
    private ListView listView;
    private BaseAdapter adapter;
    private TextView footerView;

    //touch 事件的坐标
    private float preX;
    private float preY;

    //下拉刷新放弃。如下拉后，手动收回
    private boolean cancelRefresh = false;

    //记录touch事件的类型，上滑或下拉
    private boolean moveDown = false;
    private boolean moveUp = false;
    private int curAction = QJViewAction.ACTION_UNDEFINED;

    //回调接口
    private QJPageReloadViewListener qjPageReloadViewListener;

    //自动加载标记
    private boolean autoLoadMore = false;

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

        float curX = 0, curY = 0;

        //滑动事件判定
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                Log.e(TAG, "======== onInterceptTouchEvent ==========\tdown");
                curX = preX = ev.getX();
                curY = preY = ev.getY();
                break;

            case MotionEvent.ACTION_MOVE:
//                Log.e(TAG, "======== onInterceptTouchEvent ==========\tmove");
                curX = ev.getX();
                curY = ev.getY();

                //加载更多view不可见时，全屏幕监听滑动事件
                if (!footerViewVisible()) {
                    if (curY - preY > MIN_DELTA_Y) {
                        listViewScrolling = true;
                        moveDown = true;
                    } else if (preY - curY > MIN_DELTA_Y) {
                        listViewScrolling = true;
                        moveUp = true;
                    }
                }
                // 加载更多view可见时，监听view区域外的滑动事件。
                // 若touch区域在加载更多的view区域，不拦截事件，应该为view的点击事件
                else if (!touchInFooterView(ev)) {
                    if (curY - preY > MIN_DELTA_Y) {
                        listViewScrolling = true;
                        moveDown = true;
                    } else if (preY - curY > MIN_DELTA_Y) {
                        listViewScrolling = true;
                        moveUp = true;
                    }
                }

                break;
        }
//        Log.e(TAG, "======== onInterceptTouchEvent ==========\n下滑手势：" + moveDown + "        上滑手势：" + moveUp);

        if (listViewArriveTop() && moveDown) {
            curAction = QJViewAction.ACTION_REFRESH;
            intercept = true;
        } else if (listViewArriveBottom() && moveUp) {
            curAction = QJViewAction.ACTION_LOAD_MORE;
            intercept = true;
        } else {
            curAction = QJViewAction.ACTION_UNDEFINED;

            if (listViewScrolling) {
                resetHeaderView();
                resetFooterView();
            }
        }


//        if (intercept) {
//            Log.e(TAG, "======== onInterceptTouchEvent ==========\n拦截：" + intercept);
//        }

        // 重置上滑和下拉标记
        resetActionTag();
        // 改变pre的 X，Y 坐标
        resetPreCoordinate(curX, curY);

        return intercept;
    }


    /*
    * 1.列表到达顶部，处理下滑
    * 2.列表到达底部，处理上滑*/
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                Log.e(TAG, "======== onTouchEvent ==========\tdown");
                break;

            case MotionEvent.ACTION_MOVE:
                Log.e(TAG, "======== onTouchEvent ==========\tmove\t\t当前动作：" + curAction);

                //处理滑动事件
                handled = handleMoving(event);
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                Log.e(TAG, "======== onTouchEvent ==========\tup/cancel\t\t当前动作：" + curAction);

                //处理一次完整的过程结束
                handled = handleOnceTouch();
                //重置底部和顶部的view
                resetHeaderView();
                break;
        }

        return handled;
    }

    /*
    * 实例子view*/
    private void initChildView() {
        headerView = (TextView) getChildAt(0);
        listView = (ListView) getChildAt(1);
        footerView = (TextView) getChildAt(2);

        footerView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                qjPageReloadViewListener.onLoadMore();

                //使加载更多view为gone
                resetFooterView();
            }
        });
    }

    /*
    * touch事件是否发生在加载更多的view区域*/
    private boolean touchInFooterView(MotionEvent event) {
        boolean in = false;
        int[] location = new int[2];
        // 获取控件在屏幕中的位置，返回的数组分别为控件左顶点的 x、y 的值
        footerView.getLocationOnScreen(location);
        RectF area = new RectF(location[0], location[1],
                location[0] + footerView.getWidth(),
                location[1] + footerView.getHeight());
        float x = event.getRawX();
        float y = event.getRawY();

        return area.contains(x, y);
    }

    /*
    * 加载更多view是否可见*/
    private boolean footerViewVisible() {
        boolean visible = false;
        if (footerView.getVisibility() == VISIBLE) {
            visible = true;
        }
        return visible;
    }

    /*
    * 重置顶部下拉刷新的view：
    * 1.下拉刷新手指抬起*/
    private void resetHeaderView() {
        //设置下拉刷新view的高度=0
        ViewGroup.LayoutParams params = headerView.getLayoutParams();
        params.height = 0;
        headerView.setLayoutParams(params);
    }

    /*
    * 重置底部加载更多view
    * 1.可见时，列表往上翻，即moveDown动作*/
    private void resetFooterView() {
        if (footerViewVisible()) {
            footerView.setVisibility(GONE);
        }
    }

    /*
    * 记录之前的坐标*/
    private void resetPreCoordinate(float curX, float curY) {
        preX = curX;
        preY = curY;
    }

    private void resetActionTag() {
        moveDown = moveUp = false;
    }

    /*
    * 处理滑动事件，调节view的位置*/
    private boolean handleMoving(MotionEvent event) {

        float curX = event.getX();
        float curY = event.getY();
        float deltaY = curY - preY;

        //当前动作为下拉刷新时，才操作下拉刷新的view
        if (curAction == QJViewAction.ACTION_REFRESH) {
            //测量加载更多view的高度
            int headerViewHeight = measureHeaderViewHeight(deltaY);
            //记录是否放弃下拉刷新操作
            cancelRefresh = headerViewHeight < REFRESH_MIN_HEIGHT;
        }

//        Log.e(TAG, "y间距：" + deltaY + "        最终测量view的高度：" + headerViewHeight);

        resetPreCoordinate(curX, curY);
        return true;
    }

    /*
    * 调整headerview的高度*/
    private int measureHeaderViewHeight(float deltaY) {
        //下拉刷新的height
        ViewGroup.LayoutParams layoutParams = headerView.getLayoutParams();
        layoutParams.height += deltaY;

        int finalHeight = layoutParams.height;
        if (finalHeight < 0)
            finalHeight = 0;
        else if (finalHeight > 600)
            finalHeight = 600;  //最大高度

        if (finalHeight >= REFRESH_MIN_HEIGHT)
            headerView.setText("释放立即刷新");
        else
            headerView.setText("下拉刷新");


        //重置下拉刷新view的高度
        layoutParams.height = finalHeight;
        headerView.setLayoutParams(layoutParams);
        return finalHeight;
    }

    /*
    * 处理拦截下来的一次touch事件*/
    private boolean handleOnceTouch() {

        boolean handled = false;

        //回调接口不为null，确定回调函数
        if (curAction == QJViewAction.ACTION_REFRESH) {
            if (qjPageReloadViewListener != null) {
                //没有放弃下拉刷新动作。若手动滑上去，则判定为不刷新
                if (!cancelRefresh) {
                    qjPageReloadViewListener.onRefresh();
                }
            }
            handled = true;
        } else if (curAction == QJViewAction.ACTION_LOAD_MORE) {

            if (qjPageReloadViewListener != null) {
                if (autoLoadMore)
                    qjPageReloadViewListener.onAutoLoadMore();
                else
                    initLoadMoreView();
            }
            handled = true;
        }
        return handled;
    }

    /*
    * 显示加载更多view*/
    private void initLoadMoreView() {
        footerView.setVisibility(VISIBLE);
        footerView.setText("加载更多");
    }

    /*
    * 列表是否到达顶部*/
    private boolean listViewArriveTop() {
        boolean atTop = false;
        if (listView != null) {
            atTop = !(listView.getChildCount() > 0 &&
                    (listView.getFirstVisiblePosition() > 0
                            || listView.getChildAt(0).getTop() < listView.getPaddingTop()));
        }
        return atTop;
    }

    /*
    * 列表是否到达底部*/
    private boolean listViewArriveBottom() {
        boolean atBottom = false;
        if (listView != null) {
            int lastChildBottom = listView.getChildAt(listView.getChildCount() - 1).getBottom();
            atBottom = listView.getChildCount() > 0
                    && listView.getLastVisiblePosition() == listView.getAdapter().getCount() - 1
                    && lastChildBottom >= listView.getMeasuredHeight();
        }

//        Log.e(TAG, "======== lisViewArriveBottom ==========\n到达底部：" + atBottom + "     " +
//                "Item数量：" + listView.getAdapter().getCount());
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

        /*
        * 自动加载更多*/
        void onAutoLoadMore();

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

    /**
     * 设置是否开启滑到底部自动加载
     *
     * @param auto
     */
    public void setAutoLoadMore(boolean auto) {
        this.autoLoadMore = auto;
    }
}
