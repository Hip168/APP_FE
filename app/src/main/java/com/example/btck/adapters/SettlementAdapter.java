package com.example.btck.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.btck.R;
import com.example.btck.models.SettlementPublic;
import java.util.List;

public class SettlementAdapter extends RecyclerView.Adapter<SettlementAdapter.SettlementViewHolder> {

    private final List<SettlementPublic> items;

    public SettlementAdapter(List<SettlementPublic> items) { this.items = items; }

    @NonNull
    @Override
    public SettlementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_settlement, parent, false);
        return new SettlementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettlementViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class SettlementViewHolder extends RecyclerView.ViewHolder {
        // Use IDs that actually exist in item_settlement.xml
        TextView tvPayer, tvReceiver, tvAmount, tvDate;

        SettlementViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPayer    = itemView.findViewById(R.id.tvSettlementPayer);
            tvReceiver = itemView.findViewById(R.id.tvSettlementReceiver);
            tvAmount   = itemView.findViewById(R.id.tvSettlementAmount);
            tvDate     = itemView.findViewById(R.id.tvSettlementDate);
        }

        void bind(SettlementPublic s) {
            if (tvPayer != null)    tvPayer.setText(s.getFromDisplayName());
            if (tvReceiver != null) tvReceiver.setText(s.getToDisplayName());
            if (tvAmount != null)   tvAmount.setText(s.getFormattedAmount());
            if (tvDate != null)     tvDate.setText(
                    s.createdAt != null && s.createdAt.length() >= 10 ? s.createdAt.substring(0, 10) : ""
            );
        }
    }
}
