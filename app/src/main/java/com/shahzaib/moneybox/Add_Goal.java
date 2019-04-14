package com.shahzaib.moneybox;

import android.Manifest;
import android.annotation.SuppressLint;
//import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.shahzaib.moneybox.Model.Currency;
import com.shahzaib.moneybox.Model.Goal;
import com.shahzaib.moneybox.ThreadPoolExecutor.ExecutorSupplier;
import com.shahzaib.moneybox.database.DbContract;
import com.shahzaib.moneybox.Dialogs.CalendarDialog;
import com.shahzaib.moneybox.Dialogs.ClockDialog;
import com.shahzaib.moneybox.Dialogs.Dialog;
//import com.shahzaib.moneybox.utils.SharedPreferencesUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import alarm_utils.AlarmService;
import de.hdodenhof.circleimageview.CircleImageView;

public class Add_Goal extends AppCompatActivity
        implements View.OnClickListener, DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {
    public static final String KEY_INTENT_ITEM_ID = "itemID";
    public static final String SP_KEY_ALARM_ID = "alarmID";
    public static final String SP_ALARM = "alarmSharedPreference";
    public static final String SP_KEY_IMAGE_ID = "uniqueImageID";
    public static final String SP_UNIQUE_IMAGE_ID = "uniqueImageIDSP";
    public static final String KEY_INSTANCE_STATE_GOAL_TITLE = "goalTitle";
    public static final String KEY_INSTANCE_STATE_GOAL_AMOUNT = "goalAmount";
    public static final String KEY_INTENT_CURRENCY_COUNTRY_NAME = "countryName";
    public static final String KEY_INTENT_CURRENCY_CODE = "currencyCode";
    public static final String KEY_INTENT_CURRENCY_SYMBOL = "currencySymbol";

    final int REQUEST_CODE_IMAGE_PIC = 100;
    final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 101;
    final int REQUEST_CODE_CURRENCY = 102;

    TextView targetDateTV, savingFrequencyTV, reminderTimeTV, reminderDayTV, reminderDateTV, toolbar_text,goalCurrencyTV;
    ImageButton ic_done, ic_close;
    EditText goalTitleET, targetAmountET;
    CircleImageView goalImageIV;
    ProgressBar progressBar;
    LinearLayout targetDateContainer, savingFrequencyContainer, reminderTimeContainer, reminderDayContainer, reminderDateContainer, goalCurrencyContainer;
    SwitchCompat reminderToggle;


    //******* helper variables
    boolean isGoalHasPicture = false;
    Bitmap goalPicture;
    String goalPictureName;
    Calendar reminder;
    Goal.SavingFrequency savingFrequency = Goal.SavingFrequency.NOT_PLANNED;
    private String[] savingFrequencies = new String[]{"Not Planned", "Daily", "Weekly", "Monthly"};
    private String[] daysOfWeek = new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private String[] daysOfMonth = new String[]
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                    "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
                    "21", "22", "23", "24", "25", "26", "27", "28", "29", "30",};

    private SparseArray<Goal.SavingFrequency> savingFrequencySparseArray = getSavingFrequencySparseArray();
    Calendar targetDate;
    String itemID;
    int alarmid = 0;
    boolean isItemForUpdate = false;
    Goal goal = new Goal(this);
    ScrollView scrollView;
    boolean isReminderChanged = false;
    boolean isReminderTogglePressed_WhenSFNotPlanned = false; // SF stands for SavingFrequency


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add__goal);
        ic_done = findViewById(R.id.ic_done);
        ic_close = findViewById(R.id.ic_close);
        targetDateTV = findViewById(R.id.targetDateTV);
        goalTitleET = findViewById(R.id.goalTitleET);
        targetAmountET = findViewById(R.id.targetAmountET);
        goalImageIV = findViewById(R.id.goalImageIV);
        targetDateContainer = findViewById(R.id.targetDateContainer);
        reminderToggle = findViewById(R.id.reminderToggle);
        savingFrequencyContainer = findViewById(R.id.savingFrequencyContainer);
        reminderTimeContainer = findViewById(R.id.reminderTimeContainer);
        reminderDayContainer = findViewById(R.id.reminderDayContainer);
        reminderDateContainer = findViewById(R.id.reminderDateContainer);
        goalCurrencyContainer = findViewById(R.id.goalCurrencyContainer);
        savingFrequencyTV = findViewById(R.id.savingFrequencyTV);
        reminderTimeTV = findViewById(R.id.reminderTimeTV);
        reminderDayTV = findViewById(R.id.reminderDayTV);
        reminderDateTV = findViewById(R.id.reminderDateTV);
        goalCurrencyTV = findViewById(R.id.goalCurrencyTV);
        scrollView = findViewById(R.id.scrollView);
        toolbar_text = findViewById(R.id.toolbar_text);
        progressBar = findViewById(R.id.imageLoadingProgressBar);



        itemID = getIntent().getStringExtra(KEY_INTENT_ITEM_ID);
        isItemForUpdate = itemID != null;

        if (savedInstanceState != null) {
            goalTitleET.setText(savedInstanceState.getString(KEY_INSTANCE_STATE_GOAL_TITLE, ""));
            targetAmountET.setText(String.valueOf(savedInstanceState.getLong(KEY_INSTANCE_STATE_GOAL_AMOUNT, 0)));
        }


        if (isItemForUpdate) {// Goal already exist
            toolbar_text.setText("Edit Goal");
            Cursor cursor = getContentResolver().query(DbContract.GOALS.CONTENT_URI.buildUpon().appendPath(itemID).build(),
                    null, null, null, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    goal.setPicture(cursor.getString(cursor.getColumnIndex(DbContract.GOALS.COLUMN_PICTURE_NAME)));
                    goal.setTitle(cursor.getString(cursor.getColumnIndex(DbContract.GOALS.COLUMN_TITLE)));
                    goal.setTargetAmount(cursor.getLong(cursor.getColumnIndex(DbContract.GOALS.COLUMN_TARGET_AMOUNT)));
                    goal.setTargetDateInMillis(cursor.getLong(cursor.getColumnIndex(DbContract.GOALS.COLUMN_TARGET_DATE)));
                    goal.setSavingFrequency(cursor.getString(cursor.getColumnIndex(DbContract.GOALS.COLUMN_SAVING_FREQUENCY)));
                    goal.setReminder(cursor.getLong(cursor.getColumnIndex(DbContract.GOALS.COLUMN_REMINDER)));
                    goal.setGoalCurrency(new Currency(this,
                            cursor.getString(cursor.getColumnIndex(DbContract.GOALS.COLUMN_CURRENCY_COUNTERY)),
                            cursor.getString(cursor.getColumnIndex(DbContract.GOALS.COLUMN_CURRENCY_CODE)),
                            cursor.getString(cursor.getColumnIndex(DbContract.GOALS.COLUMN_CURRENCY_SYMBOL))
                    ));

                    refreshCurrencyData();
                    alarmid = cursor.getInt(cursor.getColumnIndex(DbContract.GOALS.COLUMN_ALARM_ID));

                    //******* bind the UI & also initialize the variables
                    if (goal.getPicture() != null) {
                        goalImageIV.setImageBitmap(goal.getPicture());
                        goalPicture = goal.getPicture();
                        goalPictureName = cursor.getString(cursor.getColumnIndex(DbContract.GOALS.COLUMN_PICTURE_NAME));
                        isGoalHasPicture = true;
                    }
                    goalTitleET.setText(goal.getTitle());
                    targetAmountET.setText(String.valueOf(goal.getTargetAmount()));
                    targetDateTV.setText(goal.getTargetDate());
                    if (targetDate == null) targetDate = Calendar.getInstance();
                    targetDate.setTimeInMillis(goal.getTargetDateInMillis());
                    savingFrequencyTV.setText(parseSavingFrequencyToString(goal.getSavingFrequency()));
                    savingFrequency = goal.getSavingFrequency();
                    if (goal.getReminderInMillis() != 0) {
                        if (reminder == null) reminder = Calendar.getInstance();
                        reminder.setTimeInMillis(goal.getReminderInMillis());
                        reminderToggle.setChecked(true);
                        initializeReminder();
                    }
                }
            }
        } else { // brand new goal
            toolbar_text.setText("Add Goal");
            initializeTargetDate();
            refreshCurrencyData();
        }


        //  ********************************************************* on click listeners
        ic_done.setOnClickListener(this);
        ic_close.setOnClickListener(this);
        goalImageIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePicker();
            }
        });
        targetDateContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTargetDatePicker();
            }
        });
        savingFrequencyContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogForSelectingSavingFrequency();
            }
        });
        reminderToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTheReminder();
            }
        });
        reminderTimeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClock();
            }
        });
        reminderDayContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogForSelectingDay();
            }
        });
        reminderDateContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogForSelectingDate();
            }
        });
        goalCurrencyContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Add_Goal.this, CurrenciesList.class),REQUEST_CODE_CURRENCY);
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ic_done:
                String goalTitle = getTitleOfGoal();
                long targetAmount = getTargetAmount();
                String goalPictureName = getGoalPictureName();
                Calendar targetDate = getTargetDate();

                boolean isDataMissing = checkIsDataMissing(goalTitle, targetAmount);

                if (!isDataMissing) {
                    int alarmID = 0;
                    Calendar reminder = null;

                    boolean isReminderActive = checkIsReminderActive();
                    if (isReminderActive) {
                        reminder = getReminderData();
                        if (isItemForUpdate && isReminderChanged) {
                            updateTheReminder(alarmid, reminder);
                        } else {
                            alarmID = getUniqueAlarmID();
                            setTheReminder(alarmID, reminder);
                        }
                    }
                    if (isItemForUpdate) {
                        updateDataIntoDatabase(itemID, goalTitle, targetAmount, goalPictureName, savingFrequency, targetDate,goal.getGoalCurrency(), reminder, alarmid);
                        finish();
                    } else {
                        saveDataIntoDatabase(goalTitle, targetAmount, goalPictureName, savingFrequency, targetDate,goal.getGoalCurrency(), reminder, alarmID);
//                        Intent returnIntent = new Intent();
//                        returnIntent.putExtra(MainActivity.INTENT_KEY_IS_ITEM_ADDED, true);
//                        setResult(Activity.RESULT_OK, returnIntent);
                        Toast.makeText(this, "New Goal Added", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                break;

            case R.id.ic_close:
                finish();
                break;
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMAGE_PIC) {
            if(data != null)
            {
                goalImageIV.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                setGoalPicture(data);
            }
            else
            {
                isGoalHasPicture = false;
                goalPicture = null;
            }
        } else if(requestCode == REQUEST_CODE_CURRENCY)
        {
            // get data from intent and do what we want
            if(data !=null) {
                Currency selectedCurrency = new Currency(this);
                selectedCurrency.setCountry(data.getStringExtra(KEY_INTENT_CURRENCY_COUNTRY_NAME));
                selectedCurrency.setCode(data.getStringExtra(KEY_INTENT_CURRENCY_CODE));
                selectedCurrency.setSymbol(data.getStringExtra(KEY_INTENT_CURRENCY_SYMBOL));

                goal.setGoalCurrency(selectedCurrency);
                SHOW_LOG("Selected Currency: "+selectedCurrency.toString());
                // change the default currency, also bind data to view and store in variable that will be saved in db
//                SharedPreferencesUtils.setDefaultCurrency(this,selectedCurrency);
                refreshCurrencyData();

            }

        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE) {// check user granted the permission or not

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SHOW_LOG("User Granted the permission");
                // double check, permission is granted or denied by the system by some error like screen overlay
                boolean isHavePermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                if (isHavePermission) {
                    SHOW_LOG("User Has the permission");
                    showImagePicker();
                } else {
                    SHOW_LOG("Permission is denied by some error");
                }

            } else {
                SHOW_LOG("User Denied the permission");
            }

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (getTitleOfGoal() != null)
            outState.putString(KEY_INSTANCE_STATE_GOAL_TITLE, getTitleOfGoal());
        if (getTargetAmount() > 0)
            outState.putLong(KEY_INSTANCE_STATE_GOAL_AMOUNT, getTargetAmount());

    }

    //    ********************************************************** Getters
    private String getTitleOfGoal() {
        String goalTitle = goalTitleET.getText().toString();
        if (goalTitle.length() > 0) {
            return goalTitle;
        } else {
            return null;
        }
    }

    public long getTargetAmount() {
        if (targetAmountET.getText().toString().length() > 0) {
            long targetAmount = Long.parseLong(targetAmountET.getText().toString());
            if (targetAmount > 0) {
                return targetAmount;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    private Bitmap getGoalPicture() {
        if (!isGoalHasPicture) {
            return null;
        } else {
            return goalPicture;
        }

    }

    private Calendar getTargetDate() {
        return targetDate;
    }

    private Goal.SavingFrequency getSavingFrequency() {
        return savingFrequency;
    }

    private Calendar getReminderData() {
        return reminder;
    }

    public String getGoalPictureName() {
        return goalPictureName;
    }


    //    ********************************************************** Setters
    private void setGoalPicture(Intent imageData) {
        if (imageData == null) {
            isGoalHasPicture = false;
            goalPicture = null;
        } else {
            final Uri imageUri = imageData.getData();
            //store image into internal storage and save the uri in the background thread

            // load and save image in background and update imageview
            ExecutorSupplier.getInstance().forBackgroundTasks().execute(new Runnable() {
                private final int IMAGE_NOT_LOADED = 0;
                private final int IMAGE_LOADED = 1;

                Handler handler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        if (msg.arg1 == IMAGE_LOADED) {
                            goalImageIV.setImageBitmap(goalPicture);
                            goalImageIV.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                };


                @Override
                public void run() {
                    try {
                        goalPicture = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        isGoalHasPicture = true;
                        Message message = new Message();
                        message.arg1 = IMAGE_LOADED;
                        handler.sendMessage(message);

                        if (goalPictureName == null) goalPictureName = getUniqueNameForImage();
                        File folder = getFilesDir();
                        File file = new File(folder.getAbsolutePath(), goalPictureName);
                        FileOutputStream fos = new FileOutputStream(file);
                        goalPicture.compress(Bitmap.CompressFormat.JPEG, 10, fos);
                        fos.close();
                        SHOW_LOG("Image Saved or Update in internal Storage successfully");

                    } catch (IOException e) {
                        e.printStackTrace();
                        SHOW_LOG("Error occur while reading/writing Image from the storage");
                    }
                }
            });

        }
    }


    private void setSavingFrequency(int which) {

        if (isReminderTogglePressed_WhenSFNotPlanned && savingFrequencySparseArray.get(which) != Goal.SavingFrequency.NOT_PLANNED) {
            this.savingFrequency = savingFrequencySparseArray.get(which);
            savingFrequencyTV.setText(savingFrequencies[which]);
            reminderToggle.setChecked(true);
            isReminderChanged = true;
            initializeReminder();
            isReminderTogglePressed_WhenSFNotPlanned = false;
        } else {

            this.savingFrequency = savingFrequencySparseArray.get(which);
            savingFrequencyTV.setText(savingFrequencies[which]);
            reminderToggle.setChecked(false);
            isReminderChanged = true;
            initializeReminder();
        }


    }


    //    ********************************************************** Helper methods
    private void showImagePicker() {
        // first check for permission
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            boolean isHavePermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            if (!isHavePermission) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ_EXTERNAL_STORAGE);
                return;
            }
        }


        Intent chooseImageIntent = new Intent(Intent.ACTION_PICK);
        chooseImageIntent.setType("image/*");
        Intent chooser = Intent.createChooser(chooseImageIntent, "Complete action using");
        if (chooser.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(chooser, REQUEST_CODE_IMAGE_PIC);
        } else {
            Toast.makeText(this, "There is no app to perform this action", Toast.LENGTH_SHORT).show();
        }

    }


    private void SHOW_LOG(String message) {
        Log.i("123456", message);
    }

    private void initializeTargetDate() {
        targetDate = Calendar.getInstance();
        targetDateTV.setText(Goal.formatDate(targetDate.getTimeInMillis()));
    }

    private void showTargetDatePicker() {
        CalendarDialog calendarDialog = new CalendarDialog();
        if (isItemForUpdate && targetDate != null) {
            calendarDialog.setCalendar(targetDate);
        }
        calendarDialog.show(getSupportFragmentManager(), "Target Date Picker");
    }

    private SparseArray<Goal.SavingFrequency> getSavingFrequencySparseArray() {
        SparseArray<Goal.SavingFrequency> savingFrequencySparseArray = new SparseArray<>();

        savingFrequencySparseArray.put(0, Goal.SavingFrequency.NOT_PLANNED);
        savingFrequencySparseArray.put(1, Goal.SavingFrequency.DAILY);
        savingFrequencySparseArray.put(2, Goal.SavingFrequency.WEEKLY);
        savingFrequencySparseArray.put(3, Goal.SavingFrequency.MONTHLY);
        return savingFrequencySparseArray;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        targetDate = Calendar.getInstance();
        targetDate.set(Calendar.YEAR, year);
        targetDate.set(Calendar.MONTH, month);
        targetDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        targetDateTV.setText(Goal.formatDate(targetDate.getTimeInMillis()));
    }

    private boolean checkIsDataMissing(String goalTitle, long targetAmount) {
        if (goalTitle != null && targetAmount > 0) {
            return false;
        } else {
            if (goalTitle == null && targetAmount <= 0) {
                SHOW_LOG("Both (Title,Target Amount) are empty ");
                Dialog.showAlertDialog(this, "Please enter title of the goal");
            } else {
                if (goalTitle == null) {
                    SHOW_LOG("Goal Title is empty");
                    Dialog.showAlertDialog(this, "Please enter title of the goal");

                } else {
                    SHOW_LOG("Goal amount is null");
                    Dialog.showAlertDialog(this, "Please enter the goal amount");
                }
            }
            return true;
        }
    }

    private boolean checkIsReminderActive() {
        return savingFrequency != Goal.SavingFrequency.NOT_PLANNED && reminderToggle.isChecked();
    }

    private void setTheReminder(int alarmID, Calendar reminder) {
        /*
         * * Note: as of API 19, all repeating alarms are inexact.  If your
         * application needs precise delivery times then it must use one-time
         * exact alarms, rescheduling each time when it finish
         */


        Calendar currentCalendar = Calendar.getInstance();
        if (!reminder.after(currentCalendar)) {// agr remainder date, current date sy previous(pechy) hy to agr goal save krty hi reminder On ho jaey ga.
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

        Intent alarmIntent = new Intent(this, AlarmService.class);
        alarmIntent.putExtra(AlarmService.KEY_ALARM_ID, alarmID);

        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(
                this,
                alarmID,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
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

            SHOW_LOG("New Alarm is created: " + reminder.getTime());

        } else
            SHOW_LOG("Alarm Manager is Null, in Daily Repeating function");

    }

    private void updateTheReminder(int alarmid, Calendar reminder) {
        // delete the previous reminder and set a new reminder
        deleteReminder(itemID, alarmid);
        setTheReminder(alarmid, reminder);
    }

    private void showDialogForSelectingSavingFrequency() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Add_Goal.this);
        builder.setTitle("Saving Frequency")
                .setItems(savingFrequencies, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setSavingFrequency(which);
                    }
                })
                .show();
    }

    private void toggleTheReminder() {
        if (savingFrequency == Goal.SavingFrequency.NOT_PLANNED) {
            isReminderTogglePressed_WhenSFNotPlanned = true;
            showDialogForSelectingSavingFrequency();
//            Dialog.showAlertDialog(this,"Select saving frequency first !");
//            reminderToggle.setChecked(false);
        } else {
            initializeReminder();
        }
    }

    private void showClock() {
        ClockDialog clockDialog = new ClockDialog();
        clockDialog.show(getSupportFragmentManager(), "Set Time For Reminder");
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        setReminderTime(hourOfDay, minute);
    }

    private void setReminderTime(int hourOfDay, int minute) {
        if (reminder == null || isItemForUpdate) {
            reminder = Calendar.getInstance();
        }
        reminder.set(Calendar.HOUR_OF_DAY, hourOfDay);
        reminder.set(Calendar.MINUTE, minute);
        isReminderChanged = true;
        initializeReminder();
    }

    private void showDialogForSelectingDay() {
        new AlertDialog.Builder(Add_Goal.this)
                .setTitle("Select a Day")
                .setItems(daysOfWeek, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectDayForReminder(which + 1);
                    }
                })
                .show();
    }

    private void selectDayForReminder(int dayOfWeek) {
        if (reminder == null || isItemForUpdate) {
            reminder = Calendar.getInstance();
        }
        reminder.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        isReminderChanged = true;
        initializeReminder();
    }

    private void showDialogForSelectingDate() {
        new AlertDialog.Builder(Add_Goal.this)
                .setTitle("Select a Date")
                .setItems(daysOfMonth, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectDateForReminder(which + 1);
                    }
                })
                .show();
    }

    private void selectDateForReminder(int dayOfMonth) {
        if (reminder == null || isItemForUpdate) {
            reminder = Calendar.getInstance();
        }
        reminder.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        isReminderChanged = true;
        initializeReminder();
    }

    private void initializeReminder() {
        // show the reminder detail according the the savingFrequency & reminder toggle
        if (!reminderToggle.isChecked()) {
            reminderTimeContainer.setVisibility(View.GONE);
            reminderDayContainer.setVisibility(View.GONE);
            reminderDateContainer.setVisibility(View.GONE);
            reminder = null;
            SHOW_LOG("Previous reminder is reset");
            // if activity is in edit mode then, delete reminder from the database
            if (isItemForUpdate) {
                if (alarmid > 0) {
                    deleteReminder(itemID, alarmid);
                }

            }

        } else {
            if (reminder == null) {
                reminder = Calendar.getInstance();
                SHOW_LOG("New Reminder is added");
            }// else user set the alarm

            scrollToBottomOfScreen();
            switch (this.savingFrequency) {
                case DAILY:
                    reminderTimeTV.setText(Goal.formatTime(reminder.getTimeInMillis()));
                    reminderTimeContainer.setVisibility(View.VISIBLE);
                    reminderDayContainer.setVisibility(View.GONE);
                    reminderDateContainer.setVisibility(View.GONE);
                    break;

                case WEEKLY:
                    reminderTimeTV.setText(Goal.formatTime(reminder.getTimeInMillis()));
                    reminderDayTV.setText(daysOfWeek[reminder.get(Calendar.DAY_OF_WEEK) - 1]);
                    reminderTimeContainer.setVisibility(View.VISIBLE);
                    reminderDayContainer.setVisibility(View.VISIBLE);
                    reminderDateContainer.setVisibility(View.GONE);
                    break;

                case MONTHLY:
                    reminderTimeTV.setText(Goal.formatTime(reminder.getTimeInMillis()));
                    if (reminder.get(Calendar.DAY_OF_MONTH) == 31) {
                        reminder.set(Calendar.DAY_OF_MONTH, 30); // we not want to include 31 date
                    }
                    reminderDateTV.setText(daysOfMonth[reminder.get(Calendar.DAY_OF_MONTH) - 1]);
                    // OR   reminderDateTV.setText(reminder.get(Calendar.DAY_OF_MONTH));
                    reminderTimeContainer.setVisibility(View.VISIBLE);
                    reminderDayContainer.setVisibility(View.GONE);
                    reminderDateContainer.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }


    private void saveDataIntoDatabase(String goalTitle, long targetAmount, String goalPictureName, Goal.SavingFrequency savingFrequency, Calendar targetDate,Currency goalCurrency, Calendar reminder, int alarmID) {
        ContentValues values = new ContentValues();
        values.put(DbContract.GOALS.COLUMN_TITLE, goalTitle);
        values.put(DbContract.GOALS.COLUMN_TARGET_AMOUNT, targetAmount);
        values.put(DbContract.GOALS.COLUMN_TARGET_DATE, targetDate.getTimeInMillis());

        values.put(DbContract.GOALS.COLUMN_SAVING_FREQUENCY, savingFrequency.toString());
        if (reminder != null)
            values.put(DbContract.GOALS.COLUMN_REMINDER, reminder.getTimeInMillis());
        values.put(DbContract.GOALS.COLUMN_IS_COMPLETED, "false"); // help to separate completed & unCompleted Goals

        if (alarmID != 0) {
            values.put(DbContract.GOALS.COLUMN_ALARM_ID, alarmID);
        }
        // save image, get the unique name for the image and after storing the image into internal storage,
        // store its URI into database
        if (goalPictureName != null) {
            values.put(DbContract.GOALS.COLUMN_PICTURE_NAME, goalPictureName);
        }

        //save goal currency data into database
        if(goalCurrency!=null) {
            values.put(DbContract.GOALS.COLUMN_CURRENCY_COUNTERY,goalCurrency.getCountry());
            values.put(DbContract.GOALS.COLUMN_CURRENCY_CODE,goalCurrency.getCode());
            values.put(DbContract.GOALS.COLUMN_CURRENCY_SYMBOL,goalCurrency.getSymbol());
        }
        getContentResolver().insert(DbContract.GOALS.CONTENT_URI, values);
    }

    private byte[] convertPictureIntoBytes(Bitmap goalPicture) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        goalPicture.compress(Bitmap.CompressFormat.JPEG, 1, stream);
        byte imageInByte[] = stream.toByteArray();
        return imageInByte;
    }

    private String parseSavingFrequencyToString(Goal.SavingFrequency savingFrequency) {
        if (savingFrequency == null) {
            this.savingFrequency = Goal.SavingFrequency.NOT_PLANNED;
            return "Not Planned";
        }

        if (savingFrequency == Goal.SavingFrequency.NOT_PLANNED) {
            return "Not Planned";
        } else if (savingFrequency == Goal.SavingFrequency.DAILY) {
            return "Daily";
        } else if (savingFrequency == Goal.SavingFrequency.WEEKLY) {
            return "Weekly";
        } else if (savingFrequency == Goal.SavingFrequency.MONTHLY) {
            return "Monthly";
        } else {
            throw new UnsupportedOperationException("Unknown SavingFrequency: " + savingFrequency);
        }
    }

    private void updateDataIntoDatabase(String itemID, String goalTitle, long targetAmount, String goalPictureName, Goal.SavingFrequency savingFrequency, Calendar targetDate,Currency goalCurrency, Calendar reminder, int alarmID) {
        ContentValues values = new ContentValues();
        values.put(DbContract.GOALS.COLUMN_TITLE, goalTitle);
        values.put(DbContract.GOALS.COLUMN_TARGET_AMOUNT, targetAmount);
        values.put(DbContract.GOALS.COLUMN_TARGET_DATE, targetDate.getTimeInMillis());
        if (goalPictureName != null)
            values.put(DbContract.GOALS.COLUMN_PICTURE_NAME, goalPictureName);
        values.put(DbContract.GOALS.COLUMN_SAVING_FREQUENCY, savingFrequency.toString());
        if (reminder == null) values.put(DbContract.GOALS.COLUMN_REMINDER, 0);
        else values.put(DbContract.GOALS.COLUMN_REMINDER, reminder.getTimeInMillis());
        if (alarmID != 0) {
            values.put(DbContract.GOALS.COLUMN_ALARM_ID, alarmID);
        }
        //save goal currency data into database
        if(goalCurrency!=null) {
            values.put(DbContract.GOALS.COLUMN_CURRENCY_COUNTERY,goalCurrency.getCountry());
            values.put(DbContract.GOALS.COLUMN_CURRENCY_CODE,goalCurrency.getCode());
            values.put(DbContract.GOALS.COLUMN_CURRENCY_SYMBOL,goalCurrency.getSymbol());
        }


        Cursor cursor = getContentResolver().query(DbContract.GOALS.CONTENT_URI.buildUpon().appendPath(itemID).build(), null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                long depositedAmount = cursor.getLong(cursor.getColumnIndex(DbContract.GOALS.COLUMN_DEPOSITED_AMOUNT));
                cursor.close();
                if (depositedAmount >= targetAmount) {
                    values.put(DbContract.GOALS.COLUMN_IS_COMPLETED, "true"); // help to separate completed & unCompleted Goals
                } else {
                    values.put(DbContract.GOALS.COLUMN_IS_COMPLETED, "false"); // help to separate completed & unCompleted Goals
                }
            }
        }


        getContentResolver().update(DbContract.GOALS.CONTENT_URI.buildUpon().appendPath(itemID).build(), values, null, null);
        Toast.makeText(this, "Goal Updated", Toast.LENGTH_SHORT).show();
    }

    private int getUniqueAlarmID() {
        SharedPreferences sharedPreferences = getSharedPreferences(SP_ALARM, MODE_PRIVATE);
        int alarmID = sharedPreferences.getInt(SP_KEY_ALARM_ID, 0);
        SHOW_LOG("Alarm ID: " + alarmID);
        sharedPreferences.edit().putInt(SP_KEY_ALARM_ID, ++alarmID).apply();
        return alarmID;
    }

    private void deleteReminder(String itemID, int alarmid) {
        Intent alarmIntent = new Intent(this, AlarmService.class);
        alarmIntent.putExtra(AlarmService.KEY_ALARM_ID, alarmid);

        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(
                this,
                alarmid,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        ContentValues values = new ContentValues();
        values.put(DbContract.GOALS.COLUMN_REMINDER, 0);
        values.put(DbContract.GOALS.COLUMN_ALARM_ID, 0);
        getContentResolver().update(DbContract.GOALS.CONTENT_URI.buildUpon().appendPath(itemID).build(), values, null, null);
        SHOW_LOG("Reminder Deleted from the database ");
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if(alarmManager!=null) alarmManager.cancel(alarmPendingIntent);
        SHOW_LOG("Alarm Also Canceled");
    }

    private void scrollToBottomOfScreen() {
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private String getUniqueNameForImage() {
        SharedPreferences sharedPreferences = getSharedPreferences(SP_UNIQUE_IMAGE_ID, MODE_PRIVATE);
        int uniqueImageID = sharedPreferences.getInt(SP_KEY_IMAGE_ID, 0);
        sharedPreferences.edit().putInt(SP_KEY_IMAGE_ID, ++uniqueImageID).apply();
        String uniqueImageName = "image" + uniqueImageID + ".jpg";
        Log.i("123456", "ImageId: " + uniqueImageID + "\nSaved Image Name: " + uniqueImageName);
        return uniqueImageName;
    }

    private void refreshCurrencyData()
    {
        goalCurrencyTV.setText(goal.getGoalCurrency().getCode());
    }


}


