package com.shahzaib.moneybox;

import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.shahzaib.moneybox.Model.Currency;
import com.shahzaib.moneybox.Model.Goal;
import com.shahzaib.moneybox.ThreadPoolExecutor.ExecutorSupplier;
import com.shahzaib.moneybox.database.DbContract;

import de.hdodenhof.circleimageview.CircleImageView;

public class GoalOverview extends AppCompatActivity implements View.OnClickListener {

    public static final String INTENT_KEY_ITEM_ID = "itemID";


    CircleImageView goalImageIV;
    TextView titleTV, percentage_DepositedAmount_TV, targetDateTV, remainingDaysTV, reminderTimeTV;
    TextView targetAmountTV, depositedAmountTV, remainingAmountTV, savingNeededTV;
    ProgressBar progressBar;
    ImageButton ic_deposit, ic_edit, ic_back_arrow, ic_delete,ic_goal_contribution_history;
    Toolbar goalContributionHistoryBottomBar;
    String itemID;
    Goal goal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_overview);


        goal = new Goal(this);
        goalImageIV = findViewById(R.id.goalImageIV);
        titleTV = findViewById(R.id.titleTV);
        percentage_DepositedAmount_TV = findViewById(R.id.percentage_DepositedAmount_TV);
        targetDateTV = findViewById(R.id.targetDateTV);
        remainingDaysTV = findViewById(R.id.remainingDaysTV);
        reminderTimeTV = findViewById(R.id.reminderTimeTV);
        targetAmountTV = findViewById(R.id.targetAmountTV);
        depositedAmountTV = findViewById(R.id.depositedAmountTV);
        remainingAmountTV = findViewById(R.id.remainingAmountTV);
        savingNeededTV = findViewById(R.id.savingNeededTV);
        ic_deposit = findViewById(R.id.ic_deposit);
        ic_edit = findViewById(R.id.ic_edit);
        ic_back_arrow = findViewById(R.id.ic_back_arrow);
        progressBar = findViewById(R.id.progressBar);
        ic_delete = findViewById(R.id.ic_delete);
        ic_goal_contribution_history = findViewById(R.id.ic_goal_contribution_history);
        goalContributionHistoryBottomBar = findViewById(R.id.goalContributionHistoryBottomBar);



        //*********************** On Click listeners
        ic_deposit.setOnClickListener(this);
        ic_edit.setOnClickListener(this);
        ic_back_arrow.setOnClickListener(this);
        ic_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // delete the item, and finish activity
                int itemDeleted = getContentResolver().delete(DbContract.GOALS.CONTENT_URI.buildUpon().appendPath(itemID).build(), null, null);
