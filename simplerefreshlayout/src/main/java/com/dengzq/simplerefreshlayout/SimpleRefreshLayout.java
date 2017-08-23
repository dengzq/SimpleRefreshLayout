package com.dengzq.simplerefreshlayout;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.Scroller;

/**
 * <p>公司   tsingning</p>
 * <p>作者   dengzq</p>
 * <p>时间   2017/6/22 09:52</p>
 * <p>包名   com.dengzq.dengzqtest.widgets.simple_refresh</p>
 * <p>描述   1.支持刷新，加载，无更多布局的ViewGroup
 *          2.支持coordinatorLayout，可适配继承NestedScrollParent的父组件
 *          3.支持listView等其他控件
 *          4.支持加载布局自定义
 * </p>
 */

public class SimpleRefreshLayout extends ViewGroup implements NestedScrollingParent, NestedScrollingChild {

    private static final String TAG                             = "SimpleRefreshLayout";
    private static final int    MSG_PULL_UP                     = 100;
    private static final int    MSG_DOWN_RESET                  = 101;
    private static final int    MSG_UP_RESET                    = 102;
    private static final int    MSG_NO_MORE                     = 103;
    private static final int    SCROLL_NONE                     = -1; //无滚动
    private static final int    SCROLL_UP                       = 0;  //下拉(currY>lastY)
    private static final int    SCROLL_DOWN                     = 1;  //上拉(currY<lastY)
    private static final float  DECELERATE_INTERPOLATION_FACTOR = 2F; //滑动阻尼因子
    private static       int    ANIMATION_EXTEND_DURATION       = 200;
    private              int    childHeaderHeight               = 150;
    private              int    childFooterHeight               = 150;
    private              int    childBottomHeight               = 120;

    private NestedScrollingParentHelper mNestedScrollingParentHelper;
    private NestedScrollingChildHelper  mNestedScrollingChildHelper;

    private final int[] mParentScrollConsumed = new int[2];
    private final int[] mParentOffsetInWindow = new int[2];

    private boolean pullDownEnable = true;  //是否允许下拉刷新
    private boolean pullUpEnable   = true;  //是否允许加载更多
    private boolean enable         = true;  //是否允许视图滑动
    private boolean showBottom;             //是否显示无更多
    private boolean isLastScrollComplete;   //是否上一次滑动已结束
    private int     direction;

    private View     mTarget;
    private Scroller mScroller;

    private CanChildScrollDown mCanChildScrollDown;
    private CanChildScrollUp   mCanChildScrollUp;

    private int effectivePullDownRange;
    private int effectivePullUpRange;
    private int ignorePullRange;

    private IHeaderWrapper mHeaderView;
    private IFooterWrapper mFooterView;
    private IBottomWrapper mBottomView;

    private int   currentState;
    private float mLastY;

    private OnSimpleRefreshListener mRefreshListener;

    public SimpleRefreshLayout(Context context) {
        this(context, null);
    }

    public SimpleRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        currentState = State.PULL_DOWN_RESET;
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        mScroller = new Scroller(getContext(), new LinearInterpolator());
        effectivePullDownRange = (int) (getContext().getResources().getDisplayMetrics().density * 80);
        effectivePullUpRange = (int) (getContext().getResources().getDisplayMetrics().density * 45);
        ignorePullRange = (int) (getContext().getResources().getDisplayMetrics().density * 8);

