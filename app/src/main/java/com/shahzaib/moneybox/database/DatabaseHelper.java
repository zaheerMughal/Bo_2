package com.shahzaib.moneybox.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "Glla.db";
    private static final int DATABASE_VERSION = 3;


    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_GOALS_TABLE = "CREATE TABLE "+DbContract.GOALS.TABLE_NAME+
                "(" +
                DbContract.GOALS._ID+" INTEGER PRIMARY KEY AUTOINCREMENT," +
                DbContract.GOALS.COLUMN_TITLE+" TEXT NOT NULL," +
                DbContract.GOALS.COLUMN_PICTURE_NAME +" String, " +
                DbContract.GOALS.COLUMN_TARGET_AMOUNT +" INTEGER NOT NULL, " +
                DbContract.GOALS.COLUMN_DEPOSITED_AMOUNT +" REAL, " + //changed From Integer to Real
                DbContract.GOALS.COLUMN_TARGET_DATE+" INT8 NOT NULL," +
                DbContract.GOALS.COLUMN_IS_COMPLETED+" TEXT," +
                DbContract.GOALS.COLUMN_ALARM_ID+" INTEGER," +
                DbContract.GOALS.COLUMN_SAVING_FREQUENCY+" INTEGER," +
                DbContract.GOALS.COLUMN_REMINDER +" INT8," +
                DbContract.GOALS.COLUMN_CURRENCY_COUNTERY +" TEXT," +
                DbContract.GOALS.COLUMN_CURRENCY_CODE +" TEXT," +
                DbContract.GOALS.COLUMN_CURRENCY_SYMBOL +" TEXT" +
                ");";
        final String CREATE_REQUEST_CODES_TABLE = "CREATE TABLE "+ DbContract.NOTIFICATION_IDs.TABLE_NAME+
                "(" +
                DbContract.NOTIFICATION_IDs._ID+" INTEGER,"+
                DbContract.NOTIFICATION_IDs.COLUMN_NotificationID +" INTEGER"+
                ")";

        final String CREATE_CONTRIBUTIONS_HISTORY_TABLE = "CREATE TABLE "+ DbContract.CONTRIBUTION_HISTORY.TABLE_NAME+
                "(" +
                DbContract.CONTRIBUTION_HISTORY._ID+" INTEGER,"+
                DbContract.CONTRIBUTION_HISTORY.COLUMN_DATE +" INTEGER,"+
                DbContract.CONTRIBUTION_HISTORY.COLUMN_AMOUNT +" REAL"+ // changed From Integer to Real
                ")";


        db.execSQL(CREATE_GOALS_TABLE);
        db.execSQL(CREATE_REQUEST_CODES_TABLE);
        db.execSQL(CREATE_CONTRIBUTIONS_HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("232323","Old version:"+ oldVersion);
        Log.i("232323","New version:"+ newVersion);

        if(oldVersion==2 && newVersion==3)
        {
            // do nothing, don't loss user data this time
        }
        else if(oldVersion==1 && newVersion==3)
        {

            // alter table and add columns, because don't loss user data
            final String ALTER_GOALS_TABLE_ADD_COUNTRY_COLUMN
                    = "ALTER TABLE "+DbContract.GOALS.TABLE_NAME+" ADD COLUMN "+DbContract.GOALS.COLUMN_CURRENCY_COUNTERY+" TEXT";
            db.execSQL(ALTER_GOALS_TABLE_ADD_COUNTRY_COLUMN);

            final String ALTER_GOALS_TABLE_ADD_SYMBOL_COLUMN
                    = "ALTER TABLE "+DbContract.GOALS.TABLE_NAME+" ADD COLUMN "+DbContract.GOALS.COLUMN_CURRENCY_SYMBOL+" TEXT";
            db.execSQL(ALTER_GOALS_TABLE_ADD_SYMBOL_COLUMN);

            final String ALTER_GOALS_TABLE_ADD_CODE_COLUMN
                    = "ALTER TABLE "+DbContract.GOALS.TABLE_NAME+" ADD COLUMN "+DbContract.GOALS.COLUMN_CURRENCY_CODE+" TEXT";
            db.execSQL(ALTER_GOALS_TABLE_ADD_CODE_COLUMN);

        }

    }


}
