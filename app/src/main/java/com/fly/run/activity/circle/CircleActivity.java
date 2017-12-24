package com.fly.run.activity.circle;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.fly.run.R;
import com.fly.run.activity.base.BaseUIActivity;
import com.fly.run.adapter.circle.CircleAdapter;
import com.fly.run.bean.AccountBean;
import com.fly.run.bean.CircleBean;
import com.fly.run.bean.ResultTaskBean;
import com.fly.run.httptask.HttpTaskUtil;
import com.fly.run.manager.UserInfoManager;
import com.fly.run.utils.ToastUtil;
import com.fly.run.view.actionbar.CommonActionBar;
import com.fly.run.view.listview.DynamicListView;
import com.squareup.okhttp.Request;

import java.util.List;

public class CircleActivity extends BaseUIActivity implements View.OnClickListener, DynamicListView.DynamicListViewListener {

    private CommonActionBar actionBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DynamicListView listView;
    private CircleAdapter adapter;
    private HttpTaskUtil httpTaskUtil;

    private int pageNum = 1;
    private final int pageSize = 40;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle);
        initActionBar();
        initView();
        swipeRefreshLayout.setRefreshing(true);
        loadTaskData();
    }

    private void initActionBar() {
        actionBar = (CommonActionBar) findViewById(R.id.common_action_bar);
        actionBar.setActionTitle("跑圈圈");
        actionBar.setActionLeftIconListenr(-1, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        actionBar.setActionRightIconListenr(R.drawable.ic_menu_camera, getResources().getColor(R.color.color_ffffff), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentToActivity(CirclePublishActivity.class);
            }
        });
    }

    private void initView() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        // 设置颜色属性的时候一定要注意是引用了资源文件还是直接设置16进制的颜色，因为都是int值容易搞混
        // 设置下拉进度的背景颜色，默认就是白色的
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
        // 设置下拉进度的主题颜色
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_purple, android.R.color.holo_green_light, R.color.colorAccent, android.R.color.holo_blue_light);

        // 下拉时触发SwipeRefreshLayout的下拉动画，动画完毕之后就会回调这个方法
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageNum = 1;
                loadTaskData();
            }
        });
        listView = (DynamicListView) findViewById(R.id.listview);
        adapter = new CircleAdapter(this);
        listView.setAdapter(adapter);
        listView.setDoMoreWhenBottom(false);    // 滚动到低端的时候不自己加载更多
//        listView.setOnRefreshListener(this);
        listView.setOnMoreListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }

    private void loadTaskData() {
        if (httpTaskUtil == null) {
            httpTaskUtil = new HttpTaskUtil();
            httpTaskUtil.setResultListener(resultListener);
        }
        AccountBean bean = UserInfoManager.getInstance().getAccountInfo();
        if (bean == null || bean.getId() <= 0) {
            ToastUtil.show("请登录");
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        httpTaskUtil.QueryCircleRunTask(pageNum, pageSize, "" + bean.getId());
    }

    HttpTaskUtil.ResultListener resultListener = new HttpTaskUtil.ResultListener() {
        @Override
        public void onResponse(String response) {
            try {
                ResultTaskBean bean = JSON.parseObject(response, ResultTaskBean.class);
                if (bean != null && bean.code == 1) {
                    if (!TextUtils.isEmpty(bean.data)) {
                        List<CircleBean> list = JSON.parseArray(bean.data, CircleBean.class);
                        if (list == null || list.size() == 0)
                            return;
                        if (pageNum == 1) {
                            adapter.setData(list);
                        } else {
                            adapter.addData(list);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            } catch (Exception e) {
                pageNum--;
                ToastUtil.show((e != null && !TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "网络请求失败"));
            } finally {
                if (pageNum == 1)
                    swipeRefreshLayout.setRefreshing(false);
                else
                    listView.doneMore();
            }
        }

        @Override
        public void onFailure(Request request, Exception e) {
            pageNum--;
            ToastUtil.show((e != null && !TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "网络请求失败"));
        }
    };

    @Override
    public boolean onRefreshOrMore(DynamicListView dynamicListView, boolean isRefresh) {
        if (isRefresh) {
            //刷新
            pageNum = 1;
            loadTaskData();
        } else {
            //加载更多
            pageNum++;
            loadTaskData();
        }
        return false;
    }
}
