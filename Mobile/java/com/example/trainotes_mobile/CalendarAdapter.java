package com.example.trainotes_mobile;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.ArrayList;

class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder>
{
    private ArrayList<LocalDate> daysArray;
    private OnItemListener onItemListener;

    public CalendarAdapter(ArrayList<LocalDate> daysArray, OnItemListener onItemListener)
    {
        this.daysArray = daysArray;
        this.onItemListener = onItemListener;
    }

    @Override
    public CalendarViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View calendarCellView = inflater.inflate(R.layout.calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = calendarCellView.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * 0.166666666);
        return new CalendarViewHolder(calendarCellView, onItemListener, daysArray);
    }

    @Override
    public void onBindViewHolder(CalendarViewHolder holder, int position)
    {
        LocalDate date = daysArray.get(position);
        holder.dayTextView.setText(String.valueOf(date.getDayOfMonth()));
        if(date.equals(MainActivity.selectedDate))
            holder.calendarCell.setBackgroundResource(R.drawable.button_blue_rounded);
        if(!date.getMonth().equals(MainActivity.currentDate.getMonth()))
            holder.dayTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.light_gray));
    }

    @Override
    public int getItemCount()
    {
        return daysArray.size();
    }

    public interface OnItemListener
    {
        void onItemClick(int position, LocalDate dayText);
    }
}
