/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: ProfileFragment.java
 * Versión: v8.7.0
 * Descripción: Fragmento de perfil que muestra datos personales, estadísticas 
 *              y publicaciones del autor.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */
package binaryqva.educative.preusync.ui.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.HashMap;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.network.models.Profile;
import binaryqva.educative.preusync.ui.activities.AuthActivity;
import binaryqva.educative.preusync.ui.activities.HomeActivity;
import binaryqva.educative.preusync.ui.activities.SettingsActivity;
import binaryqva.educative.preusync.ui.adapters.ProfileAdapter;
import binaryqva.educative.preusync.ui.helpers.ProfileEditorHelper;
import binaryqva.educative.preusync.ui.viewmodels.ProfileViewModel;
import binaryqva.educative.preusync.utils.common.AppUtils;
import binaryqva.educative.preusync.utils.common.CacheManager;
import binaryqva.educative.preusync.utils.common.DialogHelper;
import binaryqva.educative.preusync.utils.common.FilePickerHelper;
import binaryqva.educative.preusync.utils.common.OfflineSnackbarManager;
import binaryqva.educative.preusync.utils.common.PaginationState;
import binaryqva.educative.preusync.utils.common.PreferenceManager;
import binaryqva.educative.preusync.utils.ui.SmartSwipeRefreshLayout;
import binaryqva.educative.preusync.utils.ui.SwipeRefreshHelper;
import binaryqva.educative.preusync.utils.theme.ThemeManager;

public class ProfileFragment extends Fragment {

    private static final String TAG = "PreuSync_ProfileFragment";

    private SmartSwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView profileRecyclerView;
    private LinearLayout profileLoader, profileErrorLayout;
    private TextView profileErrorText;
    private MaterialButton profileRetryButton;

    private ProfileAdapter profileAdapter;
    private SwipeRefreshHelper swipeRefreshHelper;
    private OfflineSnackbarManager offlineSnackbarManager;
    private FilePickerHelper filePickerHelper;
    private PreferenceManager preferencesManager;
    private ProfileEditorHelper editProfileHelper;
    private CacheManager cacheManager;
    private ProfileViewModel profileViewModel;

    private String currentUserId;
    private boolean profileLoaded = false;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        int colorAccent = ThemeManager.getThemeColor(requireContext(), R.attr.colorAccent);

        profileRecyclerView = view.findViewById(R.id.profileRecyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        profileLoader = view.findViewById(R.id.profileLoader);
        profileErrorLayout = view.findViewById(R.id.profileErrorLayout);
        profileErrorText = view.findViewById(R.id.profileErrorTextView);
        profileRetryButton = view.findViewById(R.id.profileRetryButton);

        preferencesManager = PreferenceManager.getInstance(requireContext());
        cacheManager = CacheManager.getInstance();
        currentUserId = preferencesManager.getSupabaseUserId();

        if (swipeRefreshLayout instanceof SmartSwipeRefreshLayout) {
            swipeRefreshLayout.setTargetRecyclerView(profileRecyclerView);
        }

        swipeRefreshHelper = new SwipeRefreshHelper(swipeRefreshLayout, colorAccent);
        swipeRefreshHelper.setOnRefreshListener(this::onRefresh);

        offlineSnackbarManager = new OfflineSnackbarManager(this, view, this::onRefresh);
        filePickerHelper = new FilePickerHelper(this);

        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        editProfileHelper = new ProfileEditorHelper(requireActivity(), profileViewModel, filePickerHelper);

        setupRecyclerView();
        observeViewModel();
        loadUserData();
        checkStoragePermissions();

        return view;
    }

    private void setupRecyclerView() {
        profileRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        profileRecyclerView.setHasFixedSize(true);
        profileRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (lm != null && (lm.getChildCount() + lm.findFirstVisibleItemPosition()) >= lm.getItemCount() - 2) {
                        profileViewModel.loadUserPosts(false);
                    }
                }
            }
        });
        profileAdapter = new ProfileAdapter(requireContext());
        profileAdapter.setOnPostClickListener(item -> {
            if (getActivity() instanceof HomeActivity) ((HomeActivity) getActivity()).showPostDetailsBottomSheet(item);
        });
        profileAdapter.setOnEditProfileListener(this::openEditProfile);
        profileAdapter.setOnSettingsListener(() -> startActivity(new Intent(requireActivity(), SettingsActivity.class)));
        profileAdapter.setOnLogoutListener(() -> logout(true));
        profileRecyclerView.setAdapter(profileAdapter);
    }

    private void openEditProfile() {
        Profile userData = profileViewModel.getUserData().getValue();
        if (userData == null) return;
        
        editProfileHelper.showEditBottomSheet(userData, new ProfileEditorHelper.OnSaveListener() {
            @Override
            public void onSave(HashMap<String, Object> updateData) {
                profileViewModel.updateUser(updateData);
            }
            @Override public void onCancel() {}
        });
    }

    private void observeViewModel() {
        profileViewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (loading) profileLoader.setVisibility(View.VISIBLE);
            else { profileLoader.setVisibility(View.GONE); if (swipeRefreshHelper != null) swipeRefreshHelper.finishRefresh(); }
        });

        profileViewModel.getUserData().observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
                profileAdapter.setProfileData(data);
                profileRecyclerView.setVisibility(View.VISIBLE);
                profileErrorLayout.setVisibility(View.GONE);
                profileLoaded = true;
            }
        });

        profileViewModel.getPostsPaginationState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            profileAdapter.setPostsData(state.items, state.hasMore);
            profileAdapter.setLoadingState(state.state == PaginationState.STATE_LOADING);
        });

        profileViewModel.getIsLoggedOut().observe(getViewLifecycleOwner(), loggedOut -> {
            if (loggedOut) {
                startActivity(new Intent(requireActivity(), AuthActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                requireActivity().finish();
            }
        });
    }

    private void loadUserData() {
        if (currentUserId == null) { profileViewModel.logout(); return; }
        profileViewModel.loadUserData();
    }

    private void onRefresh() {
        profileViewModel.refresh();
    }

    private void logout(boolean confirm) {
        if (confirm) {
            DialogHelper.showConfirmDialog(requireActivity(), getString(R.string.profile_confirm_logout),
                getString(R.string.profile_logout_confirmation), getString(R.string.button_logout), 
                getString(R.string.button_cancel), () -> profileViewModel.logout(), null);
        } else profileViewModel.logout();
    }

    public void onPostDeleted(String postId) {
        if (profileViewModel != null) profileViewModel.removePostById(postId);
    }

    private void checkStoragePermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
        }
    }
}


