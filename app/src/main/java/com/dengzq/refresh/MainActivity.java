package com.dengzq.refresh;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dengzq.refresh.widgets.SimpleBottomView;
import com.dengzq.refresh.widgets.SimpleLoadView;
import com.dengzq.refresh.widgets.SimpleRefreshView;
import com.dengzq.simplerefreshlayout.SimpleRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    RecyclerView        mRecyclerView;
    SimpleRefreshLayout mSimpleRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refresh);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mSimpleRefreshLayout = (SimpleRefreshLayout) findViewById(R.id.simple_refresh);

        mSimpleRefreshLayout.setScrollEnable(true);
        mSimpleRefreshLayout.setPullUpEnable(true);
        mSimpleRefreshLayout.setPullDownEnable(true);
        mSimpleRefreshLayout.setHeaderView(new SimpleRefreshView(this));
        mSimpleRefreshLayout.setFooterView(new SimpleLoadView(this));
        mSimpleRefreshLayout.setBottomView(new SimpleBottomView(this));

        mSimpleRefreshLayout.setOnSimpleRefreshListener(new SimpleRefreshLayout.OnSimpleRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mData.add(0, "我是刷新的数据");
                        mAdapter.notifyDataSetChanged();
                        mSimpleRefreshLayout.onRefreshComplete();

                    }
                }, 1500);
            }

            @Override
            public void onLoadMore() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mData.add(mData.size(), "我是加载的数据");
                        mAdapter.notifyDataSetChanged();
                        mSimpleRefreshLayout.onLoadMoreComplete();

                        if (mData.size() >= 13) {
                            mSimpleRefreshLayout.showNoMore(true);
                        }
                    }
                }, 1500);
            }
        });

        initData();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
    }

    List<String> mData = new ArrayList<>();

    private void initData() {
        for (int i = 0; i < 10; i++) {
            mData.add("第" + i + "个");
        }
    }

    private RecyclerView.Adapter mAdapter = new RecyclerView.Adapter() {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(MainActivity.this).inflate(R.layout.item_recycler_view, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            TextView tv = (TextView) holder.itemView.findViewById(R.id.tv_text);
            tv.setText(mData.get(position));
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    };

    private class MyViewHolder extends RecyclerView.ViewHolder {

        TextView  mTextView;
        ImageView mIvImage;

        MyViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.tv_text);
            mIvImage = (ImageView) itemView.findViewById(R.id.iv_image);
        }
    }
}
