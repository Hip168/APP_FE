package com.example.btck.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.btck.R;
import com.example.btck.activities.GroupDetailActivity;
import com.example.btck.adapters.EventAdapter;
import com.example.btck.databinding.FragmentGroupsBinding;
import com.example.btck.models.EventPublic;
import com.example.btck.viewmodel.EventViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;

public class GroupsFragment extends Fragment {

    private FragmentGroupsBinding binding;
    private EventViewModel viewModel;
    private EventAdapter adapter;
    private List<EventPublic> eventList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGroupsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        setupRecyclerView();
        observeData();
        setupClickListeners();
        loadData();
    }

    private void setupRecyclerView() {
        adapter = new EventAdapter(eventList, event -> {
            Intent intent = new Intent(requireContext(), GroupDetailActivity.class);
            intent.putExtra("event_id", event.id);
            intent.putExtra("event_name", event.name);
            startActivity(intent);
            requireActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
        binding.rvGroups.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvGroups.setAdapter(adapter);

        binding.swipeRefresh.setColorSchemeColors(requireContext().getColor(R.color.mint_dark));
        binding.swipeRefresh.setOnRefreshListener(() -> {
            loadData();
        });
    }

    private void observeData() {
        viewModel.events.observe(getViewLifecycleOwner(), result -> {
            binding.swipeRefresh.setRefreshing(false);
            if (result != null && result.data != null) {
                eventList.clear();
                eventList.addAll(result.data);
                adapter.notifyDataSetChanged();
                binding.tvGroupCount.setText(result.count + " nhóm");
                boolean empty = result.data.isEmpty();
                binding.layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                binding.rvGroups.setVisibility(empty ? View.GONE : View.VISIBLE);
            }
        });

        viewModel.createdEvent.observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                Toast.makeText(requireContext(), "Tạo nhóm thành công!", Toast.LENGTH_SHORT).show();
                loadData();
                viewModel.createdEvent.setValue(null);
            }
        });

        viewModel.joinResult.observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                Toast.makeText(requireContext(), "Tham gia nhóm thành công!", Toast.LENGTH_SHORT).show();
                loadData();
                viewModel.joinResult.setValue(null);
            }
        });

        viewModel.errorMessage.observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        binding.fabCreateGroup.setOnClickListener(v -> showCreateGroupDialog());
        binding.btnJoinGroup.setOnClickListener(v -> showJoinGroupDialog());
    }

    private void showCreateGroupDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_group, null);
        EditText etName = dialogView.findViewById(R.id.etGroupName);
        EditText etDesc = dialogView.findViewById(R.id.etGroupDesc);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Tạo nhóm mới")
                .setView(dialogView)
                .setPositiveButton("Tạo", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String desc = etDesc.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(requireContext(), "Vui lòng nhập tên nhóm", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    viewModel.createEvent(name, desc.isEmpty() ? null : desc);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showJoinGroupDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_join_group, null);
        EditText etCode = dialogView.findViewById(R.id.etCode);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Tham gia nhóm")
                .setView(dialogView)
                .setPositiveButton("Tham gia", (dialog, which) -> {
                    String code = etCode.getText().toString().trim();
                    if (TextUtils.isEmpty(code)) {
                        Toast.makeText(requireContext(), "Vui lòng nhập mã mời", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    viewModel.joinEvent(code);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void loadData() {
        viewModel.loadEvents();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
