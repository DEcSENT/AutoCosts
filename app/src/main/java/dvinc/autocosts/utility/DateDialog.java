package dvinc.autocosts.utility;
/*
 * Created by DV on Space 5 
 * 25.05.2017
 */

import java.util.Calendar;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;


/**
 * TODO: desc
 */
@SuppressLint("ValidFragment")
public class DateDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private EditText mTxtDate;
    public DateDialog(View view){
        mTxtDate = (EditText) view;
    }
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        String date=day+"."+(month+1)+"."+year;
        mTxtDate.setText(date);
    }
}