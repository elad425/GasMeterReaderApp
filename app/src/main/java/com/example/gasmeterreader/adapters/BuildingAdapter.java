package com.example.gasmeterreader.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gasmeterreader.R;
import com.example.gasmeterreader.activities.ReadingActivity;
import com.example.gasmeterreader.entities.Building;

import java.util.List;

public class BuildingAdapter extends RecyclerView.Adapter<BuildingAdapter.VideoViewHolder> {
    private List<Building> buildingsList;
    private final Context context;

    @SuppressLint("NotifyDataSetChanged")
    public BuildingAdapter(List<Building> buildingsList, Context context) {
        this.buildingsList = buildingsList;
        this.context = context;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_building, parent, false);
        return new VideoViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        Building building = buildingsList.get(position);
        holder.street.setText(String.format("%s %d", building.getAddress(), building.getBuildingNumber()));
        holder.center.setText(String.format("%d",building.getCenter()));
        holder.leftTodo.setText(String.format("%d",building.getCompleted()));
        holder.total.setText(String.format("%d",building.getReadList().size()));
        holder.city.setText(String.format("%s",building.getCity()));
        holder.isComplete.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));

        if (building.isComplete()){
            holder.isComplete.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
        }

        holder.itemView.setOnClickListener(v -> {
            Building clickedBuildingItem = buildingsList.get(holder.getAdapterPosition());
            Intent i = new Intent(context, ReadingActivity.class);
            i.putExtra("building_center", clickedBuildingItem.getCenter());
            context.startActivity(i);
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateBuildings(List<Building> newBuildings) {
        buildingsList = newBuildings;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (buildingsList != null) {
            return buildingsList.size();
        } else return 0;
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        TextView street;
        TextView center;
        TextView leftTodo;
        TextView city;
        TextView total;
        View isComplete;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            street = itemView.findViewById(R.id.street);
            center = itemView.findViewById(R.id.center);
            leftTodo = itemView.findViewById(R.id.leftTodo);
            city = itemView.findViewById(R.id.city);
            total = itemView.findViewById(R.id.total);
            isComplete = itemView.findViewById(R.id.statusIndicator);
        }
    }
}