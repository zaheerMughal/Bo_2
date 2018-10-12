package alarm_utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.shahzaib.moneybox.Model.Goal;
import com.shahzaib.moneybox.database.DbContract;
import com.shahzaib.moneybox.DepositMoney;
import com.shahzaib.moneybox.GoalOverview;
import com.shahzaib.moneybox.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Calendar;


import static android.content.Context.MODE_PRIVATE;

public class AlarmService extends BroadcastReceiver {
    public static final String KEY_ALARM_ID = "alarmID";
    private final static String NOTIFICATION_CHANNEL_ID = "GoalsNotificationChannel";
    private final static String NOTIFICATION_CHANNEL_NAME = "Goals";
    private static final String NOTIFICATION_COUNT_SP = "GoalsNotificationCountSP";
    private static final String KEY_SP_NOTIFICATION_ID = "NotificationID";
    private static final String PENDING_INTENT_REQUEST_CODE_SP = "PendingIntentID";
    private static final String KEY_SP_REQUEST_CODE = "RequestCode";
    private static final String SP_UNIQUE_NUMBER  = "uniqueNumber";
    private static final String SP_KEY_UNIQUE_NUMBER = "number";


    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        String alarmID = String.valueOf(intent.getIntExtra(KEY_ALARM_ID,-1)); // get alarmID to update the alarm
        String itemID = "";
        String title = "";

        Log.i("111111","alarmID received: "+alarmID);
        String selection = DbContract.GOALS.COLUMN_ALARM_ID+"="+alarmID;
        Cursor cursor = context.getContentResolver().query(DbContract.GOALS.CONTENT_URI,null,selection,null,null);
        if(cursor!=null) {
            if(cursor.moveToFirst())
            {
                itemID = cursor.getString(cursor.getColumnIndex(DbContract.GOALS._ID));
                title = cursor.getString(cursor.getColumnIndex(DbContract.GOALS.COLUMN_TITLE));
                cursor.close();
            }
        }


