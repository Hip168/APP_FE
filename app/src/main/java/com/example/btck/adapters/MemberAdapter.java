package com.example.btck.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.btck.R;
import com.example.btck.models.EventMemberPublic;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private final List<EventMemberPublic> members;
    private OnMemberClickListener listener;

    public interface OnMemberClickListener {
        void onMemberClick(EventMemberPublic member);
    }

    public MemberAdapter(List<EventMemberPublic> members) {
        this.members = members;
    }

    public MemberAdapter(List<EventMemberPublic> members, OnMemberClickListener listener) {
        this.members = members;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        EventMemberPublic member = members.get(position);
        holder.bind(member);
        if (listener != null) {
            holder.itemView.setOnClickListener(v -> listener.onMemberClick(member));
        }
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitial, tvMemberName, tvMemberEmail, tvRole;

        MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitial     = itemView.findViewById(R.id.tvInitial);
            tvMemberName  = itemView.findViewById(R.id.tvMemberName);
            tvMemberEmail = itemView.findViewById(R.id.tvMemberEmail);
            tvRole        = itemView.findViewById(R.id.tvRole);
        }

        void bind(EventMemberPublic member) {
            String displayName = member.getDisplayName();
            tvMemberName.setText(displayName);

            if (member.userEmail != null && !member.userEmail.isEmpty()) {
                tvMemberEmail.setVisibility(View.VISIBLE);
                tvMemberEmail.setText(member.userEmail);
            } else {
                tvMemberEmail.setVisibility(View.GONE);
            }

            // Avatar initial
            tvInitial.setText(member.getInitials());

            // Role badge
            if (tvRole != null) {
                if ("admin".equalsIgnoreCase(member.role)) {
                    tvRole.setVisibility(View.VISIBLE);
                    tvRole.setText("Admin");
                } else {
                    tvRole.setVisibility(View.GONE);
                }
            }
        }
    }
}
