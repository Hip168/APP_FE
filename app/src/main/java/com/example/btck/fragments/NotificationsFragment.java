package com.example.btck.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.btck.R;
import com.example.btck.activities.ExpenseDetailActivity;
import com.example.btck.activities.GroupDetailActivity;
import com.example.btck.activities.SettlementActivity;
import com.example.btck.activities.MainActivity;
import com.example.btck.adapters.NotificationAdapter;
import com.example.btck.api.ApiService;
import com.example.btck.api.RetrofitClient;
import com.example.btck.databinding.FragmentNotificationsBinding;
import com.example.btck.models.NotificationPublic;
import com.example.btck.models.NotificationsPublic;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private NotificationAdapter adapter;
    private List<NotificationPublic> notifList = new ArrayList<>();
    private ApiService apiService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiService = RetrofitClient.getApiService();

        setupRecyclerView();
        loadNotifications();

        binding.swipeRefresh.setColorSchemeColors(requireContext().getColor(R.color.mint_dark));
        binding.swipeRefresh.setOnRefreshListener(this::loadNotifications);

        binding.btnMarkAllRead.setOnClickListener(v -> markAllRead());
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(notifList, notif -> {
            if (!notif.isRead) markAsRead(notif);
            openNotification(notif);
        });
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvNotifications.setAdapter(adapter);
    }

    private void loadNotifications() {
        apiService.getNotifications(0, 50).enqueue(new Callback<NotificationsPublic>() {
            @Override
            public void onResponse(Call<NotificationsPublic> call, Response<NotificationsPublic> response) {
                binding.swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    notifList.clear();
                    notifList.addAll(response.body().data);
                    adapter.notifyDataSetChanged();
                    long unread = notifList.stream().filter(n -> !n.isRead).count();
                    binding.tvUnreadCount.setText(unread + " chưa đọc");
                    boolean empty = notifList.isEmpty();
                    binding.layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                    binding.rvNotifications.setVisibility(empty ? View.GONE : View.VISIBLE);

                    // Cập nhật badge ở MainActivity
                    if (requireActivity() instanceof MainActivity) {
                        ((MainActivity) requireActivity()).updateNotificationBadge();
                    }
                }
            }
            @Override
            public void onFailure(Call<NotificationsPublic> call, Throwable t) {
                binding.swipeRefresh.setRefreshing(false);
                if (isAdded()) Toast.makeText(requireContext(), "Lỗi tải thông báo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markAsRead(NotificationPublic notif) {
        apiService.markNotificationRead(notif.id).enqueue(new Callback<NotificationPublic>() {
            @Override public void onResponse(Call<NotificationPublic> call, Response<NotificationPublic> response) {
                notif.isRead = true;
                adapter.notifyDataSetChanged();
                // Cập nhật badge ở MainActivity
                if (isAdded() && getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).updateNotificationBadge();
                }
            }
            @Override public void onFailure(Call<NotificationPublic> call, Throwable t) {}
        });
    }

    private void openNotification(NotificationPublic notif) {
        String type = notif.type != null ? notif.type.toUpperCase() : "";
        if ("EXPENSE_CREATED".equals(type) && notif.eventId != null && notif.referenceId != null) {
            Intent intent = new Intent(requireContext(), ExpenseDetailActivity.class);
            intent.putExtra("event_id", notif.eventId);
            intent.putExtra("expense_id", notif.referenceId);
            startActivity(intent);
            return;
        }

        if ("SETTLEMENT_RECORDED".equals(type) && notif.eventId != null) {
            Intent intent = new Intent(requireContext(), SettlementActivity.class);
            intent.putExtra("event_id", notif.eventId);
            startActivity(intent);
            return;
        }

        if (notif.eventId != null) {
            Intent intent = new Intent(requireContext(), GroupDetailActivity.class);
            intent.putExtra("event_id", notif.eventId);
            startActivity(intent);
            return;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(notif.title != null ? notif.title : "Thông báo")
                .setMessage((notif.content != null ? notif.content : "")
                        + (notif.createdAt != null ? "\n\n" + notif.createdAt : ""))
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void markAllRead() {
        apiService.markAllRead().enqueue(new Callback<com.example.btck.models.MessageResponse>() {
            @Override public void onResponse(Call<com.example.btck.models.MessageResponse> call, Response<com.example.btck.models.MessageResponse> response) {
                if (isAdded()) {
                    for (NotificationPublic n : notifList) n.isRead = true;
                    adapter.notifyDataSetChanged();
                    binding.tvUnreadCount.setText("0 chưa đọc");
                    Toast.makeText(requireContext(), "Đã đọc tất cả", Toast.LENGTH_SHORT).show();
                    // Cập nhật badge ở MainActivity
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).updateNotificationBadge();
                    }
                }
            }
            @Override public void onFailure(Call<com.example.btck.models.MessageResponse> call, Throwable t) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
