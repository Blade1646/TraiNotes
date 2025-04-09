package com.example.trainotes_mobile;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

class TrainingDayAdapter extends RecyclerView.Adapter<TrainingDayViewHolder>
{
    private ArrayList<Integer> exercisesIDsArray;
    private ArrayList<String> exercisesNamesArray;
    private HashMap<Integer, ArrayList<HashMap<String, String>>> exercisesHashMap;
    private OnItemListener onItemListener;

    private boolean isDefaultUnitSystem;

    public TrainingDayAdapter(boolean isDefaultUnitSystem, ArrayList<Integer> exercisesIDsArray,
                              ArrayList<String> exercisesNamesArray,
                              HashMap<Integer, ArrayList<HashMap<String, String>>> exercisesHashMap,
                              OnItemListener onItemListener) {
        this.isDefaultUnitSystem = isDefaultUnitSystem;
        this.exercisesIDsArray = exercisesIDsArray;
        this.exercisesNamesArray = exercisesNamesArray;
        this.exercisesHashMap = exercisesHashMap;
        this.onItemListener = onItemListener;
    }

    @Override
    public TrainingDayViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View exerciseView = inflater.inflate(R.layout.exercise, parent, false);
        return new TrainingDayViewHolder(exerciseView, onItemListener);
    }

    @Override
    public void onBindViewHolder(TrainingDayViewHolder holder, int position)
    {
        holder.exerciseNameTextView.setText(exercisesNamesArray.get(position));
        int approachesAmount = exercisesHashMap.get(exercisesIDsArray.get(position)).size();
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        if (approachesAmount > 5) {
            holder.repsMoreTextView.setVisibility(View.VISIBLE);
            holder.repsMoreTextView.setText((approachesAmount - 5) +
                    holder.itemView.getContext().getString(R.string.more_append));
            layoutParams.height = 550;
        }
        else {
            holder.repsMoreTextView.setVisibility(View.GONE);
            layoutParams.height = 100 + (75 * approachesAmount);
        }
        holder.itemView.setLayoutParams(layoutParams);

        holder.exerciseListView.setAdapter(new SimpleAdapter(holder.itemView.getContext(),
                exercisesHashMap.get(exercisesIDsArray.get(position)).subList
                        (0, Math.min(5, exercisesHashMap.get(exercisesIDsArray.get(position)).size())),
                R.layout.exercise_approach,
                new String[]{"number", "weight", "reps"},
                new int[]{R.id.exerciseNumberTextView, R.id.exerciseWeightTextView, R.id.exerciseRepsTextView}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                TextView exerciseWeightHintTextView = view.findViewById(R.id.exerciseWeightHintTextView);
                TextView exerciseNumberTextView = view.findViewById(R.id.exerciseNumberTextView);

                exerciseWeightHintTextView.setText(isDefaultUnitSystem ?
                        view.getContext().getString(R.string.lbs_append) : view.getContext().getString(R.string.kgs_append));
                exerciseNumberTextView.setText(Integer.toString(position + 1));
                return view;
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return exercisesIDsArray.size();
    }

    public interface OnItemListener
    {
        void onItemClick(int position);
    }
}
