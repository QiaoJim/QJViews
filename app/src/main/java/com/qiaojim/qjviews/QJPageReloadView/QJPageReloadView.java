package com.qiaojim.qjviews.QJPageReloadView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.qiaojim.qjviews.R;

import java.lang.ref.WeakReference;

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
    // 字体大小
    private final int DEFAULT_TEXT_SIZE = 40;

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

    //是否正在加载ing，是的话屏蔽后续加载回调
    private boolean loading = false;

    //自定义属性值
    private int headerViewBgdColor;
    private int footerViewBgdColor;
    private int headerViewTextColor;
    private int footerViewTextColor;
    private int headerViewTextSize;
    private int footerViewTextSize;
    private int refreshMinHeight = -1;
    private int refreshMaxHeight = -1;
    private boolean refreshEnable = false;
    private boolean loadMoreEnable = false;
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

    /*
    * 初始化成员变量，标记和自定义属性值*/
    private void initFields(Context context, AttributeSet attrs) {
        this.context = context;

        int resId;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.QJPageReloadView);

        //背景颜色
        resId = typedArray.getResourceId(R.styleable.QJPageReloadView_header_view_bgd_color, -1);
        if (resId == -1)
            headerViewBgdColor = loadColor(R.color.white);
        else
            headerViewBgdColor = loadColor(resId);
        resId = typedArray.getResourceId(R.styleable.QJPageReloadView_footer_view_bgd_color, -1);
        if (resId == -1)
            footerViewBgdColor = loadColor(R.color.white);
        else
            footerViewBgdColor = loadColor(resId);

        //文字大小
        headerViewTextSize = typedArray.getDimensionPixelSize(R.styleable.QJPageReloadView_header_view_text_size, DEFAULT_TEXT_SIZE);
        footerViewTextSize = typedArray.getDimensionPixelSize(R.styleable.QJPageReloadView_footer_view_text_size, DEFAULT_TEXT_SIZE);
        //调整字体大小
        if (headerViewTextSize < 30 || headerViewTextSize > 90)
            headerViewTextSize = DEFAULT_TEXT_SIZE;
        if (footerViewTextSize < 30 || footerViewTextSize > 90)
            footerViewTextSize = DEFAULT_TEXT_SIZE;

        //文字颜色
        resId = typedArray.getResourceId(R.styleable.QJPageReloadView_header_view_text_color, -1);
        if (resId == -1)
            headerViewTextColor = loadColor(R.color.black);
        else
            headerViewTextColor = loadColor(resId);
        resId = typedArray.getResourceId(R.styleable.QJPageReloadView_footer_view_text_color, -1);
        if (resId == -1)
            footerViewTextColor = loadColor(R.color.black);
        else
            footerViewTextColor = loadColor(resId);


        //下拉刷新的开启与否
        refreshEnable = typedArray.getBoolean(R.styleable.QJPageReloadView_refresh_enable, true);
        if (refreshEnable) {
            //下拉最小、最大的高度
            refreshMinHeight = typedArray.getDimensionPixelSize(R.styleable.QJPageReloadView_refresh_min_height,
                    REFRESH_MIN_HEIGHT);
            refreshMaxHeight = typedArray.getDimensionPixelSize(R.styleable.QJPageReloadView_refresh_max_height,
                    (int) (getScreenHeight() * 0.7));

            //调整高度大小
            if (refreshMinHeight < 120)
                refreshMinHeight = REFRESH_MIN_HEIGHT;
            else if (refreshMinHeight > (int) (getScreenHeight() * 0.35))
                refreshMinHeight = (int) (getScreenHeight() * 0.35);

            if (refreshMaxHeight > (int) (getScreenHeight() * 0.7))
                refreshMaxHeight = (int) (getScreenHeight() * 0.7);

            if (refreshMinHeight > refreshMaxHeight)
                refreshMaxHeight = 2 * refreshMinHeight;
        }

        //加载更多开启与否
        loadMoreEnable = typedArray.getBoolean(R.styleable.QJPageReloadView_load_more_enable, true);
        if (loadMoreEnable)
            autoLoadMore = typedArray.getBoolean(R.styleable.QJPageReloadView_load_more_enable, false);

        typedArray.recycle();

