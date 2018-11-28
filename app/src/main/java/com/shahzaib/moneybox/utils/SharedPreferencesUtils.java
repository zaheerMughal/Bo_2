package com.shahzaib.moneybox.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.shahzaib.moneybox.Model.Currency;

public class SharedPreferencesUtils {

    private static final String CURRENCY_SP = "CurrencySP";
    private static final String GOALS_SP = "GoalsSP";
    private static final String SP_CURRENCY_COUNTRY = "CurrencyCountry";
    private static final String SP_CURRENCY_CODE = "CurrencyCode";
    private static final String SP_CURRENCY_SYMBOL = "CurrencySymbol";
    private static final String SP_SHOW_GOALS_TOTAL = "ShowGoalsTotal";

    private static SharedPreferences currencySP;
    private static SharedPreferences goalsTotalSP;


    private static SharedPreferences getCurrencySP(Context context) {
        if (currencySP == null) {
            currencySP = context.getSharedPreferences(CURRENCY_SP, Context.MODE_PRIVATE);
        }
        return currencySP;
    }

    private static SharedPreferences getGoalsTotalSP(Context context) {
        if (goalsTotalSP == null) {
            goalsTotalSP = context.getSharedPreferences(GOALS_SP, Context.MODE_PRIVATE);
        }
        return goalsTotalSP;
    }


    public static void setDefaultCurrency(Context context, Currency currency) {
        getCurrencySP(context).edit().putString(SP_CURRENCY_COUNTRY, currency.getCountry())
                .putString(SP_CURRENCY_CODE, currency.getCode())
                .putString(SP_CURRENCY_SYMBOL, currency.getSymbol()).apply();
    }

    public static Currency getDefaultCurrency(Context context) {
        String currencyCountry = getCurrencySP(context).getString(SP_CURRENCY_COUNTRY, "");
        String currencyCode = getCurrencySP(context).getString(SP_CURRENCY_CODE, "");
        String currencySymbol = getCurrencySP(context).getString(SP_CURRENCY_SYMBOL, "");

        if (currencyCountry.length() > 0) { // means user ny default currency set ki hy, so just return it
            return new Currency(context, currencyCountry, currencyCode, currencySymbol);
        } else { // user ny koi default currency nahi set ki, so jo hum default rakhna chahty hain vohi return kr doo
            return new Currency(context, "NONE", "NONE", "");
        }

    }


    public static boolean getDefault_ShowGoalsTotal(Context context) {
        return getGoalsTotalSP(context).getBoolean(SP_SHOW_GOALS_TOTAL, false);
    }

    public static void setDefault_ShowGoalsTotal(Context context, boolean showGoalsTotal)
    {
        getGoalsTotalSP(context).edit().putBoolean(SP_SHOW_GOALS_TOTAL,showGoalsTotal).apply();
    }
}
