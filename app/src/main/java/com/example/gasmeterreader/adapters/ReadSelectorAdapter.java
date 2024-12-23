package com.example.gasmeterreader.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gasmeterreader.R;
import com.example.gasmeterreader.entities.Read;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReadSelectorAdapter extends RecyclerView.Adapter<ReadSelectorAdapter.ReadViewHolder> {
    private List<Read> reads = new ArrayList<>();
    private List<Read> filteredReads = new ArrayList<>();
    private final OnReadSelectedListener listener;
    private final Context context;

    public interface OnReadSelectedListener {
        void onReadSelected(int position);
    }

    public ReadSelectorAdapter(Context context, OnReadSelectedListener listener) {
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public ReadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_read_selector, parent, false);
        return new ReadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReadViewHolder holder, int position) {
        Read read = filteredReads.get(position);
        holder.apartmentText.setText("דירה " + read.getApartment());
        holder.meterIdText.setText("מונה " + read.getMeter_id());
        holder.lastRead.setText(String.format("קודם: %.2f", read.getLast_read()));

        if (read.isRead() && read.getCurrent_read() != 0) {
            holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.readDone));
        } else if (!Objects.equals(read.getUser_status(), null)) {
            holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.readNotValid));
        } else {
            holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.readBackground));
        }

        holder.itemView.setOnClickListener(v -> {
            int originalPosition = reads.indexOf(filteredReads.get(position));
            listener.onReadSelected(originalPosition);
        });
    }

    @Override
    public int getItemCount() {
        return filteredReads.size();
    }

    public void setReads(List<Read> reads) {
        this.reads = reads;
        this.filteredReads = new ArrayList<>(reads);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredReads.clear();
        if (query.isEmpty()) {
            for (Read read : reads) {
                if (!read.isRead() || read.getCurrent_read() == 0) {
                    filteredReads.add(read);
                }
            }
        } else {
            for (Read read : reads) {
                if (String.valueOf(read.getApartment()).contains(query) ||
                        String.valueOf(read.getMeter_id()).contains(query)) {
                    filteredReads.add(read);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class ReadViewHolder extends RecyclerView.ViewHolder {
        TextView apartmentText;
        TextView meterIdText;
        TextView lastRead;
        MaterialCardView card;

        ReadViewHolder(View itemView) {
            super(itemView);
            apartmentText = itemView.findViewById(R.id.apartmentText);
            meterIdText = itemView.findViewById(R.id.meterIdText);
            lastRead = itemView.findViewById(R.id.lastRead);
            card = itemView.findViewById(R.id.card);
        }
    }
}