package com.example.heartwise;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {

    private List<ActivityEntity> activities;

    public void setActivities(List<ActivityEntity> activities) {
        this.activities = activities;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityEntity activity = activities.get(position);
        holder.tvDate.setText(activity.date);
        holder.tvActivity.setText(activity.activityDescription);
        holder.tvHeartRate.setText("Heart Rate: " + activity.heartRate + " bpm");
    }

    @Override
    public int getItemCount() {
        return (activities != null) ? activities.size() : 0;
    }

    public static class ActivityViewHolder extends RecyclerView.ViewHolder {

        TextView tvDate, tvActivity, tvHeartRate;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvActivity = itemView.findViewById(R.id.tvActivity);
            tvHeartRate = itemView.findViewById(R.id.tvHeartRate);
        }
    }
}