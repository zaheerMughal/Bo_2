package com.shahzaib.moneybox;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

//import com.google.android.gms.ads.AdListener;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.InterstitialAd;
//import com.google.android.gms.ads.MobileAds;
import com.shahzaib.moneybox.Adapters.ContributionHistoryAdapter;
import com.shahzaib.moneybox.database.DbContract;

public class ContributionsHistory extends AppCompatActivity  implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final int CONTRIBUTIONS_HISTORY_LOADER = 1;
    public static final String KEY_INTENT_ITEM_ID = "itemID";
    public static  String ADD_MOB_APP_ID;

    ImageButton ic_arrow_back;
    TextView emptyContributionHistoryTV;
//    AdView contributionsListBottomAd;

    RecyclerView contributionsRecyclerView;
    ContributionHistoryAdapter adapter ;
    String itemID ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contributions_history);
        ADD_MOB_APP_ID = getString(R.string.admob_app_id);
//        MobileAds.initialize(this, ADD_MOB_APP_ID);

        ic_arrow_back = findViewById(R.id.ic_back_arrow);
        emptyContributionHistoryTV = findViewById(R.id.emptyContributionHistoryTV);
        contributionsRecyclerView = findViewById(R.id.contributionsRecyclerView);
//        contributionsListBottomAd = findViewById(R.id.contributions_list_bottom_add);
        contributionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contributionsRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        adapter = new ContributionHistoryAdapter(this);

        itemID = getIntent().getStringExtra(KEY_INTENT_ITEM_ID);

        getLoaderManager().initLoader(CONTRIBUTIONS_HISTORY_LOADER,null,this);





        ic_arrow_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
//        requestAndLoadBannerAd(contributionsListBottomAd);
    }

//    private void requestAndLoadBannerAd(AdView bannerAdView) {
//        AdRequest adRequest = new AdRequest.Builder().addTestDevice("6C11C58267C4DD8B942D2272850C1298").addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
////        AdRequest adRequest = new AdRequest.Builder().build();
//        bannerAdView.loadAd(adRequest);
//    }






    //********************** Loader Callbacks
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id)
        {
            case CONTRIBUTIONS_HISTORY_LOADER:
                String goalSelection = DbContract.CONTRIBUTION_HISTORY._ID+"="+itemID;
                return new CursorLoader(getApplicationContext(), DbContract.CONTRIBUTION_HISTORY.CONTENT_URI,null,goalSelection,null,null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(cursor.getCount()==0) emptyContributionHistoryTV.setVisibility(View.VISIBLE);
        else emptyContributionHistoryTV.setVisibility(View.GONE);
        adapter.setCursor(cursor);
        contributionsRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }



}
