package com.shahzaib.moneybox;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.shahzaib.moneybox.database.DbContract;

import java.util.Calendar;

import alarm_utils.AlarmService;

public class DepositMoney extends AppCompatActivity implements View.OnClickListener{

    public static final String KEY_INTENT_ITEM_ID = "itemID";
    public static final String KEY_INTENT_IS_OPEN_THROUGH_NOTIFICATION = "isOpenThroughNotification";
    public static final String KEY_INSTANCE_STATE_AMOUNT = "amount";

    ImageButton ic_back_arrow;
    Button addMoneyBtn;
    EditText amountET;


    String itemID="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit_money);
        ic_back_arrow = findViewById(R.id.ic_back_arrow);
        addMoneyBtn = findViewById(R.id.addMoneyBtn);
        amountET = findViewById(R.id.amountET);
        if(savedInstanceState!=null)
        {
            amountET.setText(String.valueOf(savedInstanceState.getDouble(KEY_INSTANCE_STATE_AMOUNT)));
        }


        itemID = getIntent().getStringExtra(KEY_INTENT_ITEM_ID);
        boolean isOpenThroughNotification = getIntent().getBooleanExtra(KEY_INTENT_IS_OPEN_THROUGH_NOTIFICATION,false);
        if(isOpenThroughNotification)
        {//cancel the notification
            // get the notification Id and cancel that notification
            String selection = DbContract.NOTIFICATION_IDs._ID+"="+itemID;
            Cursor cursor = getContentResolver().query(DbContract.NOTIFICATION_IDs.CONTENT_URI,null,selection,null,null);
            if(cursor!=null)
            {
                if(cursor.moveToFirst())
                {
                    int notificationID = cursor.getInt(cursor.getColumnIndex(DbContract.NOTIFICATION_IDs.COLUMN_NotificationID));
                    AlarmService.cancelNotification(this,notificationID);
                    cursor.close();
                }
            }

            // delete the notification data from database
            getContentResolver().delete(DbContract.NOTIFICATION_IDs.CONTENT_URI.buildUpon().appendPath(itemID).build(),null,null);
        }


        ic_back_arrow.setOnClickListener(this);
        addMoneyBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.addMoneyBtn:
                getAndDepositAmount();
                break;

            case R.id.ic_back_arrow:
                finish();
                break;
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble(KEY_INSTANCE_STATE_AMOUNT,getEnteredAmount());
    }

    private void getAndDepositAmount()
    {
        double amount = getEnteredAmount();
        if(amount>0 || amount<0) // means if amount == 0 then do nothing
        {
            depositAmount(amount);
            //******* insert amount into contribution history table
            ContentValues contributionHistoryValues = new ContentValues();
            contributionHistoryValues.put(DbContract.CONTRIBUTION_HISTORY._ID,itemID);
            contributionHistoryValues.put(DbContract.CONTRIBUTION_HISTORY.COLUMN_AMOUNT,amount);
            contributionHistoryValues.put(DbContract.CONTRIBUTION_HISTORY.COLUMN_DATE,Calendar.getInstance().getTimeInMillis());
            getContentResolver().insert(DbContract.CONTRIBUTION_HISTORY.CONTENT_URI,contributionHistoryValues);
            finish();
        }
    }


    private double getEnteredAmount() {
        if(amountET.getText().toString().length()<=0)
        {
            com.shahzaib.moneybox.Dialogs.Dialog.showAlertDialog(this,"Please enter amount first !");
            return 0;
        }
        else
        {
            return Double.parseDouble(amountET.getText().toString());
        }

    }

    private void depositAmount(double amount) {
        double previousAmount = 0;
        long targetAmount = 0; // to check goal is completed or not
        int alarmID = 0;

        Cursor cursor = getContentResolver().query(DbContract.GOALS.CONTENT_URI.buildUpon().appendPath(itemID).build(), null,null,null,null);
        if(cursor!=null)
        {
            if(cursor.moveToFirst())
            {
                previousAmount = cursor.getDouble(cursor.getColumnIndex(DbContract.GOALS.COLUMN_DEPOSITED_AMOUNT));
                targetAmount = cursor.getLong(cursor.getColumnIndex(DbContract.GOALS.COLUMN_TARGET_AMOUNT));
                alarmID = cursor.getInt(cursor.getColumnIndex(DbContract.GOALS.COLUMN_ALARM_ID));
            }
        }

        amount +=previousAmount;

        ContentValues values = new ContentValues();
        values.put(DbContract.GOALS.COLUMN_DEPOSITED_AMOUNT,amount);
        if(targetAmount !=0 && amount>=targetAmount)
        { // if goal is completed
            values.put(DbContract.GOALS.COLUMN_IS_COMPLETED,"true");
            deleteReminder(itemID,alarmID);
            Toast.makeText(this, "Goal Completed :-)", Toast.LENGTH_SHORT).show();
        }


        int itemUpdated = getContentResolver().update(DbContract.GOALS.CONTENT_URI.buildUpon().appendPath(itemID).build(),values,null,null);
//        Toast.makeText(this, itemUpdated+" item updated", Toast.LENGTH_SHORT).show();
        if(cursor!=null) cursor.close();

    }

    private void deleteReminder(String itemID, int alarmID)
    {
        if(alarmID == 0) return;
        Intent alarmIntent = new Intent(this, AlarmService.class);
        alarmIntent.putExtra(AlarmService.KEY_ALARM_ID,alarmID);

        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(
                this,
                alarmID,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        ContentValues values = new ContentValues();
        values.put(DbContract.GOALS.COLUMN_REMINDER,0);
        values.put(DbContract.GOALS.COLUMN_ALARM_ID,0);
        this.getContentResolver().update(DbContract.GOALS.CONTENT_URI.buildUpon().appendPath(itemID).build(),values,null,null);
        SHOW_LOG("Reminder Deleted from the database ");
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(alarmPendingIntent);
        SHOW_LOG("Alarm Also Canceled");

    }

    private void SHOW_LOG(String message) {
        Log.i("123456",message);
    }
}
