package com.dengzq.simplerefreshlayout;

import android.view.View;

/**
 * <p>公司   tsingning</p>
 * <p>作者   dengzq</p>
 * <p>时间   2017/6/22 10:20</p>
 * <p>包名   com.dengzq.dengzqtest.widgets.simpleRefresh</p>
 * <p>描述   TODO</p>
 */

public interface IFooterWrapper {

    /**
     * 获取加载更多布局
     *
     * @return
     */
    View getFooterView();

    /**
     * 上拉中
     */
    void pullUp();

    /**
     * 上拉可释放
     */
    void pullUpReleasable();

    /**
     * 上拉已释放
     */
    void pullUpRelease();

    /**
     * 加载完成
     */
    void pullUpFinish();
}
