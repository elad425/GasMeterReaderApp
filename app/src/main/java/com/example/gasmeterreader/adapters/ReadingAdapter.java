package com.example.gasmeterreader.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gasmeterreader.R;
import com.example.gasmeterreader.entities.Read;
import com.example.gasmeterreader.viewModels.ReadingViewModel;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class ReadingAdapter extends RecyclerView.Adapter<ReadingAdapter.VideoViewHolder> {
    private List<Read> readList;
    private final ReadingViewModel viewModel;

    @SuppressLint("NotifyDataSetChanged")
    public ReadingAdapter(List<Read> readList, Context context, ReadingViewModel viewModel) {
        this.readList = readList;
        this.viewModel = viewModel;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_read, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        Read read = readList.get(position);
        holder.serial.setText(String.format("סיריאלי: %d", read.getMeter_id()));
        holder.apartment.setText(String.format("דירה %d", read.getApartment()));
        holder.last_read.setText(String.format("קודם: %.2f", read.getLast_read()));

        if(read.isRead() && read.getCurrent_read() != 0) {
            holder.card.setCardBackgroundColor(Color.parseColor("#358967"));
            holder.current_read.setText(String.format("נוכחי: %.2f", read.getCurrent_read()));
        }else{
            holder.current_read.setText("");
            holder.card.setCardBackgroundColor(Color.parseColor("#2b2f36"));
        }

        holder.itemView.setOnClickListener(v -> {
            Read clickedReadItem = readList.get(holder.getAdapterPosition());
            viewModel.setSelectedRead(clickedReadItem);
        });
    }

    @Override
    public int getItemCount() {
        return readList != null ? readList.size() : 0;
    }

    public void updateReadings(List<Read> newReads) {
        readList = newReads;
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