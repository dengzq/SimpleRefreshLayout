package com.dengzq.simplerefreshlayout;

import android.view.View;

/**
 * <p>公司   tsingning</p>
 * <p>作者   dengzq</p>
 * <p>时间   2017/6/22 10:20</p>
 * <p>包名   com.dengzq.dengzqtest.widgets.simpleRefresh</p>
 * <p>描述   TODO</p>
 */

public interface IHeaderWrapper {

    /**
     * 获取刷新布局
     * @return
     */
    View getHeaderView();

    /**
     * 下拉中
     */
    void pullDown();

    /**
     * 下拉可刷新
     */
    void pullDownReleasable();

    /**
     * 下拉刷新中
     */
    void pullDownRelease();
}
