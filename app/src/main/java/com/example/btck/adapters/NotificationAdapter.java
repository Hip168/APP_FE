package com.example.btck.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.btck.R;
import com.example.btck.models.NotificationPublic;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotifViewHolder> {

    private final List<NotificationPublic> items;
    private final OnNotifClickListener listener;

    public interface OnNotifClickListener {
        void onNotifClick(NotificationPublic notif);
    }

    public NotificationAdapter(List<NotificationPublic> items, OnNotifClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotifViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotifViewHolder holder, int position) {
        NotificationPublic notif = items.get(position);
        holder.bind(notif);
        holder.itemView.setOnClickListener(v -> listener.onNotifClick(notif));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class NotifViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvTime, tvTypeIcon;
        View unreadDot;
        CardView card;

        NotifViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTypeIcon = itemView.findViewById(R.id.tvTypeIcon);
            unreadDot = itemView.findViewById(R.id.unreadDot);
            card = itemView.findViewById(R.id.card);
        }

        void bind(NotificationPublic notif) {
            tvTitle.setText(notif.title);
            tvContent.setText(notif.content);
            tvTime.setText(formatTime(notif.createdAt));
            tvTypeIcon.setText(getTypeIcon(notif.type));
            unreadDot.setVisibility(notif.isRead ? View.GONE : View.VISIBLE);
            card.setCardBackgroundColor(notif.isRead
                    ? itemView.getContext().getColor(R.color.background_card)
                    : itemView.getContext().getColor(R.color.mint_light));
        }

        private String getTypeIcon(String type) {
            if (type == null) return "🔔";
            switch (type.toLowerCase()) {
                case "expense_added": return "💸";
                case "settlement": return "✅";
                case "member_joined": return "👥";
                case "event_created": return "🎉";
                default: return "🔔";
            }
        }

        private String formatTime(String createdAt) {
            if (createdAt == null) return "";
            try {
                if (createdAt.length() >= 10) return createdAt.substring(0, 10);
            } catch (Exception ignored) {}
            return createdAt;
        }
    }
}
