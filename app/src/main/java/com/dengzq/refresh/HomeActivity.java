package com.dengzq.refresh;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * <p>公司  tsingning</p>
 * <p>作者  dengzq</p>
 * <p>时间  2017/8/23 上午9:15</p>
 * <p>描述  TODO</p>
 */

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        findViewById(R.id.tv_recycler_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, RecyclerViewActivity.class));
            }
        });

        findViewById(R.id.tv_coordinator_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, CoordinatorLayoutActivity.class));
            }
        });
    }
}
