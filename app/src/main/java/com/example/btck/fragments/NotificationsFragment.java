package com.example.btck.fragments;

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
import com.example.btck.adapters.NotificationAdapter;
import com.example.btck.api.ApiService;
import com.example.btck.api.RetrofitClient;
import com.example.btck.databinding.FragmentNotificationsBinding;
import com.example.btck.models.NotificationPublic;
import com.example.btck.models.NotificationsPublic;
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
            }
            @Override public void onFailure(Call<NotificationPublic> call, Throwable t) {}
        });
    }

    private void markAllRead() {
        apiService.markAllRead().enqueue(new Callback<com.example.btck.models.MessageResponse>() {
            @Override public void onResponse(Call<com.example.btck.models.MessageResponse> call, Response<com.example.btck.models.MessageResponse> response) {
                if (isAdded()) {
                    for (NotificationPublic n : notifList) n.isRead = true;
                    adapter.notifyDataSetChanged();
                    binding.tvUnreadCount.setText("0 chưa đọc");
                    Toast.makeText(requireContext(), "Đã đọc tất cả", Toast.LENGTH_SHORT).show();
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
