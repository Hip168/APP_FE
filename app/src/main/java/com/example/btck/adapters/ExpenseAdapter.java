package com.example.btck.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.btck.R;
import com.example.btck.models.ExpensePublic;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private final List<ExpensePublic> expenses;
    private final OnExpenseClickListener listener;

    public interface OnExpenseClickListener {
        void onExpenseClick(ExpensePublic expense);
    }

    public ExpenseAdapter(List<ExpensePublic> expenses, OnExpenseClickListener listener) {
        this.expenses = expenses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        ExpensePublic expense = expenses.get(position);
        holder.bind(expense);
        holder.itemView.setOnClickListener(v -> listener.onExpenseClick(expense));
    }

    @Override
    public int getItemCount() { return expenses.size(); }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvAmount, tvPayer, tvDate, tvCategory, tvCategoryIcon;

        ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvPayer = itemView.findViewById(R.id.tvPayer);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvCategoryIcon = itemView.findViewById(R.id.tvCategoryIcon);
        }

        void bind(ExpensePublic expense) {
            tvDescription.setText(expense.description);
            tvAmount.setText(expense.getFormattedAmount());
            tvPayer.setText("Trả bởi: " + expense.getPayerDisplayName());
            tvDate.setText(expense.expenseDate != null ? expense.expenseDate : "");

            String cat = expense.category != null ? expense.category : "other";
            tvCategory.setText(getCategoryLabel(cat));
            tvCategoryIcon.setText(getCategoryIcon(cat));
        }

        private String getCategoryLabel(String cat) {
            switch (cat.toLowerCase()) {
                case "food": return "Ăn uống";
                case "transport": return "Di chuyển";
                case "entertainment": return "Giải trí";
                case "shopping": return "Mua sắm";
                case "accommodation": return "Lưu trú";
                default: return "Khác";
            }
        }

        private String getCategoryIcon(String cat) {
            switch (cat.toLowerCase()) {
                case "food": return "🍜";
                case "transport": return "🚗";
                case "entertainment": return "🎉";
                case "shopping": return "🛍️";
                case "accommodation": return "🏨";
                default: return "📋";
            }
        }
    }
}
