package com.example.trainotes_mobile;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class TrainingDayViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private TrainingDayAdapter.OnItemListener onItemListener;
    public TextView exerciseNameTextView;
    public TextView repsMoreTextView;
    public ListView exerciseListView;

    public TrainingDayViewHolder(View exerciseView, TrainingDayAdapter.OnItemListener onItemListener) {
        super(exerciseView);
        this.onItemListener = onItemListener;
        exerciseView.setOnClickListener(this);
        exerciseNameTextView = exerciseView.findViewById(R.id.exerciseNameTextView);
        repsMoreTextView = exerciseView.findViewById(R.id.repsMoreTextView);
        exerciseListView = exerciseView.findViewById(R.id.exerciseListView);
    }

    @Override
    public void onClick(View view)
    {
        onItemListener.onItemClick(getAdapterPosition());
    }
}