        setNestedScrollingEnabled(true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    //设置刷新布局
    public void setHeaderView(IHeaderWrapper header) {
        this.mHeaderView = header;
        addView((View) mHeaderView);
    }

    public void setHeaderView(IHeaderWrapper header, int height) {
        this.mHeaderView = header;
        this.childHeaderHeight = height;
        addView((View) mHeaderView);
    }

    //设置加载更多布局
    public void setFooterView(IFooterWrapper footer) {
        this.mFooterView = footer;
        addView((View) mFooterView);
    }

    public void setFooterView(IFooterWrapper footer, int height) {
        this.mFooterView = footer;
        this.childFooterHeight = height;
        addView((View) mFooterView);
    }

    //设置加载完成布局
    public void setBottomView(IBottomWrapper bottom) {
        this.mBottomView = bottom;
        addView((View) mBottomView);
    }

    public void setBottomView(IBottomWrapper bottom, int height) {
        this.mBottomView = bottom;
        this.childBottomHeight = height;
        addView((View) mBottomView);
    }

    public void showNoMore(boolean noMore) {
        //Handler是为了让上拉回弹先走完，再显示BottomView;
        this.showBottom = noMore;
        if (showBottom && ((currentState != State.PULL_DOWN_FINISH && currentState != State.PULL_UP_FINISH)
                || getScrollY() != 0)) {
            mHandler.sendEmptyMessageDelayed(MSG_NO_MORE, 5);
            return;
        }
        if (mBottomView != null) ((View) mBottomView).setVisibility(showBottom ? VISIBLE : GONE);
        if (mFooterView != null) ((View) mFooterView).setVisibility(showBottom ? GONE : VISIBLE);
    }

    public void setViewHeight(int height) {
        this.childHeaderHeight = height;
    }

    public void setRefreshAnimationDuration(int duration) {
        ANIMATION_EXTEND_DURATION = duration;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void setEffectivePullDownRange(int effectivePullDownRange) {
        this.effectivePullDownRange = effectivePullDownRange;
    }

    public void setEffectivePullUpRange(int effectivePullUpRange) {
        this.effectivePullUpRange = effectivePullUpRange;
    }

    public void setChildHeaderHeight(int childHeaderHeight) {
        this.childHeaderHeight = childHeaderHeight;
    }

    public void setChildFooterHeight(int childFooterHeight) {
        this.childFooterHeight = childFooterHeight;
    }

    public void setChildBottomHeight(int childBottomHeight) {
        this.childBottomHeight = childBottomHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mTarget == null) {
            ensureTarget();
        }
        if (mTarget == null) return;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == mHeaderView) {
                child.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec((int) (getResources().getDisplayMetrics().density * childHeaderHeight), MeasureSpec.EXACTLY));
            } else if (child == mFooterView) {
                child.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec((int) (getResources().getDisplayMetrics().density * childFooterHeight), MeasureSpec.EXACTLY));
            } else if (child == mBottomView) {
                child.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec((int) (getResources().getDisplayMetrics().density * childBottomHeight), MeasureSpec.EXACTLY));
            } else {
                child.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY));
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == mHeaderView) {
                child.layout(0, -child.getMeasuredHeight(), child.getMeasuredWidth(), 0);
            } else if (child == mFooterView) {
                child.layout(0, getMeasuredHeight(), child.getMeasuredWidth(), getMeasuredHeight() + child.getMeasuredHeight());
            } else if (child == mBottomView) {
                child.layout(0, getMeasuredHeight(), child.getMeasuredWidth(), getMeasuredHeight() + child.getMeasuredHeight());
            } else {
                child.layout(getPaddingLeft(), getPaddingTop(), getPaddingLeft() + child.getMeasuredWidth(), getMeasuredHeight() - getPaddingBottom());
            }
        }
    }

    private void ensureTarget() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child != mHeaderView && child != mFooterView && child != mBottomView) {
                mTarget = child;
                break;
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercept = false;
        float   y         = ev.getY();
        direction = ev.getAction() == MotionEvent.ACTION_UP || y == mLastY ?
                SCROLL_NONE : y > mLastY ? SCROLL_UP : SCROLL_DOWN;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                intercept = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTarget != null && !ViewCompat.isNestedScrollingEnabled(mTarget)) {
                    if (y > mLastY) {//上滑
                        intercept = !canChildScrollUp();
                    } else if (y < mLastY) {
                        intercept = !canChildScrollDown();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                intercept = false;
                direction = SCROLL_NONE;
                break;
        }
        mLastY = y;
        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(getScrollY()) > ignorePullRange) {
                    requestDisallowInterceptTouchEvent(true);
                }
                if (enable) {
                    doScroll((int) (mLastY - y));
                }
                break;
            case MotionEvent.ACTION_UP:
                onStopScroll();
                requestDisallowInterceptTouchEvent(false);
                break;
        }
        mLastY = y;
        return true;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        // if this is a List < L or another view that doesn't support nested
        // scrolling, ignore this request so that the vertical scroll event
        // isn't stolen
        if ((Build.VERSION.SDK_INT < 21 && mTarget instanceof AbsListView)
                || (mTarget != null && !ViewCompat.isNestedScrollingEnabled(mTarget))) {
            // Nope.
        } else {
            super.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    private boolean canChildScrollDown() {
        if (mCanChildScrollDown != null)
            return mCanChildScrollDown.canChildScrollDown(this, mTarget);
        if (Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0 && (absListView.getLastVisiblePosition() != absListView.getChildCount() - 1
                        || absListView.getChildAt(absListView.getChildCount() - 1).getBottom() > absListView.getMeasuredHeight());
            } else
                return ViewCompat.canScrollVertically(mTarget, 1) || mTarget.getScrollY() < mTarget.getMeasuredHeight() - getMeasuredHeight();
        } else
            return ViewCompat.canScrollVertically(mTarget, 1);
    }

    private boolean canChildScrollUp() {
        if (mCanChildScrollUp != null)
            return mCanChildScrollUp.canChildScrollUp(this, mTarget);
        if (Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0 && (absListView.getChildAt(0).getTop() < absListView.getPaddingTop()
                        || absListView.getFirstVisiblePosition() > 0);
            } else
                return ViewCompat.canScrollVertically(mTarget, -1) || mTarget.getScrollY() > 0;
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }

    /**
     * 拉伸状态判断
     * <p>
     * 上拉中或上拉抬起手指，并且在加载更多，无效
     * 下拉中或下拉抬起手指，并且在下拉刷新，无效
     */
    private boolean isRefreshingOrLoading() {
        return (direction != SCROLL_UP && currentState >= State.PULL_UP_RELEASE && currentState < State.PULL_UP_FINISH)
                || (direction != SCROLL_DOWN && currentState >= State.PULL_DOWN_RELEASE && currentState < State.PULL_DOWN_FINISH);
    }

    private void doScroll(int dy) {
        if (!isLastScrollComplete) return;
        if (isRefreshingOrLoading()) return;

        if (dy > 0) {
            //上拉加载
            if (showBottom) {
                //显示无更多布局
                if (mBottomView != null) ((View) mBottomView).setVisibility(VISIBLE);
                if (mFooterView != null) ((View) mFooterView).setVisibility(GONE);
                if (getScrollY() < 0) { //下拉过程中的上拉，无效上拉
                    if (Math.abs(getScrollY()) < effectivePullDownRange) {
                        if (currentState != State.PULL_DOWN)
                            updateStatus(State.PULL_DOWN);
                    }
                } else {
                    if (!pullUpEnable) return;
                    int bHeight = 0;
                    if (mBottomView != null)
                        bHeight = ((View) mBottomView).getMeasuredHeight();
                    if (Math.abs(getScrollY()) >= bHeight) return;
                    dy /= computeInterpolationFactor(getScrollY());
                    updateStatus(State.BOTTOM);
                }
            } else {
                //显示加载布局
                if (mBottomView != null) ((View) mBottomView).setVisibility(GONE);
                if (mFooterView != null) ((View) mFooterView).setVisibility(VISIBLE);
                if (getScrollY() < 0) { //下拉过程中的上拉，无效上拉
                    if (Math.abs(getScrollY()) < effectivePullDownRange) {
                        if (currentState != State.PULL_DOWN)
                            updateStatus(State.PULL_DOWN);
                    }
                } else {
                    if (!pullUpEnable) return;
                    if (Math.abs(getScrollY()) >= effectivePullUpRange) {
                        dy /= computeInterpolationFactor(getScrollY());
                        if (currentState != State.PULL_UP_RELEASABLE)
                            updateStatus(State.PULL_UP_RELEASABLE);
                    } else {
                        if (currentState != State.PULL_UP)
                            updateStatus(State.PULL_UP);
                    }
                }
            }
        } else {
            //下拉刷新
            if (getScrollY() > 0) {   //说明不是到达顶部的下拉，无效下拉
                if (Math.abs(getScrollY()) < effectivePullUpRange) {
                    if (currentState != State.PULL_UP)
                        updateStatus(State.PULL_UP);
                }
            } else {
                if (!pullDownEnable) return;
                if (Math.abs(getScrollY()) >= effectivePullDownRange) {
                    //到达下拉最大距离，增加阻尼因子
                    dy /= computeInterpolationFactor(getScrollY());
                    if (currentState != State.PULL_DOWN_RELEASABLE)
                        updateStatus(State.PULL_DOWN_RELEASABLE);
                } else {
                    if (currentState != State.PULL_DOWN)
                        updateStatus(State.PULL_DOWN);
                }
            }
        }

        dy /= DECELERATE_INTERPOLATION_FACTOR;
        scrollBy(0, dy);
    }

    private void onStopScroll() {

        if (showBottom && getScrollY() > 0) {
            //显示无更多布局
            updateStatus(State.BOTTOM);
            if (Math.abs(getScrollY()) != 0) {
                mScroller.startScroll(0, getScrollY(), 0, -getScrollY());
                mScroller.extendDuration(ANIMATION_EXTEND_DURATION);
                invalidate();
            }
        } else {
            if (isRefreshingOrLoading()) return;
            if ((Math.abs(getScrollY()) >= effectivePullDownRange) && getScrollY() < 0) {
                //有效下拉
                updateStatus(State.PULL_DOWN_RELEASE);
                mScroller.startScroll(0, getScrollY(), 0, -(getScrollY() + effectivePullDownRange));
                mScroller.extendDuration(ANIMATION_EXTEND_DURATION);
                invalidate();
            } else if ((Math.abs(getScrollY()) >= effectivePullUpRange) && getScrollY() > 0) {
                //有效上拉
                updateStatus(State.PULL_UP_RELEASE);
                mScroller.startScroll(0, getScrollY(), 0, -(getScrollY() - effectivePullUpRange));
                mScroller.extendDuration(ANIMATION_EXTEND_DURATION);
                invalidate();
            } else {
                //无效距离，还原
                updateStatus(State.PULL_NORMAL);
            }
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            isLastScrollComplete = false;
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        } else {
            isLastScrollComplete = true;
            if (currentState == State.PULL_DOWN_RESET)
                currentState = State.PULL_DOWN_FINISH;
            if (currentState == State.PULL_UP_RESET)
                currentState = State.PULL_UP_FINISH;
        }
    }

    private void updateStatus(int state) {
        switch (state) {
            case State.PULL_NORMAL:
                pullDownReset();
                break;
            case State.PULL_DOWN:
                if (mHeaderView != null) {
                    mHeaderView.pullDown();
                }
                break;
            case State.PULL_DOWN_RELEASABLE:
                if (mHeaderView != null) {
                    mHeaderView.pullDownReleasable();
                }
                break;
            case State.PULL_DOWN_RELEASE:
                if (mHeaderView != null) {
                    mHeaderView.pullDownRelease();
                }
                if (mRefreshListener != null) {
                    mRefreshListener.onRefresh();
                }
                showNoMore(false);
                setEnable(false);
                break;
            case State.PULL_DOWN_RESET:
                pullDownReset();
                break;
            case State.PULL_UP_RESET:
                pullUpReset();
                break;
            case State.PULL_UP:
                if (mFooterView != null) {
                    mFooterView.pullUp();
                }
                break;
            case State.PULL_UP_RELEASABLE:
                if (mFooterView != null) {
                    mFooterView.pullUpReleasable();
                }
                break;
            case State.PULL_UP_RELEASE:
                if (mFooterView != null) {
                    mFooterView.pullUpRelease();
                }
                if (mRefreshListener != null) {
                    mRefreshListener.onLoadMore();
                }
                setEnable(false);
                break;
            case State.PULL_UP_FINISH:
                if (mFooterView != null) {
                    mFooterView.pullUpFinish();
                }
                break;
            case State.BOTTOM:
                if (mBottomView != null) {
                    mBottomView.showBottom();
                }
                break;
        }

        currentState = state;
    }

    private void pullUpReset() {
        setEnable(true);
        if (Math.abs(getScrollY()) != 0) {
            mScroller.startScroll(0, getScrollY(), 0, -getScrollY());
            mScroller.extendDuration(ANIMATION_EXTEND_DURATION);
            invalidate();  //触发onDraw()
        }
        mHandler.sendEmptyMessageDelayed(MSG_PULL_UP, 5);
    }

    @SuppressWarnings("Handlerleak")
    private Handler mHandler = new Handler() {
        private int pullCount = 0;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_PULL_UP:
                    pullCount++;

                    if (canChildScrollDown()) {
                        pullCount = 0;
                        mHandler.removeMessages(MSG_PULL_UP);
                        mTarget.scrollBy(0, (int) (getResources().getDisplayMetrics().density * 6));
                    } else {
                        if (pullCount >= 20) {
                            pullCount = 0;
                            mHandler.removeMessages(MSG_PULL_UP);
                        } else {
                            mHandler.sendEmptyMessageDelayed(MSG_PULL_UP, 5);
                        }
                    }
                    break;
                case MSG_DOWN_RESET:
                    if (!isLastScrollComplete) {
                        mHandler.sendEmptyMessageDelayed(MSG_DOWN_RESET, 5);
                    } else
                        onRefreshComplete();
                    break;
                case MSG_UP_RESET:
                    if (!isLastScrollComplete) {
                        mHandler.sendEmptyMessageDelayed(MSG_UP_RESET, 5);
                    } else
                        onLoadMoreComplete();
                    break;
                case MSG_NO_MORE:
                    if (getScrollY() == 0 && (currentState == State.PULL_DOWN_FINISH || currentState == State.PULL_UP_FINISH))
                        showNoMore(showBottom);
                    else
                        mHandler.sendEmptyMessageDelayed(MSG_NO_MORE, 5);
                    break;
            }
        }
    };

    private void pullDownReset() {
        setEnable(true);
        if (Math.abs(getScrollY()) != 0) {
            mScroller.startScroll(0, getScrollY(), 0, -getScrollY());
            mScroller.extendDuration(ANIMATION_EXTEND_DURATION);
            invalidate();
        }
    }

    private float computeInterpolationFactor(int dy) {
        int absY = Math.abs(dy);
        int delta;
        if (dy > 0) {
            if (absY <= effectivePullUpRange) return DECELERATE_INTERPOLATION_FACTOR;
            delta = (absY - effectivePullUpRange) / 50;  //增加50，阻尼系数+1
        } else {
            if (absY <= effectivePullDownRange) return DECELERATE_INTERPOLATION_FACTOR;
            delta = (absY - effectivePullDownRange) / 50;  //增加50，阻尼系数+1
        }

        return DECELERATE_INTERPOLATION_FACTOR + delta;
    }

    public void onRefreshComplete() {
        if (!isLastScrollComplete) {
            mHandler.sendEmptyMessageDelayed(MSG_DOWN_RESET, 5);
            return;
        }
        updateStatus(State.PULL_DOWN_RESET);
    }

    public void onLoadMoreComplete() {
        if (!isLastScrollComplete) {
            mHandler.sendEmptyMessageDelayed(MSG_UP_RESET, 5);
            return;
        }
        updateStatus(State.PULL_UP_RESET);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacksAndMessages(null);
    }

    public interface CanChildScrollUp {
        boolean canChildScrollUp(SimpleRefreshLayout parent, View child);
    }

    public interface CanChildScrollDown {
        boolean canChildScrollDown(SimpleRefreshLayout parent, View child);
    }

    public interface OnSimpleRefreshListener {
        void onRefresh();

        void onLoadMore();
    }

    public void setOnSimpleRefreshListener(OnSimpleRefreshListener listener) {
        this.mRefreshListener = listener;
    }

    public void setPullDownEnable(boolean pullDownEnable) {
        this.pullDownEnable = pullDownEnable;
    }

    public void setPullUpEnable(boolean pullUpEnable) {
        this.pullUpEnable = pullUpEnable;
    }

    public void setScrollEnable(boolean enable) {
        this.enable = enable;
    }

    //--------------------  NestedScrollParent  -------------------------------//

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return isEnabled() && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);

        //告诉父类开始滑动
        startNestedScroll(axes & ViewCompat.SCROLL_AXIS_VERTICAL);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {

        //只有在自己滑动的情形下才进行预消耗
        if (getScrollY() != 0) {

            //这里相当于做了一个边界条件
            if (getScrollY() > 0 && dy < 0 && Math.abs(dy) >= Math.abs(getScrollY())) {  //上拉过程中下拉
                consumed[1] = getScrollY();
                scrollTo(0, 0);
                return;
            }

            if (getScrollY() < 0 && dy > 0 && Math.abs(dy) >= Math.abs(getScrollY())) {
                consumed[1] = getScrollY();
                scrollTo(0, 0);
                return;
            }

            int yConsumed = Math.abs(dy) >= Math.abs(getScrollY()) ? getScrollY() : dy;
            doScroll(yConsumed);
            consumed[1] = yConsumed;
        }

        //父类消耗剩余距离
        final int[] parentConsumed = mParentScrollConsumed;
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0];
            consumed[1] += parentConsumed[1];
        }
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {

        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, mParentOffsetInWindow);

        int dy = dyUnconsumed + mParentOffsetInWindow[1];

        if (enable) {
            if (direction == SCROLL_DOWN && !pullUpEnable) return;                  //用户不开启加载
            if (direction == SCROLL_UP && !pullDownEnable) return;                  //用户不开启下拉
            doScroll(dy);
        }
    }

    @Override
    public void onStopNestedScroll(View child) {
        onStopScroll();
        mNestedScrollingParentHelper.onStopNestedScroll(child);

        stopNestedScroll();
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    //------------------------------ NestedScrollChild ---------------------//


    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mNestedScrollingChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mNestedScrollingChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable @Size(value = 2) int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable @Size(value = 2) int[] consumed, @Nullable @Size(value = 2) int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    //-------------------------------- 状态 --------------------------------//

    private interface State {

        int PULL_NORMAL          = 0;  //普通状态
        int PULL_DOWN            = 1;  //下拉中
        int PULL_DOWN_RELEASABLE = 2;  //下拉可刷新
        int PULL_DOWN_RELEASE    = 3;  //下拉正在刷新
        int PULL_DOWN_RESET      = 4;  //下拉恢复正常
        int PULL_DOWN_FINISH     = 5;  //下拉完成
        int PULL_UP              = 6;  //上拉中
        int PULL_UP_RELEASABLE   = 7;  //上拉可刷新
        int PULL_UP_RELEASE      = 8;  //上拉正在刷新
        int PULL_UP_RESET        = 9;  //上拉恢复正常
        int PULL_UP_FINISH       = 10; //上拉完成
        int BOTTOM               = 11; //无更多
    }
}
