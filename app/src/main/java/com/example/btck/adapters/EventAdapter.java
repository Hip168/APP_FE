package com.example.btck.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.btck.R;
import com.example.btck.models.EventPublic;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<EventPublic> events;
    private final OnEventClickListener listener;
    private Map<String, BalanceHint> balanceHints = new HashMap<>();

    public interface OnEventClickListener {
        void onEventClick(EventPublic event);
    }

    public EventAdapter(List<EventPublic> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    public static class BalanceHint {
        public final String text;
        public final int colorRes;

        public BalanceHint(String text, int colorRes) {
            this.text = text;
            this.colorRes = colorRes;
        }
    }

    public void setBalanceHints(Map<String, BalanceHint> balanceHints) {
        this.balanceHints = balanceHints != null ? balanceHints : new HashMap<>();
        notifyDataSetChanged();
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
        holder.bind(event, balanceHints.get(event.id));
        holder.itemView.setOnClickListener(v -> listener.onEventClick(event));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvEventDesc, tvMemberCount, tvExpenseCount, tvInitial, tvBalanceHint;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventDesc = itemView.findViewById(R.id.tvEventDesc);
            tvMemberCount = itemView.findViewById(R.id.tvMemberCount);
            tvExpenseCount = itemView.findViewById(R.id.tvExpenseCount);
            tvInitial = itemView.findViewById(R.id.tvInitial);
            tvBalanceHint = itemView.findViewById(R.id.tvBalanceHint);
        }

        void bind(EventPublic event, BalanceHint balanceHint) {
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

            if (balanceHint != null) {
                tvBalanceHint.setVisibility(View.VISIBLE);
                tvBalanceHint.setText(balanceHint.text);
                tvBalanceHint.setTextColor(itemView.getContext().getColor(balanceHint.colorRes));
            } else {
                tvBalanceHint.setVisibility(View.VISIBLE);
                tvBalanceHint.setText(event.expenseCount > 0 ? "Đang tính số dư..." : "Chưa có khoản nợ");
                tvBalanceHint.setTextColor(itemView.getContext().getColor(R.color.text_secondary));
            }
        }
    }
}
