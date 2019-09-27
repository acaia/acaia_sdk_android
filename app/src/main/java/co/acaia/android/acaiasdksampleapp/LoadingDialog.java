package co.acaia.android.acaiasdksampleapp;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

/**
 * Created by Dennis on 2019-09-24
 */
public class LoadingDialog extends Dialog {
    public LoadingDialog(Context context) {
        super(context, R.style.ProgressDialogStyle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.loading);

        this.setCancelable(false);
        this.setCanceledOnTouchOutside(false);
    }
}
