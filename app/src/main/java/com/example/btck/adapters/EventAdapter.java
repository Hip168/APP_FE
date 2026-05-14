package com.example.btck.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.btck.R;
import com.example.btck.models.EventPublic;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<EventPublic> events;
    private final OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(EventPublic event);
    }

    public EventAdapter(List<EventPublic> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventPublic event = events.get(position);
        holder.bind(event);
        holder.itemView.setOnClickListener(v -> listener.onEventClick(event));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvEventDesc, tvMemberCount, tvExpenseCount, tvInitial;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventDesc = itemView.findViewById(R.id.tvEventDesc);
            tvMemberCount = itemView.findViewById(R.id.tvMemberCount);
            tvExpenseCount = itemView.findViewById(R.id.tvExpenseCount);
            tvInitial = itemView.findViewById(R.id.tvInitial);
        }

        void bind(EventPublic event) {
            tvEventName.setText(event.name);
            if (event.description != null && !event.description.isEmpty()) {
                tvEventDesc.setVisibility(View.VISIBLE);
                tvEventDesc.setText(event.description);
            } else {
                tvEventDesc.setVisibility(View.GONE);
            }
            tvMemberCount.setText(event.memberCount + " thành viên");
            tvExpenseCount.setText(event.expenseCount + " chi tiêu");

            // Show initial
            if (event.name != null && !event.name.isEmpty()) {
                tvInitial.setText(String.valueOf(event.name.charAt(0)).toUpperCase());
            }
        }
    }
}
