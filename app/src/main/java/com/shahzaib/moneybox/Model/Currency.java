package com.shahzaib.moneybox.Model;


import android.content.Context;

import com.shahzaib.moneybox.utils.SharedPreferencesUtils;

public class Currency {

    private  String country ="";
    private  String code = "";
    private  String symbol ="";
    private Context context;


    public Currency(Context context){
        this.context = context;
    }

    public Currency(Context context,String country, String code, String symbol) {
        this.context = context;
        this.country = country;
        this.code = code;
        this.symbol = symbol;
    }


    public String getCountry() {
        if(country==null)
        {
            setDeaultCurrency();
        }
        return country;
    }

    public String getCode() {
        if(code==null)
        {
            setDeaultCurrency();
        }
        return code;
    }

    public String getSymbol() {
        if(symbol==null)
        {
            setDeaultCurrency();
        }
        return symbol;
    }





    private void setDeaultCurrency()
    {
        Currency currency = SharedPreferencesUtils.getDefaultCurrency(context);
        this.country = currency.getCountry();
        this.symbol = currency.getSymbol();
        this.code = currency.getCode();
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String toString()
    {
        return "Country: "+getCountry()+"\n"+
                "Code: "+getCode()+"\n"+
                "Symbol: "+getSymbol()+"\n";
    }
}
