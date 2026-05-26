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
    private OnSplitChangedListener listener;
    private boolean equalSplitMode = true;

    public interface OnSplitChangedListener {
        void onSplitChanged();
    }

    public static class SplitItem {
        public EventMemberPublic member;
        public boolean isSelected = true;
        public long amountOwed = 0;
    }

    public MemberSplitAdapter(List<SplitItem> items) {
        this.items = items;
    }

    public void setOnSplitChangedListener(OnSplitChangedListener listener) {
        this.listener = listener;
    }

    public void setEqualSplitMode(boolean equalSplitMode) {
        this.equalSplitMode = equalSplitMode;
        notifyDataSetChanged();
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
        holder.bind(item, equalSplitMode, listener);
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

        void bind(SplitItem item, boolean equalSplitMode, OnSplitChangedListener listener) {
            cbMember.setOnCheckedChangeListener(null);
            if (etAmount.getTag() instanceof TextWatcher) {
                etAmount.removeTextChangedListener((TextWatcher) etAmount.getTag());
            }

            tvMemberName.setText(item.member.getDisplayName());
            tvInitial.setText(item.member.getInitials());
            cbMember.setChecked(item.isSelected);
            etAmount.setText(item.amountOwed > 0 ? String.valueOf(item.amountOwed) : "");
            etAmount.setEnabled(item.isSelected && !equalSplitMode);
            etAmount.setFocusable(item.isSelected && !equalSplitMode);
            etAmount.setFocusableInTouchMode(item.isSelected && !equalSplitMode);

            cbMember.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.isSelected = isChecked;
                etAmount.setEnabled(isChecked && !equalSplitMode);
                etAmount.setFocusable(isChecked && !equalSplitMode);
                etAmount.setFocusableInTouchMode(isChecked && !equalSplitMode);
                if (!isChecked) {
                    item.amountOwed = 0;
                    etAmount.setText("");
                }
                if (listener != null) listener.onSplitChanged();
            });

            TextWatcher watcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    if (equalSplitMode) return;
                    try { item.amountOwed = Long.parseLong(s.toString().replaceAll("[.]", "")); }
                    catch (NumberFormatException e) { item.amountOwed = 0; }
                }
            };
            etAmount.addTextChangedListener(watcher);
            etAmount.setTag(watcher);
        }
    }
}
