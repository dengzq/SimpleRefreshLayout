package com.dengzq.simplerefreshlayout;

import android.view.View;

/**
 * <p>公司   tsingning</p>
 * <p>作者   dengzq</p>
 * <p>时间   2017/6/29 09:19</p>
 * <p>包名   com.dengzq.dengzqtest.widgets.simpleRefresh</p>
 * <p>描述   TODO</p>
 */

public interface IBottomWrapper {

    /**
     * 获取无更多布局
     *
     * @return
     */
    View getBottomView();

    /**
     * 显示无更多布局
     */
    void showBottom();
}
