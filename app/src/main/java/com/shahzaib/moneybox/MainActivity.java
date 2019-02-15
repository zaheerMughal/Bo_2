package com.shahzaib.moneybox;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;



import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
//import com.facebook.ads.*;

import com.shahzaib.moneybox.Adapters.GoalsAdapter;
import com.shahzaib.moneybox.database.DbContract;
import com.shahzaib.moneybox.utils.SharedPreferencesUtils;

public class MainActivity extends AppCompatActivity  implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    public static final int GOALS_LIST_LOADER = 1;
    public static final int REQUEST_CODE_NEW_ITEM_ADDED = 2;
    public static final String INTENT_KEY_IS_ITEM_ADDED = "isItemAdded";
//    public static final String FB_BANNER_AD_PLACEMENT_ID = "1467991256671380_1467992480004591";
//    public static final String FB_BANNER_AD_PLACEMENT_ID = "YOUR_PLACEMENT_ID";



    ImageButton  ic_add_goal,ic_completed_goals,ic_settings;
    Button addGoalBtn;
    RecyclerView goalRecyclerView;
    GoalsAdapter adapter;
    TextView emptyGoalListTV;
    int totalNumberOfGoals=0;

    // facebook ad
//    private AdView adView;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ic_settings = findViewById(R.id.ic_settings);
        ic_add_goal = findViewById(R.id.ic_add_goal);
        ic_completed_goals = findViewById(R.id.ic_completed_goals);
        addGoalBtn = findViewById(R.id.addGoalBtn);
        emptyGoalListTV = findViewById(R.id.emptyGoalListTV);
        goalRecyclerView = findViewById(R.id.goalsRecyclerView);
        goalRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GoalsAdapter(this);

        getLoaderManager().initLoader(GOALS_LIST_LOADER,null,this);

        //*********** On click listeners
        ic_add_goal.setOnClickListener(this);
        addGoalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,Add_Goal.class);
                startActivityForResult(intent,REQUEST_CODE_NEW_ITEM_ADDED);
            }
        });
        ic_completed_goals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,CompletedGoals.class));
            }
        });
        ic_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,Settings.class));
            }
        });


        /************ Face book ad related
         ***************************************/
//        adView = new AdView(this, FB_BANNER_AD_PLACEMENT_ID, AdSize.BANNER_HEIGHT_50);
        // Find the Ad Container
//        LinearLayout adContainer = (LinearLayout) findViewById(R.id.banner_container);
        // Add the ad view to your activity layout
//        adContainer.addView(adView);

//        AdSettings.addTestDevice("a23f71e7-1375-45fb-96f8-eb506a904ae2");

//         Request an ad
//        adView.loadAd();


    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.setShowGoalsTotal(SharedPreferencesUtils.getDefault_ShowGoalsTotal(this));

    }

    @Override
    protected void onDestroy() {
//        if (adView != null) {
//            adView.destroy();
//        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.ic_add_goal:
                Intent intent = new Intent(this,Add_Goal.class);
                startActivityForResult(intent,REQUEST_CODE_NEW_ITEM_ADDED);
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case REQUEST_CODE_NEW_ITEM_ADDED:
                if(data == null) return;
                if(data.getBooleanExtra(INTENT_KEY_IS_ITEM_ADDED,false))
                {
//                    scrollToPosition(totalNumberOfGoals);
                }
                break;
        }
    }















    //********************** Loader Callbacks
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id)
        {
            case GOALS_LIST_LOADER:
                String goalSelection = "isCompleted!=?";
                String[] goalSelectionArgs = new String[]{"true"};
                return new CursorLoader(getApplicationContext(), DbContract.GOALS.CONTENT_URI,null,goalSelection,goalSelectionArgs,null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(cursor.getCount()==0)
        {
            emptyGoalListTV.setVisibility(View.VISIBLE);
            addGoalBtn.setVisibility(View.VISIBLE);
        }
        else {
            emptyGoalListTV.setVisibility(View.GONE);
            addGoalBtn.setVisibility(View.GONE);
        }
        adapter.setCursor(cursor);
        goalRecyclerView.setAdapter(adapter);
        totalNumberOfGoals = cursor.getCount();

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }









    //************************** Helper Methods
    private void scrollToPosition(final int position) {
        goalRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                goalRecyclerView.smoothScrollToPosition(position);
            }

        });
    }


}
