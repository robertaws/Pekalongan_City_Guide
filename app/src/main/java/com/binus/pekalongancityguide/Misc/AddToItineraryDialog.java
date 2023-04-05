package com.binus.pekalongancityguide.Misc;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.fragment.app.DialogFragment;

import com.binus.pekalongancityguide.R;

import java.util.Calendar;
import java.util.Locale;

public class AddToItineraryDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private TextView dateText;
    private TextView startTimeText;
    private TextView endTimeText;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_to_itinerary, null);

        builder.setView(view)
                .setTitle("Add to Itinerary")
                .setPositiveButton("Add", (dialog, which) -> {
                    // Add to itinerary logic here
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Cancel logic here
                });

        dateText = view.findViewById(R.id.date_text_view);
        startTimeText = view.findViewById(R.id.start_time_text_view);
        endTimeText = view.findViewById(R.id.end_time_text_view);

        dateText.setOnClickListener(v -> showDatePicker());

        startTimeText.setOnClickListener(v -> showStartTimePicker());

        endTimeText.setOnClickListener(v -> showEndTimePicker());

        return builder.create();
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showStartTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), this,
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE),
                false);
        timePickerDialog.show();
    }

    private void showEndTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), this,
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE),
                false);
        timePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        dateText.setText(String.format(Locale.getDefault(), "%d/%d/%d", month + 1, dayOfMonth, year));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        TextView textView = null;

        switch (view.getId()) {
            case R.id.start_time_text_view:
                textView = startTimeText;
                break;
            case R.id.end_time_text_view:
                textView = endTimeText;
                break;
        }

        if (textView != null) {
            textView.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
        }
    }
}
