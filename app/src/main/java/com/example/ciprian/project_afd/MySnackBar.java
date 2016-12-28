package com.example.ciprian.project_afd;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Ciprian on 28/12/2016.
 */

public class MySnackBar {

    private View coordinatorLayout;

    public MySnackBar(View coordinatorLayout) {
        this.coordinatorLayout = coordinatorLayout;
    }

    public void showSnackBar(String message, int duration) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, duration);
        View snackBarView = snackbar.getView();
        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }
}
