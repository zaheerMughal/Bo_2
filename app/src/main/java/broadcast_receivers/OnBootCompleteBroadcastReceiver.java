package broadcast_receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;

import com.shahzaib.moneybox.Model.Goal;
import com.shahzaib.moneybox.database.DbContract;

import java.util.Calendar;

import alarm_utils.AlarmService;

import static android.content.Context.ALARM_SERVICE;

public class OnBootCompleteBroadcastReceiver extends BroadcastReceiver {

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED") || intent.getAction().equals("android.intent.action.ACTION_POWER_CONNECTED"))
        {
            SHOW_LOG("boot Completed");
            // get all goals reminder data and set the alarm according to savingFrequency
            this.context = context;

            String goalSelection = DbContract.GOALS.COLUMN_REMINDER+"!=0";
            Cursor cursor = context.getContentResolver().query(DbContract.GOALS.CONTENT_URI,null,goalSelection,null,null);
            if(cursor!=null)
            {
                if(cursor.moveToFirst())
                {
                    do {
                        long reminderTimeInMillis = cursor.getLong(cursor.getColumnIndex(DbContract.GOALS.COLUMN_REMINDER));
                        Goal.SavingFrequency savingFrequency = Goal.getSavingFrequency(cursor.getString(cursor.getColumnIndex(DbContract.GOALS.COLUMN_SAVING_FREQUENCY)));
                        int alarmID = cursor.getInt(cursor.getColumnIndex(DbContract.GOALS.COLUMN_ALARM_ID));
                        if(reminderTimeInMillis != 0 && savingFrequency != Goal.SavingFrequency.NOT_PLANNED && alarmID != 0 ) // just double check
                        {
                            updateReminder(reminderTimeInMillis,savingFrequency,alarmID);
                        }

                    }while (cursor.moveToNext());
                }

            }


        }


    }

    private void updateReminder(long reminderTimeInMillis, Goal.SavingFrequency savingFrequency, int alarmID) {
        /*
         * * Note: as of API 19, all repeating alarms are inexact.  If your
         * application needs precise delivery times then it must use one-time
         * exact alarms, rescheduling each time when it finish
         */

        Calendar reminder = Calendar.getInstance();
        reminder.setTimeInMillis(reminderTimeInMillis);
        Calendar currentCalendar = Calendar.getInstance();



        if (!reminder.after(currentCalendar)) {// agr remainder date, current date sy previous(pechy) hy to agr goal save krty hi reminder On ho jaey ga.
            // goal save krty hi reminder ko on hony sy bachana hy
            Calendar calendar = Calendar.getInstance();

            switch (savingFrequency) {
                case DAILY:
                    // set alarm for upcoming day for selected time
                    reminder.set(Calendar.DAY_OF_MONTH, reminder.get(Calendar.DAY_OF_MONTH) + 1);
                    break;

                case WEEKLY:
                    // set alarm for upcoming day of week for selected time
                    int SELECTED_DAY = reminder.get(Calendar.DAY_OF_WEEK);
                    int CURRENT_DAY = calendar.get(Calendar.DAY_OF_WEEK);

                    if (SELECTED_DAY <= CURRENT_DAY) {
                        reminder.set(Calendar.DAY_OF_MONTH, reminder.get(Calendar.DAY_OF_MONTH) + 7);
                    }

                    break;

                case MONTHLY:
                    // set alarm for upcoming Month for selected time
                    reminder.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
                    break;

            }
        }

        Intent alarmIntent = new Intent(context, AlarmService.class);
        alarmIntent.putExtra(AlarmService.KEY_ALARM_ID, alarmID);

        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(
                context,
                alarmID,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.getTimeInMillis(), alarmPendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminder.getTimeInMillis(), alarmPendingIntent);
                }
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, reminder.getTimeInMillis(), alarmPendingIntent);
            }

            SHOW_LOG("Alarm updated after device reboot: " + reminder.getTime());

        } else
            SHOW_LOG("Alarm Manager is Null, in Daily Repeating function");
    }

    private void SHOW_LOG(String message) {
        Log.i("123456",message);
    }


}
