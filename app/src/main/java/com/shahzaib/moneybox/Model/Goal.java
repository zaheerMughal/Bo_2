package com.shahzaib.moneybox.Model;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.shahzaib.moneybox.database.DbContract;
import com.shahzaib.moneybox.utils.SharedPreferencesUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;

import alarm_utils.AlarmService;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class Goal {

    public static final String TAG ="GoalClass";
    public static final String SP_ALARM = "alarmSharedPreference";
    public static final String SP_KEY_ALARM_ID = "alarmID";




    private Bitmap picture;
    private String goalPictureUniqueName = null;
    private String title = null;
    private long targetDateInMillis = 0;
    private SavingFrequency savingFrequency = SavingFrequency.NOT_PLANNED; // by default not planned
    private long reminder = 0;
    private long targetAmount = 0;
    private double depositedAmount = 0;
    private Context context; // to retrieve image from the database
    private Currency goalCurrency;



    private long id = -1;
    private GoalCurrency currency = null;
    private String error = null;
    private int alarmId = -1;




    public Goal(Context context) {
        this.context = context;
    }







    // database related
    public boolean save(){
        if(isDataValid()){
            Log.i(TAG, "save: all data is valid");

            if(getId() == -1){ // goal is brand new
                // save krny sy pehly remider ko set krna hy (agr user ny active kiya hy to)

                if(getReminderInMillis() != 0){ // agr reminder set hova hy to , set the reminder
                    setTheReminder();
                }

                return insert();
            }else{ // goal already exist, so update the goal
                return update();
            }

        }
        return false;
    }




    private boolean insert(){
        //Now, all data is valid, reminder is created (if any)
        // It's time to insert brand new goal into database

        ContentValues values = new ContentValues();
        values.put(DbContract.GOALS.COLUMN_TITLE, getTitle());
        values.put(DbContract.GOALS.COLUMN_TARGET_AMOUNT, getTargetAmount());
        values.put(DbContract.GOALS.COLUMN_TARGET_DATE, getTargetDateInMillis());
        values.put(DbContract.GOALS.COLUMN_SAVING_FREQUENCY, getSavingFrequency().toString());
        if(getReminderInMillis() != 0){ // agr reminder set hova hy to , set the reminder
            values.put(DbContract.GOALS.COLUMN_REMINDER, getReminderInMillis());
        }
        values.put(DbContract.GOALS.COLUMN_IS_COMPLETED, "false"); // help to separate completed & unCompleted Goals

        if (getAlarmId() != -1) {
            values.put(DbContract.GOALS.COLUMN_ALARM_ID, getAlarmId());
        }

        if (getPictureName() != null) {
            values.put(DbContract.GOALS.COLUMN_PICTURE_NAME, getPictureName());
        }

        //save goal currency data into database
        if(getCurrency()!=null) {
            values.put(DbContract.GOALS.COLUMN_CURRENCY_COUNTERY,getCurrency().getCountry());
            values.put(DbContract.GOALS.COLUMN_CURRENCY_CODE,getCurrency().getCode());
            values.put(DbContract.GOALS.COLUMN_CURRENCY_SYMBOL,getCurrency().getSymbol());
        }

        context.getContentResolver().insert(DbContract.GOALS.CONTENT_URI, values);
        Toast.makeText(context, "New Goal Inserted", Toast.LENGTH_SHORT).show();
        return true;
    }

    private boolean update(){ // update goal into database
        Toast.makeText(context, "Update Goal into database", Toast.LENGTH_SHORT).show();
        return true;
    }





    // ----------- Validation
    private boolean isDataValid(){
        // perform validation, i.e title can't be null
        if(getTitle() == null){
            setError("Please enter title of the goal");
            return false;
        }else if(getTargetAmount() <=0 ){
            setError("Please enter the goal amount");
            return false;
        }

        return true;
    }




    //************************************************************ Getters


    public GoalCurrency getCurrency() {
        return currency;
    }

    public int getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(int alarmId) {
        this.alarmId = alarmId;
    }

    public long getId(){
        return this.id;
    }
    public String getError(){
        return this.error;
    }
    private void setError(String error){
        this.error = error;
    }
    public Currency getGoalCurrency() {
        if(goalCurrency!=null){
            return goalCurrency;
        }else
        {
            return SharedPreferencesUtils.getDefaultCurrency(context);
        }
    }
    public Bitmap getPicture() {
        return picture;
//        if(picture == null)
//        {
//            return BitmapFactory.decodeResource(,R.drawable.default_goal_image);
//        }
//        else
//        {
//            return picture;
//        }
    }
    public String getPictureName()
    {
        return goalPictureUniqueName;
    }
    public File getPictureFileAddress() {
        if(goalPictureUniqueName==null)
        {
            Log.i(TAG,"goalPictureUniqueName == NULL");
            return null;
        }

        File folder = context.getFilesDir();
        File file  = new File(folder.getAbsolutePath(),goalPictureUniqueName);
        Log.i(TAG,goalPictureUniqueName+" File address received.....");
        return file.getAbsoluteFile();
    }
    public String getTitle() {
        return title;
    }
    public String getTargetDate()
    {
        return formatDate(targetDateInMillis);
    }
    public long getTargetDateInMillis()
    {
        return targetDateInMillis;
    }
    public String getRemainingDays() {
        // following example explains how this function calculate the remaining days
        // Example: suppose remaining starting date = may,31,2018 and end date = aug,30,2019;
        //          result:
        //                  1 years 2 months 30 days
        //                  or 14 months 30 days
        //                  or 65 weeks 1 days
        //                  or 456 days


        // years             = totalDays/365 = 456/365 = 1     (extract the remainder)
        //                   = 1 year
        // remainingDays     = totalDays - (years*365) = 456 - (1*365) = 91
        //                   = 91
        // months            = remainingDays / 30.4166 = 91/30.4166 = 2
        //                   = 2 months
        // RemainingDays    = remainingDays - (months*30.4166) = 91- (2*30.4166) = 30
        //                   = 30 days
        // weeks             = newRemainingDays/7 = 30/7 = 4
        //                   = 4
        // days              = newRemainingDays - (weeks*7) = 30 - (28) = 2
        //                   = 2 days
        // result:
        //      1 year, 2 months, 4 weeks, 2 days
        //      OR
        //      1 year, 2 months, 30 days

        if(remainingDays()==0) return "0 days";
        final double DAYS_IN_YEAR = 365;
        final double DAYS_IN_MONTH = 30.4166;

        int year,month, days;
        double totalDays = remainingDays();


        year = (int) (totalDays/DAYS_IN_YEAR);
        month = (int) ((totalDays - (year * DAYS_IN_YEAR)) / DAYS_IN_MONTH);
        days  = (int) ((totalDays - (year * DAYS_IN_YEAR)) - (month * DAYS_IN_MONTH));

        // formatting the output
        String remaining = "";
        if(year != 0)
        {
            remaining += year+" years ";
        }
        if(month != 0)
        {
            if(year != 0)
            {
                remaining +=", ";
            }
            remaining += month+" months ";
        }
        if(days != 0)
        {
            if(month!=0 || year !=0)
            {
                remaining +=", ";
            }
            remaining += days+" days";
        }

        return remaining;
    }
    public String getReminderTime() {
        String reminderTime;
        switch (savingFrequency) {
            case NOT_PLANNED:
                reminderTime = "Not Planned";
                return reminderTime;

            case DAILY:
                if (reminder != 0) {
                    reminderTime = "daily at " + formatTime(reminder);
                    return reminderTime;
                }
                break;

            case WEEKLY:
                if (reminder != 0) {
                    reminderTime = "every " + getWeek(reminder) + " at " + formatTime(reminder);
                    return reminderTime;
                }
                break;

            case MONTHLY:
                if (reminder != 0) {
                    reminderTime = "every " + getDayOfMonth(reminder) + " at " + formatTime(reminder);
                    return reminderTime;
                }
                break;
        }
        return "Not Set"; // reminder not set
    }
    public long getTargetAmount() {
        return targetAmount;
    }
    public String getTargetAmountInString() {
       return appendCurrencySymbol(Goal.separateNumberWithComma(getTargetAmount()));
    }
    public double getDepositedAmount() {
        return depositedAmount;
    }
    public String getDepositedAmountInString() {
        return appendCurrencySymbol(Goal.separateNumberWithComma(getDepositedAmount()));
    }
    public double getRemainingAmount() {
        return (getTargetAmount() - getDepositedAmount());
    }
    public String getRemainingAmountInString() {
        return appendCurrencySymbol(Goal.separateNumberWithComma(getRemainingAmount()));
    }
    public String getSavingNeeded() {

        NumberFormat formatter = new DecimalFormat("###.00");
        //formatter.format(getRemainingAmount());

        switch (savingFrequency)
        {
            case DAILY:
                if(remainingDays()==0){
                    return getRemainingAmountInString()+" /-Daily";
                }
                else {
                    double result =  ((double)getRemainingAmount()  / (double)remainingDays()) ;
                    return appendCurrencySymbol(Goal.separateNumberWithComma(result))  +" /-Daily";
                }

            case WEEKLY:
                if(getRemainingWeeks()==0){
                    return getRemainingAmountInString()+" /-Weekly";
                }
                else
                {
                    double result =  ((double)getRemainingAmount()  / (double)getRemainingWeeks()) ;
                    return appendCurrencySymbol(Goal.separateNumberWithComma(result))  +" /-Weekly";
                }

            case MONTHLY:
                if(getRemainingMonths()==0){
                    return getRemainingAmountInString()+" /-Monthly";
                }
                else
                {
                    double result =  ((double)getRemainingAmount()  / (double)getRemainingMonths()) ;
                    return appendCurrencySymbol(Goal.separateNumberWithComma(result))  +" /-Monthly";
                }

            case NOT_PLANNED:
                return "Not Planned";
        }
        return "Invalid Reminder frequency";
    }
    public String getPercentageCompleted_With_DepositedAmount() {
        if(getPercentageCompleted()!=0)
        {
            return ""+getPercentageCompleted()+"% ("+getDepositedAmountInString()+")";
        }
        else
        {
            return ""+getPercentageCompleted()+"%";
        }
    }
    public int getPercentageCompleted() {
        return (int) ((getDepositedAmount() * 100)/getTargetAmount());
    }
    public SavingFrequency getSavingFrequency() {
        return savingFrequency;
    }
    public  static SavingFrequency getSavingFrequency(String savingFrequency) {
        if(savingFrequency==null)
        {
            return SavingFrequency.NOT_PLANNED;
        }



        if(savingFrequency.equals(SavingFrequency.NOT_PLANNED.toString()))
        {
            return SavingFrequency.NOT_PLANNED;
        }
        else if(savingFrequency.equals(SavingFrequency.DAILY.toString()))
        {
            return SavingFrequency.DAILY;
        }
        else if(savingFrequency.equals(SavingFrequency.WEEKLY.toString()))
        {
            return SavingFrequency.WEEKLY;
        }
        else if(savingFrequency.equals(SavingFrequency.MONTHLY.toString()))
        {
           return SavingFrequency.MONTHLY;
        }
        else
        {
            throw new UnsupportedOperationException("Unknown SavingFrequency: "+savingFrequency);
        }

    }
    public long getReminderInMillis() {
        return reminder;
    }







    //******************************************************* Setters
    public void setId(long id){
        this.id = id;
    }
    public void setCurrency(GoalCurrency currency){
        this.currency = currency;
    }
    public void setGoalCurrency(Currency goalCurrency)
    {
        this.goalCurrency = goalCurrency;
    }
    public void setPictureName(String imageName) {
        goalPictureUniqueName = imageName;
    }
    public  void setPicture(String imageName) {
        if(imageName==null) return;
        goalPictureUniqueName = imageName;
        File folder = context.getFilesDir();
        File file = new File(folder.getAbsolutePath(),imageName);
        try {
            FileInputStream fin = new FileInputStream(file);
            picture = BitmapFactory.decodeStream(fin);
            Log.i("123456", "Image Retrieve successfully");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i("123456","File Not Found Exception in Goal class");
        }
    }
    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }
    public void setPicture(byte[] imageInBytes) {
        if(imageInBytes != null)
        this.picture = BitmapFactory.decodeByteArray(imageInBytes, 0, imageInBytes.length);
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setTargetAmount(long targetAmount) {
        this.targetAmount = targetAmount;
    }
    public void setDepositedAmount(double depositedAmount) {
        this.depositedAmount = depositedAmount;
    }
    public void setTargetDateInMillis(long targetDateInMillis) {
        this.targetDateInMillis = targetDateInMillis;
    }
    public void setSavingFrequency(SavingFrequency savingFrequency) {
        this.savingFrequency = savingFrequency;
    }
    public void setSavingFrequency(String savingFrequency) {
        if(savingFrequency==null)
        {
            this.savingFrequency = SavingFrequency.NOT_PLANNED;
            return;
        }



        if(savingFrequency.equals(SavingFrequency.NOT_PLANNED.toString()))
        {
            this.savingFrequency = SavingFrequency.NOT_PLANNED;
        }
        else if(savingFrequency.equals(SavingFrequency.DAILY.toString()))
        {
            this.savingFrequency = SavingFrequency.DAILY;
        }
        else if(savingFrequency.equals(SavingFrequency.WEEKLY.toString()))
        {
            this.savingFrequency = SavingFrequency.WEEKLY;
        }
        else if(savingFrequency.equals(SavingFrequency.MONTHLY.toString()))
        {
            this.savingFrequency = SavingFrequency.MONTHLY;
        }
        else
        {
            throw new UnsupportedOperationException("Unknown SavingFrequency: "+savingFrequency);
        }

    }
    public void setReminder(long reminder) {
        this.reminder = reminder;
    }












    //***************************************************** Helper functions
    public String appendCurrencySymbol(String amount) {
        if(getGoalCurrency().getSymbol().length() ==1)
        {
            return getGoalCurrency().getSymbol()+" "+amount; //example: $ 124
        }else
        {
            return amount+" "+getGoalCurrency().getSymbol(); //example: 1234 USD
        }
    }
    public static String formatDate(long timeInMillis) {
        if(timeInMillis == 0) return "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);

        String day, year;
        int month;

        day = "" + calendar.get(Calendar.DAY_OF_MONTH);
        month = (calendar.get(Calendar.MONTH));
        year = "" + calendar.get(Calendar.YEAR);


        return day + "," + getMonth(month) + "," + year;
        //Example:
        //return  1,june,2018
    }
    private static String getMonth(int monthNum) {
        String month = "";
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        if (monthNum >= 0 && monthNum <= 11) {
            month = months[monthNum];
        }
        return month;
    }
    public static String formatTime(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);

        String hour, minute, am_pm;
        hour = "" + (calendar.get(Calendar.HOUR));
        if (calendar.get(Calendar.HOUR) == 0) hour = "12";
        minute = "" + calendar.get(Calendar.MINUTE);
        if (calendar.get(Calendar.MINUTE) <= 9) minute = "0" + minute;
        am_pm = "AM";
        if (calendar.get(Calendar.AM_PM) == 1) am_pm = "PM";

        return hour + ":" + minute + " " + am_pm;
    }
    private int remainingDays() {// example: 66 result aya hy, us ka matlb k aj k din ko nikal k 66vain din hmara matloba din hy.
        if(targetDateInMillis == 0) return 0;
        int totalRemainingDays;

        Calendar currentCalendar = Calendar.getInstance();
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.setTimeInMillis(targetDateInMillis);

        if(!targetCalendar.after(currentCalendar)) return 0; // target date is before current date
        // Calculate total remaining Days:
        // case 1: if both, starting and the last date has same year
        if(targetCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR))
        {
            int targetDays  = countDaysFromFirstDay(targetCalendar.getTimeInMillis()) ;
            int currentDays = countDaysFromFirstDay(currentCalendar.getTimeInMillis());
            totalRemainingDays = targetDays - currentDays;
            return totalRemainingDays;
        }
        else
        {
            int totalStartingDays, totalEndDays;
            int temp1, temp2;
            // sub sy pehly starting years k Total Days ko calculate krna hy usi year k end tk
            // phr last year k shuru sy given last date tk Total Days ko calculate krna hy.
            // yahan tk krny k bad hmary pas first year k total days aur last year k total days aajaen gy.

            // ab jo darmiyan k years hain un ko calculate kr k in sub ka result to totalDays main
            // store kr dyna hy. Lets do it


            //********* pehly starting year k Total Days ko calculate krna hy usi year k end tk
            // Note: last day ko b include krna hy so 1 ko last main add krna hy
            temp1 = countDaysFromFirstDay(currentCalendar.getTimeInMillis());
            Calendar endDayCalendar = Calendar.getInstance();
            endDayCalendar.setTimeInMillis(currentCalendar.getTimeInMillis());// current date k year k last day ka calendar
            endDayCalendar.set(Calendar.DAY_OF_MONTH,31);
            endDayCalendar.set(Calendar.MONTH,Calendar.DECEMBER);
            temp2 = countDaysFromFirstDay(endDayCalendar.getTimeInMillis());
            totalStartingDays = (temp2 - temp1)+1;

            //********* phr end year k shuru sy given last date tk Total Days ko calculate krna hy.
            // Note: last main last day ko include nahi krna.
            Calendar firstDayCalender = Calendar.getInstance();
            firstDayCalender.setTimeInMillis(targetCalendar.getTimeInMillis());// target date k year k first day ka calendar
            firstDayCalender.set(Calendar.DAY_OF_MONTH,1);
            firstDayCalender.set(Calendar.MONTH,Calendar.JANUARY);
            temp1 = countDaysFromFirstDay(firstDayCalender.getTimeInMillis());
            temp2 = countDaysFromFirstDay(targetCalendar.getTimeInMillis());
            totalEndDays = temp2 - temp1;
            totalRemainingDays = totalStartingDays+totalEndDays;



            //********** ab jo darmiyan k years hain un ko calculate kr k in sub ka result ko totalDays main
            // add kr dyna hy. Lets do it
            // example: starting date = 1,"jan",2017 / last date =21,"mar",2020
            //
            //          abi tk hum ny 1,"jan",2017 -- 31,"dec",2017 tk sub  totalDays ko count kr liya hy
            //          aur 1,"jan",2020 -- 21,"mar",2020 tk sub days ko count kr k
            //
            //			matlb hum ny first year aur last year k days ko count kr liya hy
            //			ab hum ny drmiyan k years k din ko count krna hy aur TotalDays main add kr dyna hy
            // Let's do it.

            int middleYear = currentCalendar.get(Calendar.YEAR)+1;
            while(middleYear<targetCalendar.get(Calendar.YEAR))
            {
                if(isLeapYear(middleYear))
                {
                    totalRemainingDays +=366;
                }
                else
                {
                    totalRemainingDays += 365;
                }
                middleYear++;
            }

            return totalRemainingDays;
        }
    }
    private int countDaysFromFirstDay(long timeInMillis) {// given year k start sy ab tk kitny din ho gy hain
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);

        int []months = new int[12]; // 0=jan , 1=feb	, 2=mar ... so on
        int totalDays;


        // here is 2 more cases
        // ---> if year is leap year
        if(!isLeapYear(calendar.get(Calendar.YEAR)))
        {
            // for more detail check algorithm project picture
            months[0] = 0;
            months[1] = 31;
            months[2] = 59;
            months[3] = 90;
            months[4] = 120;
            months[5] = 151;
            months[6] = 181;
            months[7] = 212;
            months[8] = 243;
            months[9] = 273;
            months[10] = 304;
            months[11] = 334;
        }
        else // ---> if year is leap year
        {
            months[0] = 0;
            months[1] = 31;
            months[2] = 60;
            months[3] = 91;
            months[4] = 121;
            months[5] = 152;
            months[6] = 182;
            months[7] = 213;
            months[8] = 244;
            months[9] = 274;
            months[10] = 305;
            months[11] = 335;
        }

        // here we calculate the total days
        totalDays = months[calendar.get(Calendar.MONTH)]+calendar.get(Calendar.DAY_OF_MONTH);
        return totalDays;
    }
    private boolean isLeapYear(int year) {
        if(year%4 == 0)
        {
            return true;
        }
        else
        {
            return false;
        }

    }
    private String getWeek(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);

        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
                return "Sunday";
            case Calendar.MONDAY:
                return "Monday";
            case Calendar.TUESDAY:
                return "Tuesday";
            case Calendar.WEDNESDAY:
                return "Wednesday";
            case Calendar.THURSDAY:
                return "Thursday";
            case Calendar.FRIDAY:
                return "Friday";
            case Calendar.SATURDAY:
                return "Saturday";

        }

        return "Invalid Week";
    }
    private String getDayOfMonth(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        switch (calendar.get(Calendar.DAY_OF_MONTH))
        {
            case 1:
                return "1st";
            case 2:
                return "2nd";
            case 3:
                return "3rd";
        }
        return ""+ calendar.get(Calendar.DAY_OF_MONTH)+"th";
    }
    private int getRemainingWeeks()
    {// for calculating, saving need to meet
        return remainingDays()/7;
    }
    private int getRemainingMonths() { // for calculating, saving need to meet
        //example: agr result 1 hy to matlb next month matloba month hy
        return remainingDays()/30;
    }
    public static String separateNumberWithComma(long number){
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(number);
    }
    public static String separateNumberWithComma(double number){
        DecimalFormat formatter = new DecimalFormat("#,###.00");

        if(number==0) return "0";
        else if(number == (long) number) return separateNumberWithComma(((long)number));
        if(number>0 && number < 1)
        {
            String numberFormattedString = formatter.format(number);
            return "0"+numberFormattedString; //example: don't return .25 instead return 0.25
        }
        else if(number>-1 && number <0)
        {
            String numberFormattedString = formatter.format(number);
            numberFormattedString = numberFormattedString.replace('-','0');
            return "-"+numberFormattedString; //example: don't return -.25 instead return -0.25
        }

        return formatter.format(number);
    }



    public enum SavingFrequency {
        DAILY, WEEKLY, MONTHLY, NOT_PLANNED;
    }




    /* ************************     Helper Functions      *******************
     **********************************************************************************/
    private void setTheReminder() {
        /*
         * * Note: as of API 19, all repeating alarms are inexact.  If your
         * application needs precise delivery times then it must use one-time
         * exact alarms, rescheduling each time when it finish
         */


        setAlarmId(getUniqueAlarmID());
        Calendar currentCalendar = Calendar.getInstance();
        Calendar reminder = Calendar.getInstance();
        reminder.setTimeInMillis(getReminderInMillis());

        if (!reminder.after(currentCalendar)) {
            // agr remainder date, current date sy previous(pechy) hy to agr goal save krty hi reminder On ho jaey ga.
            // goal save krty hi reminder ko on hony sy bachana hy
            Goal.SavingFrequency savingFrequency = getSavingFrequency();
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
        alarmIntent.putExtra(AlarmService.KEY_ALARM_ID, getAlarmId());

        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(
                context,
                getAlarmId(),
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

            Log.i(TAG, "New Reminder(Alarm) is created: " + reminder.getTime());

        } else
            Log.i(TAG, "Alarm Manager is Null, no reminder is created" + reminder.getTime());
    }
    private int getUniqueAlarmID() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_ALARM, MODE_PRIVATE);
        int alarmID = sharedPreferences.getInt(SP_KEY_ALARM_ID, 0);
        sharedPreferences.edit().putInt(SP_KEY_ALARM_ID, ++alarmID).apply();
        return alarmID;
    }


}
