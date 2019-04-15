package com.shahzaib.moneybox.Model;

public class GoalCurrency {

    private  String country ="";
    private  String code = "";
    private  String symbol ="";

    public GoalCurrency() {
    }
    public GoalCurrency(String country, String code, String symbol) {
        this.country = country;
        this.code = code;
        this.symbol = symbol;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
