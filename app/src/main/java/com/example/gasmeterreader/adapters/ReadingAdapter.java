package com.example.gasmeterreader.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gasmeterreader.R;
import com.example.gasmeterreader.entities.Read;
import com.example.gasmeterreader.viewModels.ReadingViewModel;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReadingAdapter extends RecyclerView.Adapter<ReadingAdapter.VideoViewHolder> {
    private List<Read> readList;
    private List<Read> filteredList;
    private final ReadingViewModel viewModel;
    private final Context context;

    @SuppressLint("NotifyDataSetChanged")
    public ReadingAdapter(List<Read> readList, ReadingViewModel viewModel, Context context) {
        this.readList = readList;
        this.viewModel = viewModel;
        this.context = context;
        notifyDataSetChanged();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_read, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        Read read = filteredList.get(position);
        Read selectedRead = viewModel.getSelectedRead().getValue();

        // Set basic read information
        holder.serial.setText(String.format("סיריאלי: %d", read.getMeter_id()));
        holder.apartment.setText(String.format("דירה %d", read.getApartment()));
        holder.last_read.setText(String.format("קודם: %.2f", read.getLast_read()));

        // Determine background color based on read status and selection
        if (selectedRead != null && selectedRead.getMeter_id() == read.getMeter_id()) {
            if(!Objects.equals(read.getUser_status(), null)) {
                holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.readNotValidSelected));
            } else if (read.isRead() && read.getCurrent_read() != 0){
                holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.readDoneSelected));
            } else {
                holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.selectedRead));
            }
            holder.current_read.setText(String.format("נוכחי: %.2f", read.getCurrent_read()));
        } else if(!Objects.equals(read.getUser_status(), null)) {
            holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.readNotValid));
            holder.current_read.setText(read.getUser_status());
        } else if (read.isRead() && read.getCurrent_read() != 0) {
            holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.readDone));
            holder.current_read.setText(String.format("נוכחי: %.2f", read.getCurrent_read()));
        } else {
            holder.current_read.setText("");
            holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.readBackground));
        }

        holder.itemView.setOnClickListener(v -> {
            Read clickedReadItem = filteredList.get(holder.getAdapterPosition());
            viewModel.setSelectedRead(clickedReadItem);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList != null ? filteredList.size() : 0;
    }

    public void updateReadings(List<Read> newReads) {
        readList = newReads;
        filteredList = new ArrayList<>(newReads);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(readList);
        } else {
            for (Read read : readList) {
                if (String.valueOf(read.getApartment()).contains(query) ||
                        String.valueOf(read.getMeter_id()).contains(query)) {
                    filteredList.add(read);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        TextView serial;
        TextView apartment;
        TextView last_read;
        TextView current_read;
        MaterialCardView card;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            serial = itemView.findViewById(R.id.serial_number);
            apartment = itemView.findViewById(R.id.apartment_number);
            card = itemView.findViewById(R.id.card);
            last_read = itemView.findViewById(R.id.last_read);
            current_read = itemView.findViewById(R.id.current_read);
        }
    }
}