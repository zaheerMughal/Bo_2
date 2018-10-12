package com.shahzaib.moneybox;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.shahzaib.moneybox.Adapters.GoalsAdapter;
import com.shahzaib.moneybox.database.DbContract;

public class CompletedGoals extends AppCompatActivity  implements LoaderManager.LoaderCallbacks<Cursor>{
    public static final int COMPLETED_GOALS_LIST_LOADER = 2;
    public static  String ADD_MOB_APP_ID;


    ImageButton ic_close;

    RecyclerView completedGoalsRecyclerView;
    GoalsAdapter adapter;
    TextView emptyCompletedGoals;
    AdView completed_goals_list_bottom_ad;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_goals);
        ADD_MOB_APP_ID = getString(R.string.admob_app_id);
        MobileAds.initialize(this, ADD_MOB_APP_ID);

        ic_close = findViewById(R.id.ic_close);
        completedGoalsRecyclerView = findViewById(R.id.completedGoalsRecyclerView);
        emptyCompletedGoals = findViewById(R.id.emptyCompletedGoals);
        completed_goals_list_bottom_ad = findViewById(R.id.completed_goals_list_bottom_add);
        completedGoalsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GoalsAdapter(this);


        getLoaderManager().initLoader(COMPLETED_GOALS_LIST_LOADER,null,this);
        ic_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        requestAndLoadBannerAd(completed_goals_list_bottom_ad);
    }

    private void requestAndLoadBannerAd(AdView bannerAdView) {
//        AdRequest adRequest = new AdRequest.Builder().addTestDevice("6C11C58267C4DD8B942D2272850C1298").addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAdView.loadAd(adRequest);
    }





    //********************** Loader Callbacks
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id)
        {
            case COMPLETED_GOALS_LIST_LOADER:
                String goalSelection = "isCompleted==?";
                String[] goalSelectionArgs = new String[]{"true"};
                return new CursorLoader(getApplicationContext(), DbContract.GOALS.CONTENT_URI,null,goalSelection,goalSelectionArgs,null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
//        if(cursor.getCount()==0) finish();
        if(cursor.getCount()==0){
            emptyCompletedGoals.setVisibility(View.VISIBLE);
        }
        else
        {
            emptyCompletedGoals.setVisibility(View.GONE);
        }
        adapter.setCursor(cursor);
        completedGoalsRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
