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
import com.example.btck.R;
import com.example.btck.api.RetrofitClient;
import com.example.btck.models.SimplifiedDebt;
import com.example.btck.models.SimplifiedDebtsResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private EventViewModel eventViewModel;
    private AuthViewModel authViewModel;
    private EventAdapter recentGroupAdapter;
    private List<EventPublic> eventList = new ArrayList<>();
    private Map<String, EventAdapter.BalanceHint> balanceHints = new HashMap<>();
    private String currentUserId;

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
        currentUserId = new TokenManager(requireContext()).getUserId();

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
        return "Xin chào";
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
                loadBalanceHints(eventList);
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

    private void loadBalanceHints(List<EventPublic> events) {
        balanceHints.clear();
        recentGroupAdapter.setBalanceHints(balanceHints);
        if (currentUserId == null) return;

        for (EventPublic event : events) {
            if (event.expenseCount <= 0) {
                balanceHints.put(event.id, new EventAdapter.BalanceHint("Chưa có khoản nợ", R.color.text_secondary));
                recentGroupAdapter.setBalanceHints(new HashMap<>(balanceHints));
                continue;
            }

            RetrofitClient.getApiService().getSimplifiedDebts(event.id)
                    .enqueue(new Callback<SimplifiedDebtsResponse>() {
                        @Override
                        public void onResponse(Call<SimplifiedDebtsResponse> call, Response<SimplifiedDebtsResponse> response) {
                            if (!isAdded()) return;
                            if (response.isSuccessful() && response.body() != null && response.body().debts != null) {
                                balanceHints.put(event.id, buildBalanceHint(response.body().debts));
                            } else {
                                balanceHints.put(event.id, new EventAdapter.BalanceHint("Không tải được số dư", R.color.text_secondary));
                            }
                            recentGroupAdapter.setBalanceHints(new HashMap<>(balanceHints));
                        }

                        @Override
                        public void onFailure(Call<SimplifiedDebtsResponse> call, Throwable t) {
                            if (!isAdded()) return;
                            balanceHints.put(event.id, new EventAdapter.BalanceHint("Không tải được số dư", R.color.text_secondary));
                            recentGroupAdapter.setBalanceHints(new HashMap<>(balanceHints));
                        }
                    });
        }
    }

    private EventAdapter.BalanceHint buildBalanceHint(List<SimplifiedDebt> debts) {
        long youOwe = 0;
        long owedToYou = 0;
        List<String> snippets = new ArrayList<>();

        for (SimplifiedDebt debt : debts) {
            if (currentUserId.equals(debt.fromUserId)) {
                youOwe += debt.amount;
                snippets.add("Bạn nợ " + debt.getToDisplayName() + " " + formatMoneyShort(debt.amount));
            } else if (currentUserId.equals(debt.toUserId)) {
                owedToYou += debt.amount;
                snippets.add(debt.getFromDisplayName() + " nợ bạn " + formatMoneyShort(debt.amount));
            }
        }

        if (snippets.isEmpty()) {
            return new EventAdapter.BalanceHint("Đã cân bằng", R.color.color_owed);
        }

        String text = snippets.size() > 1
                ? snippets.get(0) + " +" + (snippets.size() - 1) + " khoản"
                : snippets.get(0);
        int color = youOwe > 0 ? R.color.color_owe : R.color.color_owed;
        return new EventAdapter.BalanceHint(text, color);
    }

    private String formatMoneyShort(long amount) {
        if (amount >= 1_000_000) {
            return String.format(Locale.getDefault(), "%.1f triệuđ", amount / 1_000_000.0);
        }
        return String.format(Locale.getDefault(), "%,dđ", amount);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
