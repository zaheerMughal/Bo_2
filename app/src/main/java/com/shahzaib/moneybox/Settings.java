package com.shahzaib.moneybox;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shahzaib.moneybox.Model.Currency;
import com.shahzaib.moneybox.utils.SharedPreferencesUtils;

public class Settings extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    final int REQUEST_CODE_CURRENCY = 555;

    public static final String KEY_INTENT_CURRENCY_COUNTRY_NAME = "countryName";
    public static final String KEY_INTENT_CURRENCY_CODE = "currencyCode";
    public static final String KEY_INTENT_CURRENCY_SYMBOL = "currencySymbol";


    ImageButton ic_close;
    LinearLayout goalCurrencyContainer,rateThisAppContainer, goalsTotalContainer,aboutContainer;
    TextView goalCurrencyTV;
    SwitchCompat goalsTotalToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ic_close = findViewById(R.id.ic_close);
        goalCurrencyContainer = findViewById(R.id.goalCurrencyContainer);
        rateThisAppContainer = findViewById(R.id.rateThisAppContainer);
        goalsTotalContainer = findViewById(R.id.goalsTotalContainer);
        goalCurrencyTV = findViewById(R.id.goalCurrencyTV);
        goalsTotalToggle = findViewById(R.id.goalsTotalToggle);
        aboutContainer = findViewById(R.id.aboutContainer);






        /*  Initaild Data Binding
        *******************/
        goalCurrencyTV.setText(SharedPreferencesUtils.getDefaultCurrency(this).getCode());
        goalsTotalToggle.setChecked(SharedPreferencesUtils.getDefault_ShowGoalsTotal(this));












        /*  Listeners
         *******************/
        goalCurrencyContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Settings.this, CurrenciesList.class),REQUEST_CODE_CURRENCY);

            }
        });

        ic_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        rateThisAppContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goForRateMoneyBox();
            }
        });

        goalsTotalContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleShowGoalsTotal();
            }
        });

        aboutContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.this,About.class));
            }
        });


        goalsTotalToggle.setOnCheckedChangeListener(this);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_CURRENCY)
        {
            // get data from intent and do what we want
            if(data !=null) {
                Currency selectedCurrency = new Currency(this);
                selectedCurrency.setCountry(data.getStringExtra(KEY_INTENT_CURRENCY_COUNTRY_NAME));
                selectedCurrency.setCode(data.getStringExtra(KEY_INTENT_CURRENCY_CODE));
                selectedCurrency.setSymbol(data.getStringExtra(KEY_INTENT_CURRENCY_SYMBOL));
                goalCurrencyTV.setText(selectedCurrency.getCode());

                // change the default currency, also bind data to view and store in variable that will be saved in db
                SharedPreferencesUtils.setDefaultCurrency(this,selectedCurrency);
            }

        }
    }



    private void goForRateMoneyBox() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }


    private void toggleShowGoalsTotal() {
        goalsTotalToggle.toggle();
    }



    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SharedPreferencesUtils.setDefault_ShowGoalsTotal(this,isChecked);
    }
}
