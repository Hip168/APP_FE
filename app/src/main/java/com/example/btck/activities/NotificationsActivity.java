package com.example.btck.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.btck.adapters.NotificationAdapter;
import com.example.btck.databinding.ActivityNotificationsBinding;
import com.example.btck.models.NotificationPublic;
import com.example.btck.viewmodel.NotificationViewModel;
import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private ActivityNotificationsBinding binding;
    private NotificationViewModel viewModel;
    private NotificationAdapter adapter;
    private List<NotificationPublic> notificationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thông báo");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);

        setupRecyclerView();
        setupSwipeRefresh();
        setupMarkAllRead();
        observeData();
        loadData();
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(notificationList, notification -> {
            viewModel.markRead(notification.id);
        });
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        binding.rvNotifications.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(com.example.btck.R.color.mint_dark);
        binding.swipeRefresh.setOnRefreshListener(this::loadData);
    }

    private void setupMarkAllRead() {
        binding.btnMarkAllRead.setOnClickListener(v -> {
            viewModel.markAllRead();
        });
    }

    private void observeData() {
        viewModel.notifications.observe(this, result -> {
            binding.swipeRefresh.setRefreshing(false);
            if (result != null && result.data != null) {
                notificationList.clear();
                notificationList.addAll(result.data);
                adapter.notifyDataSetChanged();

                boolean empty = result.data.isEmpty();
                binding.layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                binding.rvNotifications.setVisibility(empty ? View.GONE : View.VISIBLE);
            }
        });

        viewModel.markedRead.observe(this, n -> {
            if (n != null) {
                // Cập nhật item đã đọc trong list
                for (int i = 0; i < notificationList.size(); i++) {
                    if (notificationList.get(i).id != null &&
                            notificationList.get(i).id.equals(n.id)) {
                        notificationList.get(i).isRead = true;
                        adapter.notifyItemChanged(i);
                        break;
                    }
                }
            }
        });

        viewModel.markedAllRead.observe(this, done -> {
            if (done != null && done) {
                for (NotificationPublic n : notificationList) n.isRead = true;
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "Đã đánh dấu tất cả đã đọc", Toast.LENGTH_SHORT).show();
                viewModel.resetMarkedAllRead();
            }
        });

        viewModel.errorMessage.observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void loadData() {
        binding.swipeRefresh.setRefreshing(true);
        viewModel.loadNotifications();
    }
}
