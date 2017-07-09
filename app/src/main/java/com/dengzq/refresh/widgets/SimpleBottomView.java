package com.dengzq.refresh.widgets;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.dengzq.refresh.R;
import com.dengzq.simplerefreshlayout.IBottomWrapper;


/**
 * <p>公司   tsingning</p>
 * <p>作者   dengzq</p>
 * <p>时间   2017/7/4 13:32</p>
 * <p>包名   com.dengzq.refresh.widgets.simpleRefresh</p>
 * <p>描述   TODO</p>
 */

public class SimpleBottomView extends LinearLayout implements IBottomWrapper {

    public SimpleBottomView(Context context) {
        this(context, null);
    }

    public SimpleBottomView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleBottomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView(){
        setBackgroundColor(Color.parseColor("#F5F8F9"));
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_refresh_bottom,this,false);
        addView(view);
    }

    @Override
    public View getBottomView() {
        return this;
    }

    @Override
    public void showBottom() {

    }
}
