package com.fly.run.activity.ChooseImages;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.GridView;

import com.fly.run.R;
import com.fly.run.activity.base.BaseUIActivity;
import com.fly.run.adapter.ChooseImagesAdapter;
import com.fly.run.bean.FileItem;
import com.fly.run.utils.MediaQueryUtil;
import com.fly.run.utils.ToastUtil;
import com.fly.run.view.actionbar.CommonActionBar;

import java.util.List;

public class ChooseImagesActivity extends BaseUIActivity {

    private CommonActionBar actionBar;
    private GridView gridView;
    private ChooseImagesAdapter adapter;
    private int ChooseCount = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_images);
        initActionBar();
        initView();
        new Thread(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                Log.e(TAG, "start = " + start);
                initLocalImages();
                Log.e(TAG, "耗时 = " + (System.currentTimeMillis() - start)+"  数量 = "+list.size());
            }
        }).start();
    }

    private void initActionBar() {
        actionBar = (CommonActionBar) findViewById(R.id.common_action_bar);
        actionBar.setActionLeftIconListenr(-1, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        actionBar.setActionRightIconListenr(R.drawable.ic_jobs_commen_press, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    showGridImgsView(list);
                    break;
            }
        }
    };

    private List<FileItem> list = null;

    private void initLocalImages() {
        list = MediaQueryUtil.getAllPhoto(this);
        handler.sendEmptyMessage(1);
    }

    private void initView() {
        gridView = (GridView) findViewById(R.id.gridview);
        adapter = new ChooseImagesAdapter(this);
        gridView.setAdapter(adapter);
        adapter.setImageListener(new ChooseImagesAdapter.ImageListenrt() {
            @Override
            public void doShowImage(int position, String url) {

            }

            @Override
            public void doChooseImage(int position, String url) {
                FileItem item = adapter.getItem(position);
                if (!item.isCheck()){
                    if (adapter.getChooseItems().size() >= ChooseCount){
                        ToastUtil.show("最多选择9张图片");
                        return;
                    }
                    adapter.getChooseItems().add(item);
                } else {
                    adapter.getChooseItems().remove(item);
                }
                item.setCheck(!item.isCheck());
                adapter.notifyDataSetChanged();
            }
        });
        showGridImgsView(null);
    }

    private void showGridImgsView(List<FileItem> images) {
        adapter.setData(images);
        adapter.notifyDataSetChanged();
    }
}
