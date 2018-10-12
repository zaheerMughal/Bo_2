package com.shahzaib.moneybox.Dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import java.util.Calendar;

public class CalendarDialog extends DialogFragment {

    Calendar calendar;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(calendar == null) calendar = Calendar.getInstance();

        int YEAR, MONTH, DAY_OF_MONTH;
        YEAR = calendar.get(Calendar.YEAR);
        MONTH = calendar.get(Calendar.MONTH);
        DAY_OF_MONTH = calendar.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(),
                (DatePickerDialog.OnDateSetListener) getActivity(),YEAR,MONTH,DAY_OF_MONTH);
    }

    public void setCalendar(Calendar calendar)
    {
        this.calendar = calendar;
    }
}

