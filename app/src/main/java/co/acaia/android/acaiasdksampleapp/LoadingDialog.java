package co.acaia.android.acaiasdksampleapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by Dennis on 2019-09-24
 */
public class LoadingDialog extends Dialog {
    enum LoadingMode{
        normal, bar
    }
    private Activity activity;
    private LinearLayout layoutBar;
    private ProgressBar progressCircle, progressBar;
    private TextView tvProgress;

    public LoadingDialog(Activity activity) {
        super(activity, R.style.ProgressDialogStyle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.loading);

        this.activity = activity;
        this.setCancelable(false);
        this.setCanceledOnTouchOutside(false);

        layoutBar = findViewById(R.id.layout_bar);
        progressCircle = findViewById(R.id.progress_circle);
        progressBar = findViewById(R.id.progress_bar);
        tvProgress = findViewById(R.id.tv_progress);
        setLoadingMode(LoadingMode.normal);
    }

    public void show(LoadingMode mode){
        setLoadingMode(mode);
        super.show();
    }

    public void dismiss(){
        setProgress(0);
        super.dismiss();
    }

    public void setLoadingMode(final LoadingMode mode){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (mode){
                    case normal:
                        progressCircle.setVisibility(View.VISIBLE);
                        layoutBar.setVisibility(View.GONE);
                        break;
                    case bar:
                        layoutBar.setVisibility(View.VISIBLE);
                        progressCircle.setVisibility(View.GONE);
                        break;
                }
            }
        });
    }

    public void setProgress(final int progress){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(progress);
                tvProgress.setText(progress + " %");
            }
        });
    }
}
