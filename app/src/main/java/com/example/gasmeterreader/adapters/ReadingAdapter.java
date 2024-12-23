package com.example.gasmeterreader.adapters;

import android.annotation.SuppressLint;
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
import com.example.gasmeterreader.viewModels.ReadingViewModel;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressLint("NotifyDataSetChanged")
public class ReadingAdapter extends RecyclerView.Adapter<ReadingAdapter.ReadViewHolder> {
    private List<Read> readList;
    private List<Read> filteredList;
    private final ReadingViewModel viewModel;
    private final Context context;
    private final LayoutInflater inflater;

    public ReadingAdapter(List<Read> readList, ReadingViewModel viewModel, Context context) {
        this.viewModel = viewModel;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        updateReadings(readList);
    }

    @NonNull
    @Override
    public ReadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ReadViewHolder(inflater.inflate(R.layout.item_read, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ReadViewHolder holder, int position) {
        Read read = filteredList.get(position);
        Read selectedRead = viewModel.getSelectedRead().getValue();

        bindReadData(holder, read);
        updateCardAppearance(holder, read, selectedRead);
        setupClickListener(holder, position);
    }

    private void bindReadData(ReadViewHolder holder, Read read) {
        holder.serial.setText(String.format(Locale.ENGLISH,"סיריאלי: %d", read.getMeter_id()));
        holder.apartment.setText(String.format(Locale.ENGLISH,"דירה %d", read.getApartment()));
        holder.last_read.setText(String.format(Locale.ENGLISH,"קודם: %.2f", read.getLast_read()));
    }

    private void updateCardAppearance(ReadViewHolder holder, Read read, Read selectedRead) {
        boolean isSelected = selectedRead != null && selectedRead.getMeter_id() == read.getMeter_id();

        if (read.getUser_status() != null) {
            handleStatusRead(holder, read, isSelected);
        } else if (read.isRead() && read.getCurrent_read() != 0) {
            handleCompletedRead(holder, read, isSelected);
        } else {
            handlePendingRead(holder, isSelected);
        }
    }

    private void handleStatusRead(ReadViewHolder holder, Read read, boolean isSelected) {
        holder.card.setCardBackgroundColor(ContextCompat.getColor(context,
                isSelected ? R.color.readNotValidSelected : R.color.readNotValid));
        holder.current_read.setText(isSelected ?
                String.format(Locale.ENGLISH,"נוכחי: %.2f", read.getCurrent_read()) :
                read.getUser_status());
    }

    private void handleCompletedRead(ReadViewHolder holder, Read read, boolean isSelected) {
        holder.card.setCardBackgroundColor(ContextCompat.getColor(context,
                isSelected ? R.color.readDoneSelected : R.color.readDone));
        holder.current_read.setText(String.format(Locale.ENGLISH,"נוכחי: %.2f", read.getCurrent_read()));
    }

    private void handlePendingRead(ReadViewHolder holder, boolean isSelected) {
        holder.card.setCardBackgroundColor(ContextCompat.getColor(context,
                isSelected ? R.color.selectedRead : R.color.readBackground));
        holder.current_read.setText("");
    }

    private void setupClickListener(ReadViewHolder holder, int position) {
        holder.itemView.setOnClickListener(v -> {
            if (position < filteredList.size()) {
                viewModel.setSelectedRead(filteredList.get(position));
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (position < filteredList.size()) {
                Read read = filteredList.get(position);
                viewModel.resetRead(read);
                notifyItemChanged(position);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return filteredList != null ? filteredList.size() : 0;
    }


    public void updateReadings(List<Read> newReads) {
        this.readList = newReads != null ? newReads : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.readList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (readList == null) return;

        filteredList = new ArrayList<>();
        if (query.isEmpty()) {
            filteredList.addAll(readList);
        } else {
            String lowercaseQuery = query.toLowerCase();
            for (Read read : readList) {
                if (matchesFilter(read, lowercaseQuery)) {
                    filteredList.add(read);
                }
            }
        }
        notifyDataSetChanged();
    }

    private boolean matchesFilter(Read read, String query) {
        return String.valueOf(read.getApartment()).contains(query) ||
                String.valueOf(read.getMeter_id()).contains(query);
    }

    public static class ReadViewHolder extends RecyclerView.ViewHolder {
        final TextView serial;
        final TextView apartment;
        final TextView last_read;
        final TextView current_read;
        final MaterialCardView card;

        ReadViewHolder(@NonNull View itemView) {
            super(itemView);
            serial = itemView.findViewById(R.id.serial_number);
            apartment = itemView.findViewById(R.id.apartment_number);
            card = itemView.findViewById(R.id.card);
            last_read = itemView.findViewById(R.id.last_read);
            current_read = itemView.findViewById(R.id.current_read);
        }
    }
}