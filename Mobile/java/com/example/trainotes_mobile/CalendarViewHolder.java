package com.example.trainotes_mobile;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.ArrayList;

public class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private ArrayList<LocalDate> daysArray;
    public View calendarCell;
    public TextView dayTextView;
    private CalendarAdapter.OnItemListener onItemListener;

    public CalendarViewHolder(View calendarCellView, CalendarAdapter.OnItemListener onItemListener,
                              ArrayList<LocalDate> daysArray) {
        super(calendarCellView);
        calendarCell = calendarCellView.findViewById(R.id.calendarCell);
        dayTextView = calendarCellView.findViewById(R.id.calendarCellText);
        this.onItemListener = onItemListener;
        calendarCellView.setOnClickListener(this);
        this.daysArray = daysArray;
    }

    @Override
    public void onClick(View view)
    {
        onItemListener.onItemClick(getAdapterPosition(), daysArray.get(getAdapterPosition()));
    }
}

