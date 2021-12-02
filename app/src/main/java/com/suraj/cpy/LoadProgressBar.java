package com.suraj.cpy;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.ProgressBar;

public class LoadProgressBar {
    Activity activity;
    AlertDialog dialog;

    LoadProgressBar(Activity mactivity) {
        this.activity = mactivity;
    }

    void startProgressBar() {
        AlertDialog.Builder builder =new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.progressbar, null));
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.show();
    }

    void dismisprogressBar() {
        dialog.dismiss();
    }
}
