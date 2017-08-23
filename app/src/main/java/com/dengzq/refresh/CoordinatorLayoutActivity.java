package com.dengzq.refresh;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.dengzq.refresh.widgets.SimpleBottomView;
import com.dengzq.refresh.widgets.SimpleLoadView;
import com.dengzq.refresh.widgets.SimpleRefreshView;
import com.dengzq.simplerefreshlayout.SimpleRefreshLayout;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>公司  tsingning</p>
 * <p>作者  dengzq</p>
 * <p>时间  2017/8/5 上午11:37</p>
 * <p>描述  TODO</p>
 */

public class CoordinatorLayoutActivity extends AppCompatActivity {
    private static final String TAG = "CoordinatorLayoutActivi";
    private CoordinatorLayout   mCoordinatorLayout;
    private SimpleRefreshLayout mSimpleRefreshLayout;
    private RecyclerView        mRecyclerView;
    private RecyclerAdapter     mRecyclerAdapter;
    private AppBarLayout        mAppBarLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_coordinatorlayout);

        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        mSimpleRefreshLayout = (SimpleRefreshLayout) findViewById(R.id.simple_refresh);
        if (mSimpleRefreshLayout != null) {
            mSimpleRefreshLayout.setHeaderView(new SimpleRefreshView(CoordinatorLayoutActivity.this));
            mSimpleRefreshLayout.setFooterView(new SimpleLoadView(CoordinatorLayoutActivity.this));
            mSimpleRefreshLayout.setBottomView(new SimpleBottomView(CoordinatorLayoutActivity.this));
            mSimpleRefreshLayout.setOnSimpleRefreshListener(new SimpleRefreshLayout.OnSimpleRefreshListener() {
                @Override
                public void onRefresh() {
                    addRefreshData();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mSimpleRefreshLayout.onRefreshComplete();
                            mRecyclerAdapter.notifyDataSetChanged();
                        }
                    }, 1000);
                }

                @Override
                public void onLoadMore() {
                    addLoadMoreData();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mSimpleRefreshLayout.onLoadMoreComplete();
                            mRecyclerAdapter.notifyDataSetChanged();
                            if (mRecyclerAdapter.getItemCount() >= 30) mSimpleRefreshLayout.showNoMore(true);
                        }
                    }, 1000);
                }
            });
        }

        initData();
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mRecyclerAdapter = new RecyclerAdapter(this));

    }

    private class RecyclerAdapter extends CommonAdapter<String> {

        RecyclerAdapter(Context context) {
            super(context, R.layout.item_recycler_view, mStringList);
        }

        @Override
        protected void convert(ViewHolder holder, String s, int position) {
            holder.setText(R.id.tv_text, s);
            ImageView image = holder.getView(R.id.iv_image);
            if ("新加刷新数据".equals(s)) {
                image.setImageResource(R.mipmap.icon_refresh);
                return;
            }
            if ("新加加载数据".equals(s)) {
                image.setImageResource(R.mipmap.icon_loadmore);
                return;
            }
            image.setImageResource(R.mipmap.icon_avatar);
        }
    }

    private List<String> mStringList = new ArrayList<>();

    private void initData() {
        for (int i = 0; i < 15; i++) {
            mStringList.add(String.format("第%s条数据", i));
        }
    }

    private void addRefreshData() {
        mStringList.add(0, "新加刷新数据");
    }

    private void addLoadMoreData() {
        mStringList.add("新加加载数据");
    }
}
