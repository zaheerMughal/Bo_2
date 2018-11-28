package com.shahzaib.moneybox.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shahzaib.moneybox.Model.Currency;
import com.shahzaib.moneybox.R;

import java.util.ArrayList;

public class CurrenciesAdapter extends RecyclerView.Adapter<CurrenciesAdapter.ViewHolder> {

    private ArrayList<Currency> currenciesList;
    private int row_index = -1;
    private OnCurrencyClickListener currencyClickListener;
    private Context context;




    public CurrenciesAdapter(Context context,OnCurrencyClickListener currencyClickListener)
    {
        this.context = context;
        this.currencyClickListener  = currencyClickListener;
    }






    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.currencies_list_single_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Currency currentCurrency = currenciesList.get(position);
        if(currentCurrency.getSymbol().length()!=0)
        {
            holder.countryNameAndCode.setText(String.valueOf(currentCurrency.getCountry()+" ("+currentCurrency.getCode()+")"));
        }else
        {
            holder.countryNameAndCode.setText(String.valueOf("NONE"));
        }

        holder.symbol.setText(currentCurrency.getSymbol());




        // *** on item pressed, change background color
        resetPressedItemIndex();
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //*** send clicked item data
                currencyClickListener.clickedItemData(new Currency(context,currentCurrency.getCountry(),currentCurrency.getCode(),currentCurrency.getSymbol()));

                row_index=position;
                notifyDataSetChanged();

            }
        });
        holder.itemView.setPressed(row_index==position);



    }

    @Override
    public int getItemCount() {
        return currenciesList.size();
    }







    /* Helper Functions********/
    private void resetPressedItemIndex()
    {
        row_index = -1;
    }
    public void setData(ArrayList<Currency> currenciesList) {
        this.currenciesList = currenciesList;
    }
    public void setFilteredList(ArrayList<Currency> filteredList) {
        this.currenciesList = filteredList;
        notifyDataSetChanged();
    }















    class ViewHolder extends RecyclerView.ViewHolder{

        TextView countryNameAndCode, symbol;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            countryNameAndCode = itemView.findViewById(R.id.country_name_and_code);
            symbol = itemView.findViewById(R.id.symbol);
        }
    }



    public interface OnCurrencyClickListener{
        void clickedItemData(Currency currencyData);
    }
}
