package org.dhis2.data.forms.section.viewmodels.date;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import org.dhis2.R;

import java.util.Calendar;
import java.util.Date;

public class DatePickerDialogFragment extends DialogFragment {
    private static final String TAG = DatePickerDialogFragment.class.getSimpleName();
    private static final String ARG_ALLOW_DATES_IN_FUTURE = "arg:allowDatesInFuture";

    @Nullable
    private FormattedOnDateSetListener onDateSetListener;
    private Date openingDate;
    private Date closingDate;

    public static DatePickerDialogFragment create(boolean allowDatesInFuture) {
        Bundle arguments = new Bundle();
        arguments.putBoolean(ARG_ALLOW_DATES_IN_FUTURE, allowDatesInFuture);

        DatePickerDialogFragment fragment = new DatePickerDialogFragment();
        fragment.setArguments(arguments);

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@NonNull Bundle savedInstanceState) {
        return showCustomCalendar();
    }

    private DatePickerDialog showNativeCalendar() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(), (view, year, month, dayOfMonth) -> {
            Calendar chosenDate = Calendar.getInstance();
            chosenDate.set(year, month, dayOfMonth);
            if (onDateSetListener != null) {
                onDateSetListener.onDateSet(chosenDate.getTime());
            }
        }, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        if (openingDate != null)
            datePickerDialog.getDatePicker().setMinDate(openingDate.getTime());

        if (closingDate == null && !isAllowDatesInFuture()) {
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        } else if (closingDate != null && !isAllowDatesInFuture()) {
            if (closingDate.before(new Date(System.currentTimeMillis()))) {
                datePickerDialog.getDatePicker().setMaxDate(closingDate.getTime());
            } else {
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            }
        } else if (closingDate != null && isAllowDatesInFuture()) {
            datePickerDialog.getDatePicker().setMaxDate(closingDate.getTime());
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            datePickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
                    getContext().getResources().getString(R.string.change_calendar), (dialog, which) -> {
                        datePickerDialog.dismiss();
                        showCustomCalendar().show();
                    });
        }

        return datePickerDialog;
    }

    private Dialog showCustomCalendar() {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View datePickerView = layoutInflater.inflate(R.layout.widget_datepicker, null);
        final DatePicker datePicker = datePickerView.findViewById(R.id.widget_datepicker);
        final ImageButton changeCalendarButton = datePickerView.findViewById(R.id.changeCalendarButton);
        final Button clearButton = datePickerView.findViewById(R.id.clearButton);
        final Button acceptButton = datePickerView.findViewById(R.id.acceptButton);

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        datePicker.updateDate(year, month, day);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext(), R.style.DatePickerTheme);

        changeCalendarButton.setOnClickListener(view -> {
            showNativeCalendar().show();
            DatePickerDialogFragment.this.dismiss();
        });
        clearButton.setOnClickListener(view -> {
            if (onDateSetListener != null)
                onDateSetListener.onClearDate();
            DatePickerDialogFragment.this.dismiss();
        });

        acceptButton.setOnClickListener(view -> {
            Calendar chosenDate = Calendar.getInstance();
            chosenDate.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
            if (onDateSetListener != null) {
                onDateSetListener.onDateSet(chosenDate.getTime());
            }
            DatePickerDialogFragment.this.dismiss();
        });

        if (openingDate != null)
            datePicker.setMinDate(openingDate.getTime());

        if (closingDate == null && !isAllowDatesInFuture()) {
            datePicker.setMaxDate(System.currentTimeMillis());
        } else if (closingDate != null && !isAllowDatesInFuture()) {
            if (closingDate.before(new Date(System.currentTimeMillis()))) {
                datePicker.setMaxDate(closingDate.getTime());
            } else {
                datePicker.setMaxDate(System.currentTimeMillis());
            }
        } else if (closingDate != null && isAllowDatesInFuture()) {
            datePicker.setMaxDate(closingDate.getTime());
        }

        alertDialog.setView(datePickerView);
        return alertDialog.create();
    }

    public void show(@NonNull FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }

    public void setFormattedOnDateSetListener(@Nullable FormattedOnDateSetListener listener) {
        this.onDateSetListener = listener;
    }

    private boolean isAllowDatesInFuture() {
        return getArguments().getBoolean(ARG_ALLOW_DATES_IN_FUTURE, false);
    }

    public void setOpeningClosingDates(Date openingDate, Date closingDate) {
        this.openingDate = openingDate;
        this.closingDate = closingDate;
    }

    /**
     * The listener used to indicate the user has finished selecting a date.
     */
    public interface FormattedOnDateSetListener {
        /**
         * @param date the date in the correct simple fate format
         */
        void onDateSet(@NonNull Date date);
        void onClearDate();
    }
}