//                Toast.makeText(GoalOverview.this, itemDeleted + " item Deleted", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        ic_goal_contribution_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contributionHistoryIntent = new Intent(GoalOverview.this, ContributionsHistory.class);
                contributionHistoryIntent.putExtra(ContributionsHistory.KEY_INTENT_ITEM_ID, itemID);
                startActivity(contributionHistoryIntent);
            }
        });
        goalContributionHistoryBottomBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent contributionHistoryIntent = new Intent(GoalOverview.this, ContributionsHistory.class);
                contributionHistoryIntent.putExtra(ContributionsHistory.KEY_INTENT_ITEM_ID, itemID);
                startActivity(contributionHistoryIntent);

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        itemID = getIntent().getStringExtra(INTENT_KEY_ITEM_ID);
        Log.i("111111","itemID received Through Pending Intent: "+itemID);

        //****************** get the data from the data base and store it into Goal object
        final Cursor cursor = getContentResolver().query(DbContract.GOALS.CONTENT_URI.buildUpon().appendPath(itemID).build(),
                null, null, null, null);

        if (cursor != null) {
            if(cursor.moveToFirst())
            {
                // get goal data instead of Photo, because load photo in separate thread
                ExecutorSupplier.getInstance().forBackgroundTasks().execute(new Runnable() {


                    final int DATA_LOADED = 1;
                    Handler handler = new Handler(Looper.getMainLooper())
                    {
                        @Override
                        public void handleMessage(Message msg) {
                            titleTV.setText(goal.getTitle());
                            targetDateTV.setText(goal.getTargetDate());
                            remainingDaysTV.setText(goal.getRemainingDays());
                            reminderTimeTV.setText(goal.getReminderTime());
                            targetAmountTV.setText(goal.getTargetAmountInString());
                            depositedAmountTV.setText(goal.getDepositedAmountInString());
                            remainingAmountTV.setText(goal.getRemainingAmountInString());
                            savingNeededTV.setText(goal.getSavingNeeded());
                            progressBar.setProgress(goal.getPercentageCompleted());
                            if (goal.getPercentageCompleted() < 100) {
                                percentage_DepositedAmount_TV.setText(goal.getPercentageCompleted_With_DepositedAmount());
                            // ic_delete.setVisibility(View.GONE); // optional
                            } else {
                                percentage_DepositedAmount_TV.setText(R.string.goal_completed);
                                findViewById(R.id.savingNeedToMeet).setVisibility(View.GONE);
                                savingNeededTV.setVisibility(View.GONE);
                                ic_edit.setVisibility(View.GONE);
                                ic_deposit.setVisibility(View.GONE);
                                ic_delete.setVisibility(View.VISIBLE);
                            }
                        }
                    };


                    @Override
                    public void run() {
                        goal.setTitle(cursor.getString(cursor.getColumnIndex(DbContract.GOALS.COLUMN_TITLE)));
                        goal.setTargetAmount(cursor.getLong(cursor.getColumnIndex(DbContract.GOALS.COLUMN_TARGET_AMOUNT)));
                        goal.setTargetDateInMillis(cursor.getLong(cursor.getColumnIndex(DbContract.GOALS.COLUMN_TARGET_DATE)));
                        goal.setSavingFrequency(cursor.getString(cursor.getColumnIndex(DbContract.GOALS.COLUMN_SAVING_FREQUENCY)));
                        goal.setReminder(cursor.getLong(cursor.getColumnIndex(DbContract.GOALS.COLUMN_REMINDER)));
                        goal.setDepositedAmount(cursor.getDouble(cursor.getColumnIndex(DbContract.GOALS.COLUMN_DEPOSITED_AMOUNT)));
                        goal.setGoalCurrency(new Currency(GoalOverview.this,
                                cursor.getString(cursor.getColumnIndex(DbContract.GOALS.COLUMN_CURRENCY_COUNTERY)),
                                cursor.getString(cursor.getColumnIndex(DbContract.GOALS.COLUMN_CURRENCY_CODE)),
                                cursor.getString(cursor.getColumnIndex(DbContract.GOALS.COLUMN_CURRENCY_SYMBOL))
                        ));
                        Message message  = new Message();
                        message.arg1 = DATA_LOADED;
                        handler.sendMessage(message);
                    }
                });

                //Now load image in background and set to the ui
                ExecutorSupplier.getInstance().forBackgroundTasks().execute(new Runnable() {

                    final int DATA_LOADED = 1;
                    Handler handler = new Handler(Looper.getMainLooper())
                    {
                        @Override
                        public void handleMessage(Message msg) {
                            // check data loaded or not
                            // bind data to the UI
                            if (goal.getPicture() != null) goalImageIV.setImageBitmap(goal.getPicture());

                        }
                    };

                    @Override
                    public void run() {
                        goal.setPicture(cursor.getString(cursor.getColumnIndex(DbContract.GOALS.COLUMN_PICTURE_NAME)));
                        Message message = new Message();
                        message.arg1 = DATA_LOADED;
                        handler.sendMessage(message);
                    }
                });
            }
        } else {
            SHOW_LOG("Cursor is empty");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ic_deposit:
                Intent intent = new Intent(this, DepositMoney.class);
                intent.putExtra(DepositMoney.KEY_INTENT_ITEM_ID, itemID);
                startActivity(intent);
                break;

            case R.id.ic_edit:
                Intent editGoalIntent = new Intent(this, Add_Goal.class);
                editGoalIntent.putExtra(Add_Goal.KEY_INTENT_ITEM_ID, itemID);
                startActivity(editGoalIntent);
                break;

            case R.id.ic_back_arrow:
                finish();
                break;
        }

    }



    private void SHOW_LOG(String message) {
        Log.i("123456", message);
    }
}
