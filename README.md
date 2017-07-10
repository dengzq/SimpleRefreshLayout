# SimpleRefreshLayout
支持下拉刷新，上拉加载以及无更多；支持布局自定义;


### demo效果

![simplerefresh.gif](https://github.com/dengzq/SimpleRefreshLayout/blob/master/images/simplerefresh.gif)

### 使用

#### gradle依赖

```

compile 'com.dengzq.widget:simplerefreshlayout:0.5.0'

```

#### 1.布局配置
1.在xml中作为一个viewgroup使用。 注：子布局只能有一个
</br>
eg:

```
<com.dengzq.simplerefreshlayout.SimpleRefreshLayout
        android:id="@+id/simple_refresh"
        android:background="#F5F8F9"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        <android.support.v7.widget.RecyclerView
            android:id="@+id/recycler_view"
            android:layout_width="match_parent"
            android:layout_height="match_parent"/>
    </com.dengzq.simplerefreshlayout.SimpleRefreshLayout>
```

#### 2.相关方法

1) 下拉刷新、上拉加载监听

```
mSimpleRefreshLayout.setOnSimpleRefreshListener(new SimpleRefreshLayout.OnSimpleRefreshListener() {
            @Override
            public void onRefresh() {
                //下拉刷新完成
                mSimpleRefreshLayout.onRefreshComplete();
            }

            @Override
            public void onLoadMore() {
               //上拉加载完成
               mSimpleRefreshLayout.onLoadMoreComplete();
            }
        });
```

2) 是否开启刷新、上拉

```
mSimpleRefreshLayout.setPullUpEnable(true);
mSimpleRefreshLayout.setPullDownEnable(true);
```

3) 自定义刷新、加载、无更多布局

* 自定义刷新布局请实现`IHeaderWrapper`接口

`mSimpleRefreshLayout.setHeaderView( your view here);`

```
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
```

* 自定义上拉加载布局请实现`IFooterWrapper`接口

` mSimpleRefreshLayout.setFooterView(your view here);`

```
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
}
```

* 自定义无更多布局请实现`IBottomWrapper`接口

`mSimpleRefreshLayout.setBottomView(your view here);`

```
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
```

4) 显示无更多布局

显示`无更多`布局，需要先配置:

`mSimpleRefreshLayout.setPullUpEnable(true)`,
`mSimpleRefreshLayout.setBottomView( your view here );`

在服务器返回数据小于你所设置的分页数时，可认为无更多数据。此时，
`mSimpleRefreshLayout.showNoMore(true);`即可显示无更多底部。

5) 其他属性

1> 三种布局在simpleRefreshLayout中的高度(一般不需要配置)

```
mSimpleRefreshLayout.setChildHeaderHeight(int val);
mSimpleRefreshLayout.setChildFooterHeight(int val);
mSimpleRefreshLayout.setChildBottomHeight(int val);
```
2> 下拉刷新，上拉加载事件的有效高度(根据个人刷新布局高度处理)

默认下拉有效的高度为80dp,上拉有效的高度为45dp

```
mSimpleRefreshLayout.setEffectivePullDownRange(int val);
mSimpleRefreshLayout.setEffectivePullDownRange(int val);
```

### 特别感谢
[TwinklingRefreshLayout](https://github.com/lcodecorex/TwinklingRefreshLayout)

[CanRefresh](https://github.com/canyinghao/CanRefresh)

[SmartAndroidWidgets](https://github.com/RawnHwang/SmartAndroidWidgets)

### 关于NestedScroll
[我的博客](http://www.jianshu.com/p/d0f620f95cdf)

</br>
##### 如果你喜欢本项目，请点个✨哦~




