package com.shahzaib.moneybox;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.shahzaib.moneybox.Adapters.CurrenciesAdapter;
import com.shahzaib.moneybox.Model.Currency;
import com.shahzaib.moneybox.ThreadPoolExecutor.ExecutorSupplier;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class CurrenciesList extends AppCompatActivity implements CurrenciesAdapter.OnCurrencyClickListener{


    RecyclerView currenciesRecyclerView;
    CurrenciesAdapter adapter;
    EditText searchEditText;
    ImageButton ic_clear,ic_arrowBack;
    ArrayList<Currency> currenciesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currencies_list);
        searchEditText = findViewById(R.id.searchEditText);
        ic_clear = findViewById(R.id.ic_clear);
        ic_arrowBack = findViewById(R.id.ic_arrowBack);
        currenciesRecyclerView = findViewById(R.id.currenciesRecyclerView);
        adapter = new CurrenciesAdapter(this,this);
        currenciesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        currenciesRecyclerView.setHasFixedSize(true);


        parseJsonToCurrenciesList_andSetDataToAdapter();



        searchEditText.addTextChangedListener(new SearchToolbarTextChangeListener());
        ic_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEditText.setText("");
            }
        });
        ic_arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }




    @Override
    public void clickedItemData(Currency currencyData) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(Add_Goal.KEY_INTENT_CURRENCY_COUNTRY_NAME,currencyData.getCountry());
        resultIntent.putExtra(Add_Goal.KEY_INTENT_CURRENCY_CODE,currencyData.getCode());
        resultIntent.putExtra(Add_Goal.KEY_INTENT_CURRENCY_SYMBOL,currencyData.getSymbol());

        setResult(RESULT_OK,resultIntent);
        finish();
    }

    class  SearchToolbarTextChangeListener implements TextWatcher
    {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String query = s.toString();

            if(query.length()<=0)
            {
                ic_clear.setVisibility(View.GONE);
                adapter.setFilteredList(currenciesList);
            }
            else
            {
                ic_clear.setVisibility(View.VISIBLE);
            }
            filterResult(query);
        }
    }









    /* Helper Methods***********/
    public String loadJSONFromAsset() {
        String json = "";
        try {
            InputStream is = getAssets().open("currencies.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public void parseJsonToCurrenciesList_andSetDataToAdapter()
    {

        ExecutorSupplier.getInstance().forBackgroundTasks().execute(new Runnable() {
            private final int DATA_LOADED = 1;

            ArrayList<Currency> currenciesArrayList = new ArrayList<>();

            Handler handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (msg.arg1 == DATA_LOADED) {
                        if(currenciesArrayList!=null)
                        {
                            currenciesList = currenciesArrayList;
                            adapter.setData(currenciesArrayList);
                            currenciesRecyclerView.setAdapter(adapter);
                        }

                    }
                }
            };


            @Override
            public void run() {

                /*
                {
                  "country": "NONE",
                  "code": "NONE",
                  "symbol": ""
                },
                */
                String jsonData = loadJSONFromAsset();
                if(jsonData!=null)
                {
                    try {
                        currenciesArrayList = new ArrayList<>();
                        Currency currency;

                        JSONObject jsonObject = new JSONObject(jsonData);
                        JSONArray jsonArray = jsonObject.getJSONArray("currencies_list");
                        for(int i=0; i<jsonArray.length(); i++)
                        {
                            currency = new Currency(CurrenciesList.this);
                            currency.setCountry(jsonArray.getJSONObject(i).getString("country"));
                            currency.setSymbol(jsonArray.getJSONObject(i).getString("symbol"));
                            currency.setCode(jsonArray.getJSONObject(i).getString("code"));
                            currenciesArrayList.add(currency);
                        }

                        Message message = new Message();
                        message.arg1 = DATA_LOADED;
                        handler.sendMessage(message);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    JsonArray jsonArray = new JsonArray()
//                  Gson gson = new Gson();
//                  Currency[] currenciesList = gson.fromJson(jsonData,Currency[].class);
//                  currenciesArrayList =new ArrayList<Currency>(Arrays.asList(currenciesList));
//                  Message message = new Message();
//                  message.arg1 = DATA_LOADED;
//                  handler.sendMessage(message);
                }
            }
        });

//        String jsonData = loadJSONFromAsset();
//        if(jsonData!=null)
//        {
//            if(jsonData.length()>0)
//            {
//                Gson gson = new Gson();
//                Currency[] currenciesList = gson.fromJson(jsonData,Currency[].class);
//                adapter.setData(new ArrayList<Currency>(Arrays.asList(currenciesList)));
//                currenciesRecyclerView.setAdapter(adapter);
//            }
//        }
    }



    private void filterResult(String query) {
        ArrayList<Currency> filteredCurrenciesList = new ArrayList<>();

        for(Currency currentItem : currenciesList)
        {
            if(currentItem.getCountry().toLowerCase().contains(query.toLowerCase())
                    || currentItem.getCode().toLowerCase().contains(query.toLowerCase())
                    || currentItem.getSymbol().toLowerCase().contains(query.toLowerCase()))
            {
                filteredCurrenciesList.add(currentItem);
            }
        }

        adapter.setFilteredList(filteredCurrenciesList);
    }





}
