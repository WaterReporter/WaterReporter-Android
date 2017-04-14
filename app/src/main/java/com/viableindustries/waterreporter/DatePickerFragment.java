//package com.viableindustries.waterreporter;
//
//import android.app.DatePickerDialog;
//import android.app.Dialog;
//import android.app.DialogFragment;
//import android.os.Bundle;
//import android.widget.DatePicker;
//import android.widget.EditText;
//
//import java.util.Calendar;
//
//import butterknife.Bind;
//import butterknife.ButterKnife;
////import butterknife.InjectView;
//
///**
// * Created by Ryan Hamley on 10/23/14.
// * Simple date picker to use for selecting date on report.
// */
//public class DatePickerFragment extends DialogFragment implements
//        DatePickerDialog.OnDateSetListener {
//    @Bind(R.id.date_input) EditText dateInput;
//
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        //Use the current date as the default date in the picker
//        final Calendar c = Calendar.getInstance();
//        int year = c.get(Calendar.YEAR);
//        int month = c.get(Calendar.MONTH);
//        int day = c.get(Calendar.DAY_OF_MONTH);
//
//        //Create a new instance of DatePickerDialog and return it
//        return new DatePickerDialog(getActivity(), this, year, month, day);
//    }
//
//    @Override
//    public void onDateSet(DatePicker datePicker, int i, int i2, int i3) {
//        ButterKnife.bind(this, getActivity());
//
//        UtilityMethods utility = new UtilityMethods();
//
//        String formattedDate = utility.getDateString(i2, i3, i);
//
//        dateInput.setText(formattedDate);
//    }
//}