//        Log.e(TAG, "文字大小\t" + headerViewTextSize + "\t" + footerViewTextSize);
//        Log.e(TAG, "文字颜色\t" + headerViewTextColor + "\t" + footerViewTextColor);
//        Log.e(TAG, "背景颜色\t" + headerViewBgdColor + "\t" + footerViewBgdColor);
//        Log.e(TAG, "下拉刷新启用\t" + refreshEnable + "\t");
//        Log.e(TAG, "下拉高度\t" + refreshMinHeight + "\t" + refreshMaxHeight);
//        Log.e(TAG, "加载更多启用\t" + loadMoreEnable + "\t");
//        Log.e(TAG, "自动加载更多启用\t" + autoLoadMore);
    }

    /*
    * 获取屏幕高度，防止下拉view的高度过大*/
    private int getScreenHeight() {
        return context.getResources().getDisplayMetrics().heightPixels;
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

        if (listViewArriveTop() && moveDown && !loading && refreshEnable) {
            curAction = QJViewAction.ACTION_REFRESH;
            intercept = true;
        } else if (listViewArriveBottom() && moveUp && !loading && loadMoreEnable) {
            curAction = QJViewAction.ACTION_LOAD_MORE;
            intercept = true;
        } else {
            curAction = QJViewAction.ACTION_UNDEFINED;

            if (listViewScrolling && !loading)
                resetFooterView(QJViewState.CLEAR);
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
//                Log.e(TAG, "======== onTouchEvent ==========\tmove\t\t当前动作：" + curAction);

                //处理滑动事件
                handled = handleMoving(event);
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
//                Log.e(TAG, "======== onTouchEvent ==========\tup/cancel\t\t当前动作：" + curAction);

                //处理一次完整的过程结束
                handled = handleOnceTouch();

                //重置顶部的view
                if (listViewArriveTop() && loading)
                    resetHeaderView(QJViewState.LOADING);
                else
                    resetHeaderView(QJViewState.CLEAR);

                break;
        }

        return handled;
    }

    /*
    * 实例子view，依次添加
    * 1.顶部下拉刷新view
    * 2.中间的listview
    * 3.底部加载更多view*/
    private void initChildView() {

        setOrientation(VERTICAL);
        addHeaderView();
        addListView();
        addFooterView();

    }

    private void addHeaderView() {
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        params.bottomMargin = dp2pix(context, 3);
        headerView = new TextView(context);
        headerView.setGravity(Gravity.CENTER);
        headerView.setTextColor(headerViewTextColor);
        headerView.setTextSize(TypedValue.COMPLEX_UNIT_PX, headerViewTextSize);
        headerView.setBackgroundColor(headerViewBgdColor);
        addView(headerView, params);
    }

    private void addListView() {
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        listView = new ListView(context);
        listView.setBackgroundColor(context.getResources().getColor(R.color.white));
        addView(listView, params);
    }

    private void addFooterView() {
        int footerViewHeight = dp2pix(context, 45);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, footerViewHeight);
        params.topMargin = dp2pix(context, 3);
        footerView = new TextView(context);
        footerView.setGravity(Gravity.CENTER);
        footerView.setTextColor(footerViewTextColor);
        footerView.setTextSize(TypedValue.COMPLEX_UNIT_PX, footerViewTextSize);
        footerView.setBackgroundColor(footerViewBgdColor);
        footerView.setVisibility(GONE);
        addView(footerView, params);

        footerView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!loading) {
                    qjPageReloadViewListener.onStart();
                    //异步加载更多
                    QJReloadTask.newInstance(QJPageReloadView.this).execute(QJViewAction.ACTION_LOAD_MORE);
                    loading = true;
                    //使加载更多view为gone
                    resetFooterView(QJViewState.LOADING);
                }
            }
        });
    }

    /*
    * 处理拦截下来的一次touch事件*/
    private boolean handleOnceTouch() {

        boolean handled = false;

        //回调接口不为null，确定回调函数
        if (curAction == QJViewAction.ACTION_REFRESH) {
            if (qjPageReloadViewListener != null) {
                //没有放弃下拉刷新动作。若手动滑上去，则判定为不刷新
                //若已经正在刷新，则屏蔽此次动作
                if (!cancelRefresh && !loading) {
                    qjPageReloadViewListener.onStart();
                    QJReloadTask.newInstance(this).execute(QJViewAction.ACTION_REFRESH);
                    loading = true;
                    resetHeaderView(QJViewState.LOADING);
                }
            }
            handled = true;
        } else if (curAction == QJViewAction.ACTION_LOAD_MORE) {

            if (qjPageReloadViewListener != null) {
                if (autoLoadMore && !loading) {
                    qjPageReloadViewListener.onStart();
                    QJReloadTask.newInstance(this).execute(QJViewAction.ACTION_LOAD_MORE);
                    loading = true;
                    resetFooterView(QJViewState.LOADING);
                } else if (!loading)
                    resetFooterView(QJViewState.CREATE);
            }
            handled = true;
        }
        return handled;
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
    * 下拉刷新view是否可见*/
    private boolean headerViewVisible() {
        boolean visible = false;
        if (headerView.getHeight() > 0) {
            visible = true;
        }
        return visible;
    }

    /*
    * 改变顶部下拉刷新的view*/
    private void resetHeaderView(QJViewState state) {

        if (state.equals(QJViewState.CLEAR)) {
            //重置顶部下拉刷新的view
            //设置下拉刷新view的高度=0
            //1.下拉刷新手指抬起
            ViewGroup.LayoutParams params = headerView.getLayoutParams();
            params.height = 0;
            headerView.setLayoutParams(params);
        } else if (state.equals(QJViewState.LOADING)) {

            headerView.setText("请稍候, 刷新中...");
            ViewGroup.LayoutParams params = headerView.getLayoutParams();
            params.height = refreshMinHeight;
            headerView.setLayoutParams(params);
        } else if (state.equals(QJViewState.CREATE)) {

        }
    }

    /*
    * 改变底部加载更多view*/
    private void resetFooterView(QJViewState state) {
        if (state.equals(QJViewState.CLEAR)) {
            //重置底部加载更多view
            //1.可见时，列表往上翻，即moveDown动作
            if (footerViewVisible()) {
                footerView.setVisibility(GONE);
            }
        } else if (state.equals(QJViewState.LOADING)) {

            if (!footerViewVisible())
                footerView.setVisibility(VISIBLE);
            footerView.setText("请稍候, 加载中...");
            //底部显示view后，将列表设置到底部
            setListToBottom();

        } else if (state.equals(QJViewState.CREATE)) {

            if (!footerViewVisible())
                footerView.setVisibility(VISIBLE);
            if (loading)
                footerView.setText("请稍候, 加载中...");
            else
                footerView.setText("加载更多");

            //底部显示view后，将列表设置到底部
            setListToBottom();
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
            cancelRefresh = headerViewHeight < refreshMinHeight;
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
        else if (finalHeight > refreshMaxHeight)
            finalHeight = refreshMaxHeight;  //最大高度

        if (finalHeight >= refreshMinHeight)
            headerView.setText("释放立即刷新");
        else
            headerView.setText("下拉刷新");


        //重置下拉刷新view的高度
        layoutParams.height = finalHeight;
        headerView.setLayoutParams(layoutParams);
        return finalHeight;
    }

    /*
    * 设置列表到底部*/
    private void setListToBottom() {
        listView.post(new Runnable() {
            @Override
            public void run() {
                listView.setSelection(getTotalCount() - 1);
            }
        });
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

        return atBottom;
    }

    /*
    * pix转dp*/
    private int pix2dp(Context context, float pix) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pix / scale + 0.5f);
    }

    /*
    * dp转pix*/
    private int dp2pix(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public int pix2sp(Context context, float pix) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pix / fontScale + 0.5f);
    }

    public int sp2pix(Context context, float sp) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (sp * fontScale + 0.5f);
    }

    /*
    * 加载颜色资源*/
    private int loadColor(int resId) {
        return context.getResources().getColor(resId);
    }

    /*
    * 加载字符串资源*/
    private String loadString(int resId) {
        return context.getResources().getString(resId);
    }

    /*
    * 加载尺寸资源*/
    private float loadDimension(int resId) {
        return context.getResources().getDimension(resId);
    }

    /*
    * 动作常量*/
    private static class QJViewAction {
        public static int ACTION_UNDEFINED = 0;
        public static int ACTION_REFRESH = 1;
        public static int ACTION_LOAD_MORE = 2;
    }

    /*
    * 底部和顶部view的状态*/
    private enum QJViewState {
        LOADING, CLEAR, CREATE
    }

    /*
    * 回调接口定义*/
    public interface QJPageReloadViewListener {

        /*
        * 加载任务开始前回调，UI线程，可操作view*/
        void onStart();

        /*
        * 下拉刷新回调，非UI线程，可执行耗时操作*/
        boolean onRefresh(int totalCount);

        /*
        * 加载更多回调，非UI线程，可执行耗时操作*/
        boolean onLoadMore(int totalCount);

        /*
        * 刷新或加载后马上完成，UI线程，可操作view*/
        void onFinished();

    }

    /*
    * 异步加载任务*/
    private static class QJReloadTask extends AsyncTask<Integer, Integer, Boolean> {

        private WeakReference<QJPageReloadView> viewWeakReference = null;

        public static QJReloadTask newInstance(QJPageReloadView view) {
            return new QJReloadTask(view);
        }

        private QJReloadTask(QJPageReloadView view) {
            this.viewWeakReference = new WeakReference<QJPageReloadView>(view);
        }

        @Override
        protected Boolean doInBackground(Integer... integers) {
            int action = integers[0];
            boolean ret = false;

            QJPageReloadView view = viewWeakReference.get();
            QJPageReloadViewListener qjPageReloadViewListener = view.getQjPageReloadViewListener();
            int count = view.getTotalCount();

            if (action == QJViewAction.ACTION_REFRESH) {
                ret = qjPageReloadViewListener.onRefresh(count);
            } else if (action == QJViewAction.ACTION_LOAD_MORE) {
                ret = qjPageReloadViewListener.onLoadMore(count);
            }

            return ret;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                QJPageReloadView view = viewWeakReference.get();
                view.setLoading(false);
                view.resetHeaderView(QJViewState.CLEAR);
                view.resetFooterView(QJViewState.CLEAR);

                //UI线程回调onFinish()
                QJPageReloadViewListener qjPageReloadViewListener = view.getQjPageReloadViewListener();
                qjPageReloadViewListener.onFinished();

            }
        }
    }

    /*
    * 设置正在加载的标记*/
    private void setLoading(boolean loading) {
        this.loading = loading;
    }

    /**
     * 设置回调接口
     *
     * @param listener
     */
    public void setQJPageReloadViewListener(QJPageReloadViewListener listener) {
        this.qjPageReloadViewListener = listener;
    }

    public QJPageReloadViewListener getQjPageReloadViewListener() {
        return qjPageReloadViewListener;
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

    /**
     * 获取list中所有元素的数量
     *
     * @return
     */
    public int getTotalCount() {
        return adapter.getCount();
    }

}
