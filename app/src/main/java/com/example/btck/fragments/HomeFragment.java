package com.example.btck.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.btck.activities.GroupDetailActivity;
import com.example.btck.adapters.EventAdapter;
import com.example.btck.databinding.FragmentHomeBinding;
import com.example.btck.managers.TokenManager;
import com.example.btck.models.EventPublic;
import com.example.btck.viewmodel.AuthViewModel;
import com.example.btck.viewmodel.EventViewModel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private EventViewModel eventViewModel;
    private AuthViewModel authViewModel;
    private EventAdapter recentGroupAdapter;
    private List<EventPublic> eventList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventViewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupUI();
        setupRecyclerView();
        observeData();
        loadData();
    }

    private void setupUI() {
        TokenManager tokenManager = new TokenManager(requireContext());
        String name = tokenManager.getUserName();
        String greeting = getGreeting();
        binding.tvGreeting.setText(greeting + ",");
        binding.tvUserName.setText(name != null ? name.split(" ")[0] : "Bạn");

        String today = new SimpleDateFormat("MMMM yyyy", new Locale("vi", "VN")).format(new Date());
        binding.tvMonthYear.setText(today);
    }

    private String getGreeting() {
        int hour = new java.util.Calendar.Builder().build().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour < 12) return "Chào buổi sáng";
        if (hour < 18) return "Chào buổi chiều";
        return "Chào buổi tối";
    }

    private void setupRecyclerView() {
        recentGroupAdapter = new EventAdapter(eventList, event -> {
            Intent intent = new Intent(requireContext(), GroupDetailActivity.class);
            intent.putExtra("event_id", event.id);
            intent.putExtra("event_name", event.name);
            startActivity(intent);
        });
        binding.rvRecentGroups.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        binding.rvRecentGroups.setNestedScrollingEnabled(false);
        binding.rvRecentGroups.setAdapter(recentGroupAdapter);
    }

    private void observeData() {
        eventViewModel.events.observe(getViewLifecycleOwner(), result -> {
            if (result != null && result.data != null) {
                eventList.clear();
                int limit = Math.min(result.data.size(), 3);
                eventList.addAll(result.data.subList(0, limit));
                recentGroupAdapter.notifyDataSetChanged();
                binding.tvGroupCount.setText(result.count + " nhóm");
                if (result.data.isEmpty()) {
                    binding.layoutEmptyGroups.setVisibility(View.VISIBLE);
                    binding.rvRecentGroups.setVisibility(View.GONE);
                } else {
                    binding.layoutEmptyGroups.setVisibility(View.GONE);
                    binding.rvRecentGroups.setVisibility(View.VISIBLE);
                }
            }
            binding.shimmerLayout.stopShimmer();
            binding.shimmerLayout.setVisibility(View.GONE);
            binding.contentLayout.setVisibility(View.VISIBLE);
        });

        eventViewModel.myBalance.observe(getViewLifecycleOwner(), balance -> {
            if (balance != null && balance.summary != null) {
                long owe = balance.summary.totalYouOwe;
                long owed = balance.summary.totalOwedToYou;
                long net = balance.summary.netBalance;
                binding.tvTotalOwe.setText("-" + formatMoney(owe));
                binding.tvTotalOwed.setText("+" + formatMoney(owed));
                if (net >= 0) {
                    binding.tvNetBalance.setText("+" + formatMoney(net));
                    binding.tvNetBalance.setTextColor(requireContext().getColor(com.example.btck.R.color.color_owed));
                } else {
                    binding.tvNetBalance.setText("-" + formatMoney(Math.abs(net)));
                    binding.tvNetBalance.setTextColor(requireContext().getColor(com.example.btck.R.color.color_owe));
                }
            }
        });
    }

    private String formatMoney(long amount) {
        if (amount >= 1_000_000_000) {
            return String.format(Locale.getDefault(), "%.1f tỷđ", amount / 1_000_000_000.0);
        }
        if (amount >= 1_000_000) {
            return String.format(Locale.getDefault(), "%.1f triệuđ", amount / 1_000_000.0);
        }
        return String.format(Locale.getDefault(), "%,dđ", amount);
    }

    private void loadData() {
        binding.shimmerLayout.startShimmer();
        binding.shimmerLayout.setVisibility(View.VISIBLE);
        binding.contentLayout.setVisibility(View.GONE);
        eventViewModel.loadEvents();
        eventViewModel.loadMyBalance();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
