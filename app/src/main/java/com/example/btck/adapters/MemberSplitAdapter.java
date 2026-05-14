package com.example.btck.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.btck.R;
import com.example.btck.models.EventMemberPublic;
import java.util.List;

public class MemberSplitAdapter extends RecyclerView.Adapter<MemberSplitAdapter.SplitViewHolder> {

    private final List<SplitItem> items;

    public static class SplitItem {
        public EventMemberPublic member;
        public boolean isSelected = true;
        public long amountOwed = 0;
    }

    public MemberSplitAdapter(List<SplitItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public SplitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_split_member, parent, false);
        return new SplitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SplitViewHolder holder, int position) {
        SplitItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class SplitViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbMember;
        TextView tvMemberName, tvInitial;
        EditText etAmount;

        SplitViewHolder(@NonNull View itemView) {
            super(itemView);
            cbMember = itemView.findViewById(R.id.cbMember);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvInitial = itemView.findViewById(R.id.tvInitial);
            etAmount = itemView.findViewById(R.id.etAmount);
        }

        void bind(SplitItem item) {
            tvMemberName.setText(item.member.getDisplayName());
            tvInitial.setText(item.member.getInitials());
            cbMember.setChecked(item.isSelected);
            etAmount.setText(item.amountOwed > 0 ? String.valueOf(item.amountOwed) : "");
            etAmount.setEnabled(item.isSelected);

            cbMember.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.isSelected = isChecked;
                etAmount.setEnabled(isChecked);
                if (!isChecked) { item.amountOwed = 0; etAmount.setText(""); }
            });

            etAmount.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    try { item.amountOwed = Long.parseLong(s.toString()); }
                    catch (NumberFormatException e) { item.amountOwed = 0; }
                }
            });
        }
    }
}
