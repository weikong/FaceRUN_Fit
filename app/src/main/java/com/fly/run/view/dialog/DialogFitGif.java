package com.fly.run.view.dialog;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fly.run.R;
import com.fly.run.bean.FitBean;
import com.fly.run.config.UrlConstants;
import com.fly.run.utils.IOTools;
import com.fly.run.utils.Logger;
import com.fly.run.utils.SDCardUtil;
import com.fly.run.view.gif.GifView;

import java.io.File;
import java.io.IOException;

/**
 * @author YoYo-SHUN
 * @ClassName: DialogAlertInput
 * @Description: Information dialog
 * @Copyright InventoryLab RJC LLC.  All Rights Reserved.
 * @date 2014-7-26 PM 11:43:43
 */
public class DialogFitGif extends Dialog {

    //    private LinearLayout layout_dialog_content;
    private GifView gifView;
    private TextView tv_join;
    private ProgressDialog pDialog;

    private OnEventListener mOnEventListener;

    public DialogFitGif(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        initViews();
    }

    public DialogFitGif(Context context, int theme) {
        super(context, theme);
        initViews();
    }

    public DialogFitGif(Context context) {
        this(context, R.style.DialogFullScreen);
    }

    private void initViews() {
        setContentView(R.layout.dialog_fit_gif);
        getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        getWindow().setGravity(Gravity.CENTER);
        getWindow().setBackgroundDrawable(new ColorDrawable(0x44000000));
        View root = findViewById(R.id.dialog_root);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
//        layout_dialog_content = (LinearLayout) findViewById(R.id.layout_dialog_content);
        tv_join = (TextView) findViewById(R.id.tv_join);
        gifView = (GifView) findViewById(R.id.gifView);
        tv_join.setOnClickListener(mClickListener);
        this.setCancelable(true);
    }

    public void setFitBean(FitBean bean) {
//        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout_dialog_content.getLayoutParams();
//        if (bean != null && bean.getItemWidth() > 0 && bean.getItemHeight() > 0) {
////            int gifWidth = DisplayUtil.screenWidth - DisplayUtil.dp2px(80);
////            int viewHeight = gifWidth * bean.getItemHeight() / bean.getItemWidth();
////            params.height = viewHeight;
//            params.width = bean.getItemWidth();
//            params.height = bean.getItemHeight() + DisplayUtil.dp2px(55);
//        } else {
//            params.width = DisplayUtil.screenWidth - DisplayUtil.dp2px(80);
//        }
    }

    /**
     * @param gifUrl
     */
    public void setData(String gifUrl) {
        if (!TextUtils.isEmpty(gifUrl)) {
            final String filePath = new String(SDCardUtil.getGifDir() + "/" + gifUrl);
            File file = new File(filePath);
            if (file.exists() && file.length() > 0) {
                Message message = Message.obtain();
                message.what = 1;
                message.obj = filePath;
                handler.sendMessage(message);
                return;
            }
            String suffixName = gifUrl.substring(gifUrl.lastIndexOf("."));
            if (suffixName.equalsIgnoreCase(".gif")) {
                gifUrl = gifUrl.trim();
                gifUrl = "fit/" + gifUrl;
                if (!gifUrl.startsWith("http://"))
                    gifUrl = String.format(UrlConstants.HTTP_DOWNLOAD_FILE_2, gifUrl);
                final String finalGifUrl = gifUrl;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            handler.sendEmptyMessage(11);
                            boolean save = IOTools.saveFile(finalGifUrl, filePath);
                            if (save) {
                                Message message = Message.obtain();
                                message.what = 1;
                                message.obj = filePath;
                                handler.sendMessage(message);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            handler.sendEmptyMessage(12);
                        }
                    }
                }).start();
            }
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    String filePath = (String) msg.obj;
                    gifView.setMovieFile(filePath);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) gifView.getLayoutParams();
                            int w = params.width;
                            int h = params.height;
                            if (w > 0 && h > 0) {
                                params.width = (int) (w * 0.8f);
                                params.height = (int) (h * 0.8f);
                                gifView.setLayoutParams(params);
                                Logger.e("dialog fig w= " + params.width + " h = " + params.height);
                            }
                        }
                    }, 2000);
                    break;
                case 11:
                    showProgreessDialog();
                    break;
                case 12:
                    dismissProgressDialog();
                    break;
            }
        }
    };


    public void setOnEventListener(OnEventListener l) {
        this.mOnEventListener = l;
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void cancel() {
        super.cancel();
        if (mOnEventListener != null)
            mOnEventListener.result(false);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_join:
                    dismiss();
                    if (mOnEventListener != null)
                        mOnEventListener.result(true);
                    break;
            }
        }
    };

    public interface OnEventListener {

        /**
         * @param sure Are you sure
         */
        void result(boolean sure);
    }

    /**
     * 显示等待对话框 当点击返回键取消对话框并停留在该界面
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void showProgreessDialog() {
        if (pDialog == null) {
            pDialog = new ProgressDialog(getContext());
            pDialog.setCanceledOnTouchOutside(false);
        }
        if (pDialog.isShowing())
            pDialog.dismiss();
        pDialog.show();
        pDialog.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    try {
                        dismissProgressDialog();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
    }

    /**
     * 销毁对话框
     */
    public void dismissProgressDialog() {
        try {
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
