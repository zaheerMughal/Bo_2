package com.shahzaib.moneybox.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shahzaib.moneybox.Model.Goal;
import com.shahzaib.moneybox.database.DbContract;
import com.shahzaib.moneybox.R;

public class ContributionHistoryAdapter extends RecyclerView.Adapter<ContributionHistoryAdapter.ViewHolder> {

    private Context context;
    private Cursor cursor;


    public ContributionHistoryAdapter(Context context)
    {
        this.context = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contribution_history_single_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(cursor == null) return;

        cursor.moveToPosition(position);
        holder.dateTV.setText(Goal.formatDate(cursor.getLong(cursor.getColumnIndex(DbContract.CONTRIBUTION_HISTORY.COLUMN_DATE))));
        double amount = cursor.getDouble(cursor.getColumnIndex(DbContract.CONTRIBUTION_HISTORY.COLUMN_AMOUNT));
        if(amount>0)
        {
            holder.amountTV.setText(String.valueOf("+"+Goal.separateNumberWithComma(amount)));
            holder.amountTV.setTextColor(context.getResources().getColor(R.color.addMoneyTextColor));
        }
        else
        {
            holder.amountTV.setText(Goal.separateNumberWithComma(amount));
            holder.amountTV.setTextColor(context.getResources().getColor(R.color.withdrawMoneyTextColor));
        }


    }

    @Override
    public int getItemCount() {
        if(cursor== null) return 0;
        else return cursor.getCount();
    }






    public void setCursor(Cursor cursor)
    {
        this.cursor = cursor;
    }



    class ViewHolder extends RecyclerView.ViewHolder
    { TextView dateTV, amountTV;
        ViewHolder(View itemView) {
            super(itemView);
            dateTV = itemView.findViewById(R.id.dateTV);
            amountTV = itemView.findViewById(R.id.amountTV);
        }
    }
}
