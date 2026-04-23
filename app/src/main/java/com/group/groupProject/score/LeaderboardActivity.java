package com.group.groupProject.score;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.group.groupProject.R;
import com.group.groupProject.score.model.LeaderboardEntry;
import com.group.groupProject.score.model.LeaderboardResponse;
import com.group.groupProject.score.remote.NetworkModule;
import com.group.groupProject.score.repository.RepositoryCallback;
import com.group.groupProject.score.repository.ScoreboardRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
public class LeaderboardActivity extends AppCompatActivity {

    private static final int ALL_LEVELS_INDEX = 0;
    private static final int FIRST_LEVEL = 1;
    private static final int LAST_LEVEL = 12;

    private final ScoreboardRepository repository = NetworkModule.createRepository();

    private MaterialToolbar toolbar;
    private Spinner levelSpinner;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TextView emptyStateView;
    private LeaderboardAdapter adapter;
    private final List<String> levelOptions = new ArrayList<>();
    private int selectedLevelIndex = ALL_LEVELS_INDEX;
    private boolean spinnerReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_leaderboard);

        bindViews();
        setupToolbar();
        setupRecyclerView();
        setupSpinner();
        setupRefreshLayout();
        loadLeaderboard();
    }

    private void bindViews() {
        toolbar = findViewById(R.id.toolbar);
        levelSpinner = findViewById(R.id.spinner_level);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        recyclerView = findViewById(R.id.recycler_view);
        emptyStateView = findViewById(R.id.tv_empty_state);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.leaderboard_title);
        }
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    private void setupRecyclerView() {
        adapter = new LeaderboardAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupSpinner() {
        levelOptions.clear();
        levelOptions.add(getString(R.string.show_all_levels));
        for (int level = FIRST_LEVEL; level <= LAST_LEVEL; level++) {
            levelOptions.add(getString(R.string.level_template, level));
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                levelOptions
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        levelSpinner.setAdapter(spinnerAdapter);
        levelSpinner.setSelection(ALL_LEVELS_INDEX, false);
        spinnerReady = true;
        levelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!spinnerReady) {
                    return;
                }
                if (selectedLevelIndex != position) {
                    selectedLevelIndex = position;
                    loadLeaderboard();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No-op.
            }
        });
        selectedLevelIndex = ALL_LEVELS_INDEX;
    }

    private void setupRefreshLayout() {
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_light,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light
        );
        swipeRefreshLayout.setOnRefreshListener(this::loadLeaderboard);
    }

    private void loadLeaderboard() {
        showLoading(true);
        hideEmptyState();

        if (selectedLevelIndex == ALL_LEVELS_INDEX) {
            repository.getTopScores(new RepositoryCallback<LeaderboardResponse>() {
                @Override
                public void onSuccess(LeaderboardResponse data) {
                    handleLeaderboardResult(data);
                }

                @Override
                public void onError(String message, Throwable throwable) {
                    handleLeaderboardError(message, throwable);
                }
            });
        } else {
            repository.getTopScoresByLevel(selectedLevelIndex, new RepositoryCallback<LeaderboardResponse>() {
                @Override
                public void onSuccess(LeaderboardResponse data) {
                    handleLeaderboardResult(data);
                }

                @Override
                public void onError(String message, Throwable throwable) {
                    handleLeaderboardError(message, throwable);
                }
            });
        }
    }

    private void handleLeaderboardResult(LeaderboardResponse response) {
        showLoading(false);
        List<LeaderboardEntry> entries = response != null ? response.entries : null;
        if (entries == null || entries.isEmpty()) {
            showEmptyState();
            adapter.submitItems(new ArrayList<>());
            return;
        }
        adapter.submitItems(entries);
        showContent();
    }

    private void handleLeaderboardError(String message, Throwable throwable) {
        showLoading(false);
        adapter.submitItems(new ArrayList<>());
        showEmptyState();
        Snackbar.make(findViewById(android.R.id.content), getString(R.string.network_error), Snackbar.LENGTH_LONG)
                .show();
    }

    private void showLoading(boolean loading) {
        swipeRefreshLayout.setRefreshing(loading);
    }

    private void showContent() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyStateView.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyStateView.setText(R.string.no_result);
        emptyStateView.setVisibility(View.VISIBLE);
    }

    private void hideEmptyState() {
        emptyStateView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}


