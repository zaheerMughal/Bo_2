package com.shahzaib.moneybox.Dialogs;

import android.app.AlertDialog;
import android.content.Context;

public class Dialog {

    public static void showAlertDialog(Context context, String message)
    {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("OK",null)
                .show();
    }

    public static void showAlertDialog(Context context,String title, String message)
    {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK",null)
                .show();
    }
}