        showNotification(itemID,title);
        repeatAlarm(alarmID);
    }




    //**************************************** Helper Methods
    private void SHOW_LOG(String message)
    {
        Log.i("123456",message);
    }
    private void repeatAlarm(String alarmID) {
        long finishedReminderTimeInMillis;
        Goal.SavingFrequency savingFrequency;

        //********** Rescheduling Alarms
        String selection = DbContract.GOALS.COLUMN_ALARM_ID+"="+alarmID;
        Cursor cursor = context.getContentResolver().query(DbContract.GOALS.CONTENT_URI,null,selection,null,null);
        if(cursor!=null)
        {
            if(cursor.moveToFirst())
            {
                savingFrequency = Goal.getSavingFrequency(cursor.getString(cursor.getColumnIndex(DbContract.GOALS.COLUMN_SAVING_FREQUENCY)));
                finishedReminderTimeInMillis = cursor.getLong(cursor.getColumnIndex(DbContract.GOALS.COLUMN_REMINDER));
                String itemID = String.valueOf(cursor.getLong(cursor.getColumnIndex(DbContract.GOALS._ID)));
                cursor.close();
                switch (savingFrequency)
                {
                    case DAILY:
                        setAlarmForUpComingDay(finishedReminderTimeInMillis, Integer.parseInt(alarmID),itemID);
                        break;
                    case WEEKLY:
                        setAlarmForUpComingWeek(finishedReminderTimeInMillis, Integer.parseInt(alarmID),itemID);
                        break;
                    case MONTHLY:
                        setAlarmForUpComingMonth(finishedReminderTimeInMillis, Integer.parseInt(alarmID),itemID);
                        break;
                }
            }
        }
        else SHOW_LOG("Cursor is NULL, in AlarmService");
    }

    private void setAlarmForUpComingDay(long finishedReminderTimeInMillis, int alarmID,String itemId) {
        //******** setting the upComing Day
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(finishedReminderTimeInMillis);

        calendar.set(Calendar.DAY_OF_MONTH,calendar.get(Calendar.DAY_OF_MONTH)+1);

        //**** setting the alarm
        setAlarm(calendar.getTimeInMillis(),alarmID,itemId);
    }
    private void setAlarmForUpComingWeek(long finishedReminderTimeInMillis, int alarmID,String itemId) {
        //************* setting the upComing Week
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(finishedReminderTimeInMillis);

        calendar.set(Calendar.DAY_OF_MONTH,calendar.get(Calendar.DAY_OF_MONTH)+7);

        //******** setting the alarm
        setAlarm(calendar.getTimeInMillis(),alarmID,itemId);
    }
    private void setAlarmForUpComingMonth(long finishedReminderTimeInMillis, int alarmID,String itemId) {
        //******* setting the UpComing Month
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(finishedReminderTimeInMillis);

        calendar.set(Calendar.MONTH,calendar.get(Calendar.MONTH)+1);

        //********** Setting the alarm
        setAlarm(calendar.getTimeInMillis(),alarmID,itemId);
    }
    private void setAlarm(long reminderTimeInMillis, int alarmID,String itemId) {
        Intent alarmIntent = new Intent(context, AlarmService.class);
        alarmIntent.putExtra(AlarmService.KEY_ALARM_ID,alarmID);

        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(
                context,
                alarmID,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(alarmManager!=null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP,reminderTimeInMillis,alarmPendingIntent);
            }
            else
            {
                alarmManager.set(AlarmManager.RTC_WAKEUP,reminderTimeInMillis,alarmPendingIntent);
            }


            Calendar temp = Calendar.getInstance(); // just for Outputing results
            temp.setTimeInMillis(reminderTimeInMillis);
            SHOW_LOG("UpComing alarm is created: "+temp.getTime());


            // Now Update the reminder time in Database
            ContentValues values = new ContentValues();
            values.put(DbContract.GOALS.COLUMN_REMINDER,reminderTimeInMillis);
            int itemUpdated = context.getContentResolver().update(DbContract.GOALS.CONTENT_URI.buildUpon().appendPath(itemId).build(),values,null,null);
            SHOW_LOG(itemUpdated+" Reminder Time Updated In Database");
        }
        else
            SHOW_LOG("Alarm Manager is Null, in Daily Repeating function");
    }

    private void showNotification(String itemID, String title){

        // Build the Content of notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title);
        builder.setSmallIcon(R.drawable.ic_deposit);
        builder.setContentText("Add contribution to your goal");
        builder.setLargeIcon(getGoalPicture(context,itemID));
        builder.setDefaults(Notification.DEFAULT_VIBRATE);
        builder.setDefaults(Notification.DEFAULT_SOUND);
        builder.setAutoCancel(true); // when user click , it disappear
        builder.setContentIntent(goalOverViewPendingIntent(context,itemID)); // open goal overview activity when user click on notification
        builder.addAction(R.drawable.ic_deposit_black, "Deposit Money",depositMoneyPendingIntent(context,itemID));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
            builder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // show the Notification & also create channel for devices running android O
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel nChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(nChannel);
        }

        // following code will create each time different notification
        SharedPreferences sharedPreferences = context.getSharedPreferences(NOTIFICATION_COUNT_SP, MODE_PRIVATE);
        int NOTIFICATION_ID = sharedPreferences.getInt(KEY_SP_NOTIFICATION_ID, 0);
        sharedPreferences.edit().putInt(KEY_SP_NOTIFICATION_ID, ++NOTIFICATION_ID).apply();
        if(notificationManager!=null)notificationManager.notify(NOTIFICATION_ID, builder.build());

        // Save the NotificationID, it will help to cancel notification on button click
        Log.i("123456","Notification ID saved: "+NOTIFICATION_ID);
        ContentValues values = new ContentValues();
        values.put(DbContract.NOTIFICATION_IDs._ID,itemID);
        values.put(DbContract.NOTIFICATION_IDs.COLUMN_NotificationID,NOTIFICATION_ID);
        context.getContentResolver().insert(DbContract.NOTIFICATION_IDs.CONTENT_URI,values);
    }


    private PendingIntent goalOverViewPendingIntent(Context context,String itemID) {
        Intent intent = new Intent(context,GoalOverview.class);
        intent.putExtra(GoalOverview.INTENT_KEY_ITEM_ID,itemID);
        Log.i("111111","itemID passed Through Pending Intent: "+itemID);

        // following will help, to create different pending intent for each notification
        SharedPreferences sharedPreferences = context.getSharedPreferences("TEMP", MODE_PRIVATE);
        int temp = sharedPreferences.getInt("tempKey", 0);
        sharedPreferences.edit().putInt("tempKey", ++temp).apply();
        return PendingIntent.getActivity(context,temp,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent depositMoneyPendingIntent(Context context, String itemID) {
        Intent intent = new Intent(context, DepositMoney.class);
        intent.putExtra(DepositMoney.KEY_INTENT_ITEM_ID,itemID);
        intent.putExtra(DepositMoney.KEY_INTENT_IS_OPEN_THROUGH_NOTIFICATION,true);

        // following will help, to create different pending intent for each notification
        SharedPreferences sharedPreferences = context.getSharedPreferences("TEMP", MODE_PRIVATE);
        int temp = sharedPreferences.getInt("tempKey", 0);
        sharedPreferences.edit().putInt("tempKey", ++temp).apply();
        return PendingIntent.getActivity(context,temp,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    }
    public static void cancelNotification(Context context, int notificationID) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationID);
        Log.i("123456","Notification canceled ID: "+notificationID);

    }

    private Bitmap getGoalPicture(Context context, String itemID) {

        Cursor cursor = context.getContentResolver().query(DbContract.GOALS.CONTENT_URI.buildUpon().appendPath(itemID).build(),
                null, null, null, null);

        if(cursor!=null)
        {
            if(cursor.moveToFirst())
            {
                String imageName = cursor.getString(cursor.getColumnIndex(DbContract.GOALS.COLUMN_PICTURE_NAME));
                if(imageName!=null)
                {
                    File folder = context.getFilesDir();
                    File file = new File(folder.getAbsolutePath(),imageName);
                    try {
                        FileInputStream fin = new FileInputStream(file);
                        Bitmap image =  BitmapFactory.decodeStream(fin);
                        Log.i("123456","returning User Selected image for notification");
                        cursor.close();
                        return getRoundedBitmap(context,image);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Log.i("123456","File Not Found Exception from NotificationUTILS");
                    }
                }
            }
        }
        Log.i("123456","returning default goal image for notification");
        return BitmapFactory.decodeResource(context.getResources(),R.drawable.default_goal_image);
    }
    private Bitmap getRoundedBitmap(Context context,Bitmap bitmap) {
        Bitmap output;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } else {
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        float r = 0;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            r = bitmap.getHeight() / 2;
        } else {
            r = bitmap.getWidth() / 2;
        }

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }


}
