package com.cch.scrollviewhelper;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 晨晖 on 2015-09-02.
 */
public class MyScrollView extends ScrollView {


    private final Context context;
    private int lastTouchMoveY;

    private OnScrollListener onScrollListener;
    private List<ViewControler> viewAnimationControls;
    private OnScrollViewExpendListener onScrollViewExpendListener;

    private View strinkableHeaderView;

    private int moveRange = 0;
    /**
     * 开始滑动Y轴坐标
     */
    private float touchStartY = 0;
    //伸缩View的初始高度
    private float strinkHeaderViewHeight;
    /**
     * 是否滚动到顶部标志位
     */
    private boolean isScrollTop = true;

    //滚动距离监听事件Handler标志位
    private final static int SCROLL_LISTENER = 1;
    //回滚动作Handler监听标志位
    private final static int RETURN_BACK = 2;
    //全屏自动滚动监听标志位
    private final static int MOVE_FULL_SCREEN = 3;
    //滚动动画默认时间
    private final static int SCROLL_TIME = 3000;

    private static int screenHeight;

    private static int MOVE_STATE_CHANGE_OFFSET = 200;
    //是否全屏幕模式标志位
    private static boolean isFullScreenState = false;

    private static ScrollState curState;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SCROLL_LISTENER:
                    if (lastTouchMoveY != MyScrollView.this.getScrollY()) {
                        lastTouchMoveY = MyScrollView.this.getScrollY();
                        if (onScrollListener != null) {
                            onScrollListener.onSroll(lastTouchMoveY);
                            updateAllView();
                        }
                        handler.sendEmptyMessageDelayed(SCROLL_LISTENER, 1);
                    }
                    break;
                case RETURN_BACK:
                    if (strinkableHeaderView.getLayoutParams().height > strinkHeaderViewHeight) {
                        strinkableHeaderView.getLayoutParams().height -= ((strinkableHeaderView.getLayoutParams().height - strinkHeaderViewHeight) / 5 + 10);
                        strinkableHeaderView.setLayoutParams(strinkableHeaderView.getLayoutParams());
                        onScrollViewExpendListener.onExpanding(strinkableHeaderView, getCurExpandRate(strinkableHeaderView));
                        handler.removeMessages(MOVE_FULL_SCREEN);
                        handler.sendEmptyMessageDelayed(RETURN_BACK, 5);
                    } else {
                        Log.e("MyScrollView", "已经切换到滚动模式");
                        strinkableHeaderView.getLayoutParams().height = (int) strinkHeaderViewHeight;
                        strinkableHeaderView.setLayoutParams(strinkableHeaderView.getLayoutParams());
                        isFullScreenState = false;
                        changeViewState(ScrollState.STATE_CLOSED);
                        handler.removeMessages(RETURN_BACK);
                    }
                    break;
                case MOVE_FULL_SCREEN:
                    if (strinkableHeaderView.getLayoutParams().height < screenHeight) {
                        strinkableHeaderView.getLayoutParams().height += ((screenHeight - strinkableHeaderView.getLayoutParams().height) / 5 + 10);
                        strinkableHeaderView.setLayoutParams(strinkableHeaderView.getLayoutParams());
                        onScrollViewExpendListener.onExpanding(strinkableHeaderView, getCurExpandRate(strinkableHeaderView));
                        handler.removeMessages(RETURN_BACK);
                        handler.sendEmptyMessageDelayed(MOVE_FULL_SCREEN, 5);
                    } else {
                        Log.e("MyScrollView", "已经切换到全屏模式");
                        strinkableHeaderView.getLayoutParams().height = screenHeight;
                        strinkableHeaderView.setLayoutParams(strinkableHeaderView.getLayoutParams());
                        isFullScreenState = true;
                        changeViewState(ScrollState.STATE_FULL_SCREEN);
                        handler.removeMessages(MOVE_FULL_SCREEN);
                    }
                    break;
            }
        }
    };

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        viewAnimationControls = new ArrayList<>();
        this.onScrollListener = new OnScrollToTop();
        this.onScrollViewExpendListener = new defaultImpleteExpendListener();
        //初始化屏幕高度
        screenHeight = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();
        TypedArray paramsList = context.obtainStyledAttributes(attrs, R.styleable.MyScrollView);
        Float dp = paramsList.getDimension(R.styleable.MyScrollView_setAnchorViewHeight, 0);
        if (dp != 0) {
            moveRange = dp.intValue();
        }

    }

    public void setStrinkableHeaderView(View view) {
        this.strinkableHeaderView = view;
        strinkHeaderViewHeight = view.getLayoutParams().height;
    }

    /**
     * 改变ScrollView的伸缩View的高度
     *
     * @param deltaY
     */
    private void moveScrollView(float deltaY) {
        if (strinkableHeaderView == null) {
            return;
        }
        if (isFullScreenState) {
            ViewGroup.LayoutParams tempLayout = strinkableHeaderView.getLayoutParams();
            tempLayout.height = (int) (screenHeight + deltaY);
            strinkableHeaderView.setLayoutParams(tempLayout);
        } else {
            ViewGroup.LayoutParams tempLayout = strinkableHeaderView.getLayoutParams();
            tempLayout.height = (int) (strinkHeaderViewHeight + deltaY);
            strinkableHeaderView.setLayoutParams(tempLayout);
        }

    }

    /**
     * 根据当前状态进行不同动作
     */
    private void returnHeaderViewHeight(boolean isFullScreen) {
        //回调状态更改接口
        changeViewState(ScrollState.STATE_EXPANDING);
        if (isFullScreen) {
            handler.sendEmptyMessage(MOVE_FULL_SCREEN);
        } else {
            handler.sendEmptyMessage(RETURN_BACK);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isScrollTop || isFullScreenState) {
            int touchAction = ev.getAction();
            float curTouchY = ev.getY();
            if (Math.abs(curTouchY - touchStartY) <= 15) {
                return false;
            }
            switch (touchAction) {
                case MotionEvent.ACTION_DOWN:
                    strinkableHeaderView.setEnabled(false);
                    touchStartY = ev.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    if ((curTouchY - touchStartY) > MOVE_STATE_CHANGE_OFFSET) {
                        returnHeaderViewHeight(true);
                    } else if ((curTouchY - touchStartY) < -MOVE_STATE_CHANGE_OFFSET) {
                        returnHeaderViewHeight(false);
                    } else {
                        returnHeaderViewHeight(isFullScreenState);
                    }
                    strinkableHeaderView.setEnabled(true);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!isFullScreenState && (curTouchY - touchStartY) < 0) {
                        isScrollTop = false;
                        return false;
                    }
                    //全屏之後不再响应向下滑动
                    else if (isFullScreenState && ((curTouchY - touchStartY) >= 0)) {
                        return true;
                    } else {
                        changeViewState(ScrollState.STATE_EXPANDING);
                        moveScrollView((curTouchY - touchStartY));
                        onScrollViewExpendListener.onExpanding(strinkableHeaderView, getCurExpandRate(strinkableHeaderView));
                        return true;
                    }
            }
            return true;
        } else {
            if (onScrollListener != null) {
                onScrollListener.onSroll(lastTouchMoveY = this.getScrollY());
                updateAllView();
            }
            if (ev.getAction() == MotionEvent.ACTION_UP) {
                handler.sendEmptyMessageDelayed(SCROLL_LISTENER, 1);
            }
            return super.onTouchEvent(ev);
        }
    }


    /**
     * 修改View的滑动状态
     *
     * @param scrollState
     */
    private void changeViewState(ScrollState scrollState) {
        if (curState == null || curState != scrollState) {
            onScrollViewExpendListener.onStateChange(scrollState);
            curState = scrollState;
        }
    }

    /**
     * 获取当前展开阶段的展开View的展开比例
     *
     * @return 展开比例 0（未展开） -> 1（完全展开）
     */
    private float getCurExpandRate(View headerView) {
        int curHeight = headerView.getLayoutParams().height;
        if (curHeight <= strinkHeaderViewHeight) {
            return 0;
        } else if (curHeight >= screenHeight) {
            return 1;
        } else {
            return (curHeight - strinkHeaderViewHeight) / (screenHeight - strinkHeaderViewHeight);
        }
    }

    /**
     * 设置锚点View，即根据此View来更新其他View参数
     *
     * @param view
     */
    public void setAnchorView(View view) {
        view.measure(view.getLayoutParams().width, view.getLayoutParams().height);
        this.moveRange = view.getLayoutParams().height;
    }

    /**
     * 添加一个控制View，并会调用控制View的更新方法
     *
     * @param control
     */
    public void addControlView(View view, ViewAnimationControl control) {
        viewAnimationControls.add(new ViewControler(view, control));
        control.ofFloat(view, dealRate(this.getScrollY()));
    }

    private void updateAllView() {
        float rate = dealRate(this.getScrollY());
        for (ViewControler viewControler : viewAnimationControls) {
            viewControler.viewAnimationControl.ofFloat(viewControler.view, rate);
        }
    }

    public void setOnExpandListener(OnScrollViewExpendListener onExpandListener) {
        this.onScrollViewExpendListener = onExpandListener;
    }

    private float dealRate(int scrollY) {
        if (moveRange == 0) {
            return 1;
        }
        float res = (float) scrollY / moveRange;
        if (res < 0) {
            return 0;
        } else if (res > 1) {
            return 1;
        } else {
            return res;
        }
    }


    /**
     * 滑动到顶部监听事件
     */
    class OnScrollToTop implements OnScrollListener {

        @Override
        public void onSroll(int delta) {
            if (delta == 0) {
                isScrollTop = true;
            } else {
                isScrollTop = false;
            }
        }
    }

    class defaultImpleteExpendListener implements OnScrollViewExpendListener {

        @Override
        public void onStateChange(ScrollState scrollViewState) {

        }

        @Override
        public void onExpanding(View headerView, float rate) {

        }
    }

    /**
     * ScrollView的滑动监听
     */
    public interface OnScrollListener {
        public void onSroll(int delta);
    }

    /**
     * 界面控件动画回调接口
     */
    public interface ViewAnimationControl {

        void ofFloat(View view, float param);
    }

    /**
     * 用于控制界面其他控件的方法
     */
    private class ViewControler {
        public View view;
        public ViewAnimationControl viewAnimationControl;

        public ViewControler(View view, ViewAnimationControl viewAnimationControl) {
            this.view = view;
            this.viewAnimationControl = viewAnimationControl;
        }

    }

    public enum ScrollState {
        STATE_FULL_SCREEN,
        STATE_EXPANDING,
        STATE_CLOSED,
        STATE_NO_MOVE
    }

    public interface OnScrollViewExpendListener {
        public void onStateChange(ScrollState scrollViewState);

        public void onExpanding(View headerView, float rate);
    }

    public int Dp2Px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
