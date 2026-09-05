/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: HomeActivity.java
 * Versión: v15.0.0
 * Descripción: Actividad principal que gestiona la navegación global y el 
 *              enrutamiento de contenidos. Refactorizada para usar el modelo 
 *              unificado Event.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.network.models.ApiResponse;
import binaryqva.educative.preusync.network.models.Ephemeris;
import binaryqva.educative.preusync.network.models.News;
import binaryqva.educative.preusync.network.models.Post;
import binaryqva.educative.preusync.network.models.Profile;
import binaryqva.educative.preusync.network.models.Event;
import binaryqva.educative.preusync.network.requests.VotePostRequest;
import binaryqva.educative.preusync.network.retrofit.RetrofitClient;
import binaryqva.educative.preusync.services.PersistentService;
import binaryqva.educative.preusync.ui.adapters.ViewPagerAdapter;
import binaryqva.educative.preusync.ui.fragments.EventsFragment;
import binaryqva.educative.preusync.ui.fragments.HomeFragment;
import binaryqva.educative.preusync.ui.fragments.NewsFragment;
import binaryqva.educative.preusync.ui.fragments.PostsFragment;
import binaryqva.educative.preusync.ui.fragments.ProfileFragment;
import binaryqva.educative.preusync.ui.helpers.HomeBottomSheetHelper;
import binaryqva.educative.preusync.ui.viewmodels.HomeViewModel;
import binaryqva.educative.preusync.utils.common.AppUtils;
import binaryqva.educative.preusync.utils.common.AvatarHelper;
import binaryqva.educative.preusync.utils.common.DialogHelper;
import binaryqva.educative.preusync.utils.common.PreferenceManager;
import binaryqva.educative.preusync.utils.notification.NotificationHelper;
import binaryqva.educative.preusync.utils.scheduling.BackgroundServiceScheduler;
import binaryqva.educative.preusync.utils.theme.ThemeManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends BaseActivity {

    private static final String TAG = "HomeActivity";

    public static final String ACTION_OPEN_POST = "OPEN_POST";
    public static final String ACTION_OPEN_NEWS = "OPEN_NEWS";
    public static final String ACTION_OPEN_EVENT = "OPEN_EVENT";
    public static final String ACTION_OPEN_EPHEMERIS = "OPEN_EPHEMERIS";
    public static final String ACTION_OPEN_SCHEDULE = "OPEN_SCHEDULE";
    public static final String ACTION_NEW_POST = "NEW_POST";
    public static final String ACTION_NEWS = "NEWS";
    public static final String ACTION_POSTS = "POSTS";
    public static final String ACTION_SCHEDULE = "SCHEDULE";

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigation;
    private ExtendedFloatingActionButton fab;
    private ViewPagerAdapter pagerAdapter;

    private HomeViewModel homeViewModel;
    private HomeBottomSheetHelper bottomSheetHelper;
    private PreferenceManager preferenceManager;

    private final HashMap<String, Profile> authorCache = new HashMap<>();
    private final HashMap<String, Boolean> authorFetching = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        preferenceManager = PreferenceManager.getInstance(this);
        viewPager = findViewById(R.id.viewPager);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        fab = findViewById(R.id.homeFab);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        bottomSheetHelper = new HomeBottomSheetHelper(this, homeViewModel);

        initializeVoteRepository();
        setupViewPager();
        setupBottomNavigation();
        setupFab();

        NotificationHelper.createChannels(this);
        startPersistentServiceIfNeeded();
        BackgroundServiceScheduler.scheduleFirst(this);

        handleIntent(getIntent());
        setupBackPressedHandler();

        if (!preferenceManager.isAuthenticated()) {
            handleInvalidToken();
        }
    }

    private void initializeVoteRepository() {
        String userId = preferenceManager.getSupabaseUserId();
        if (userId != null && !userId.isEmpty()) {
            homeViewModel.initVoteRepository(userId);
        }
    }

    private void setupBackPressedHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (preferenceManager.isConfirmExit()) {
                    DialogHelper.showConfirmDialog(HomeActivity.this, getString(R.string.confirm_exit_title),
                        getString(R.string.confirm_exit_message), getString(R.string.button_exit),
                        getString(R.string.button_cancel), () -> finishAffinity(), null);
                } else {
                    finishAffinity();
                }
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bottomSheetHelper != null) bottomSheetHelper.dismissAll();
    }

    private void setupViewPager() {
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new HomeFragment());
        fragments.add(new NewsFragment());
        fragments.add(new EventsFragment());
        fragments.add(new PostsFragment());
        fragments.add(new ProfileFragment());

        pagerAdapter = new ViewPagerAdapter(this, fragments);
        viewPager.setAdapter(pagerAdapter);
        
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                bottomNavigation.setSelectedItemId(getNavItemId(position));
                updateFabVisibility(position);
                if (position == 3) {
                    fab.setIconResource(R.drawable.ic_plus);
                    fab.setText(getString(R.string.button_new_post));
                    fab.setOnClickListener(v -> showCreatePostBottomSheet());
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state != ViewPager2.SCROLL_STATE_IDLE) {
                    Fragment f = pagerAdapter.getFragment(0);
                    if (f instanceof HomeFragment) ((HomeFragment) f).clearTimers();
                }
            }
        });

        int defaultTab = preferenceManager.getDefaultStartTab();
        if (defaultTab >= 0 && defaultTab < pagerAdapter.getItemCount()) {
            viewPager.setCurrentItem(defaultTab, false);
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navInicio) navigateToPage(0);
            else if (id == R.id.navNoticias) navigateToPage(1);
            else if (id == R.id.navEventos) navigateToPage(2);
            else if (id == R.id.navPosts) navigateToPage(3);
            else if (id == R.id.navPerfil) navigateToPage(4);
            return true;
        });
    }

    private void setupFab() { fab.hide(); }

    private int getNavItemId(int position) {
        switch (position) {
            case 1: return R.id.navNoticias;
            case 2: return R.id.navEventos;
            case 3: return R.id.navPosts;
            case 4: return R.id.navPerfil;
            default: return R.id.navInicio;
        }
    }

    private void updateFabVisibility(int position) {
        if (position == 3) fab.show(); else fab.hide();
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (action == null) action = intent.getStringExtra("action");
        if (intent.hasExtra("subtab")) {
            int subtab = intent.getIntExtra("subtab", -1);
            if (subtab >= 0) { navigateToEventSubTab(subtab); return; }
        }
        if (action == null) return;
        switch (action) {
            case ACTION_NEW_POST: showCreatePostBottomSheet(); return;
            case ACTION_NEWS: navigateToPage(1); return;
            case ACTION_POSTS: navigateToPage(3); return;
            case ACTION_SCHEDULE: navigateToEventSubTab(3); return;
        }
        String id = intent.getStringExtra("id");
        if (id == null || id.isEmpty()) return;
        switch (action) {
            case ACTION_OPEN_POST: fetchAndShowPost(id); break;
            case ACTION_OPEN_NEWS: fetchAndShowNews(id); break;
            case ACTION_OPEN_EPHEMERIS: fetchAndShowEphemeris(id); break;
            case ACTION_OPEN_EVENT: fetchAndShowEvent(id); break;
        }
    }

    public void navigateToPage(int page) {
        if (viewPager != null && page >= 0 && page < pagerAdapter.getItemCount()) viewPager.setCurrentItem(page);
    }

    public void navigateToEventSubTab(int subtab) {
        navigateToPage(2);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Fragment f = pagerAdapter.getFragment(2);
            if (f instanceof EventsFragment) ((EventsFragment) f).selectTab(subtab);
        }, 300);
    }

    public void setFabVisibility(boolean visible) {
        if (fab != null) { if (visible) fab.show(); else fab.hide(); }
    }

    private void fetchAndShowPost(String postId) {
        AlertDialog progress = DialogHelper.showProgressDialog(this, getString(R.string.label_loading_post), false);
        RetrofitClient.getApiService(this).getPostById(postId).enqueue(new Callback<ApiResponse<Post>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Post>> call, @NonNull Response<ApiResponse<Post>> response) {
                if (progress != null) progress.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    showPostDetailsBottomSheet(response.body().getData());
                } else if (response.code() == 401) handleInvalidToken();
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<Post>> call, @NonNull Throwable t) {
                if (progress != null) progress.dismiss();
                AppUtils.showMessage(HomeActivity.this, getString(R.string.error_connection));
            }
        });
    }

    private void fetchAndShowNews(String newsId) {
        AlertDialog progress = DialogHelper.showProgressDialog(this, getString(R.string.label_loading_news), false);
        RetrofitClient.getApiService(this).getNewsById(newsId).enqueue(new Callback<ApiResponse<News>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<News>> call, @NonNull Response<ApiResponse<News>> response) {
                if (progress != null) progress.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    showNewsDetailsBottomSheet(response.body().getData());
                } else if (response.code() == 401) handleInvalidToken();
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<News>> call, @NonNull Throwable t) {
                if (progress != null) progress.dismiss();
                AppUtils.showMessage(HomeActivity.this, getString(R.string.error_connection));
            }
        });
    }

    private void fetchAndShowEphemeris(String id) {
        AlertDialog progress = DialogHelper.showProgressDialog(this, getString(R.string.label_loading_post), false);
        RetrofitClient.getApiService(this).getEphemerisById(id).enqueue(new Callback<ApiResponse<Ephemeris>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Ephemeris>> call, @NonNull Response<ApiResponse<Ephemeris>> response) {
                if (progress != null) progress.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    showEphemerisDetailsBottomSheet(response.body().getData());
                } else if (response.code() == 401) handleInvalidToken();
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<Ephemeris>> call, @NonNull Throwable t) {
                if (progress != null) progress.dismiss();
                AppUtils.showMessage(HomeActivity.this, getString(R.string.error_connection));
            }
        });
    }

    private void fetchAndShowEvent(String id) {
        AlertDialog progress = DialogHelper.showProgressDialog(this, getString(R.string.label_loading_post), false);
        RetrofitClient.getApiService(this).getEventById(id).enqueue(new Callback<ApiResponse<Event>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Event>> call, @NonNull Response<ApiResponse<Event>> response) {
                if (progress != null) progress.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    showEventDetailsBottomSheet(response.body().getData());
                } else if (response.code() == 401) handleInvalidToken();
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<Event>> call, @NonNull Throwable t) {
                if (progress != null) progress.dismiss();
                AppUtils.showMessage(HomeActivity.this, getString(R.string.error_connection));
            }
        });
    }

    public void setVote(String postId, String voteType) {
        if (!preferenceManager.isAuthenticated()) return;
        VotePostRequest body = new VotePostRequest(voteType);
        RetrofitClient.getApiService(this).votePost(postId, body).enqueue(new Callback<ApiResponse<HashMap<String, Object>>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<HashMap<String, Object>>> call, @NonNull Response<ApiResponse<HashMap<String, Object>>> response) {}
            @Override public void onFailure(@NonNull Call<ApiResponse<HashMap<String, Object>>> call, @NonNull Throwable t) {}
        });
    }

    public interface AuthorDataCallback { void onAuthorDataLoaded(Profile authorData); }

    public void fetchAuthorData(String username, AuthorDataCallback callback) {
        if (authorCache.containsKey(username)) { callback.onAuthorDataLoaded(authorCache.get(username)); return; }
        if (authorFetching.containsKey(username) && authorFetching.get(username)) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> fetchAuthorData(username, callback), 500); return;
        }
        authorFetching.put(username, true);
        RetrofitClient.getApiService(this).getUserByUsername(username).enqueue(new Callback<ApiResponse<Profile>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Profile>> call, @NonNull Response<ApiResponse<Profile>> response) {
                authorFetching.remove(username);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Profile profile = response.body().getData();
                    authorCache.put(username, profile);
                    callback.onAuthorDataLoaded(profile);
                } else callback.onAuthorDataLoaded(null);
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<Profile>> call, @NonNull Throwable t) {
                authorFetching.remove(username); callback.onAuthorDataLoaded(null);
            }
        });
    }

    public void updateAuthorAvatar(ImageView imageView, Profile authorData, String username) {
        if (imageView == null) return;
        String photoUrl = (authorData != null) ? authorData.getAvatarUrl() : null;
        
        if (photoUrl != null && !photoUrl.isEmpty()) {
            if (photoUrl.startsWith("{")) {
                try {
                    JSONObject json = new JSONObject(photoUrl);
                    if ("inicial".equals(json.getString("tipo"))) {
                        imageView.setImageBitmap(AvatarHelper.generateInitialAvatar(this, json.getString("inicial"), ThemeManager.getColorAccent(this)));
                    } else if ("rol".equals(json.getString("tipo"))) {
                        imageView.setImageBitmap(AvatarHelper.generateRoleAvatar(this, json.getString("rol"), ThemeManager.getColorAccent(this)));
                    }
                } catch (Exception e) { 
                    String initial = username != null && !username.isEmpty() ? username.substring(0, 1).toUpperCase() : "?";
                    imageView.setImageBitmap(AvatarHelper.generateInitialAvatar(this, initial, ThemeManager.getColorAccent(this)));
                }
            } else if (photoUrl.startsWith("http")) {
                Glide.with(this).load(photoUrl).circleCrop().into(imageView);
            } else {
                String initial = username != null && !username.isEmpty() ? username.substring(0, 1).toUpperCase() : "?";
                imageView.setImageBitmap(AvatarHelper.generateInitialAvatar(this, initial, ThemeManager.getColorAccent(this)));
            }
        } else {
            String initial = username != null && !username.isEmpty() ? username.substring(0, 1).toUpperCase() : "?";
            imageView.setImageBitmap(AvatarHelper.generateInitialAvatar(this, initial, ThemeManager.getColorAccent(this)));
        }
    }

    private void handleInvalidToken() {
        preferenceManager.clearAccount();
        Intent intent = new Intent(this, AuthActivity.class);
        intent.putExtra("session_expired", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void showPostDetailsBottomSheet(Post post) { bottomSheetHelper.showPostDetailsBottomSheet(post); }
    public void showNewsDetailsBottomSheet(News news) { bottomSheetHelper.showNewsDetailsBottomSheet(news); }
    public void showEventDetailsBottomSheet(Event event) { bottomSheetHelper.showEventDetailsBottomSheet(event); }
    public void showEphemerisDetailsBottomSheet(Ephemeris ephemeris) { bottomSheetHelper.showEphemerisDetailsBottomSheet(ephemeris); }
    public void showPostPreviewBottomSheet(String content) { bottomSheetHelper.showPostPreviewBottomSheet(content); }
    public void showCreatePostBottomSheet() { if (bottomSheetHelper != null) bottomSheetHelper.showCreatePostBottomSheet(); }
    public void dismissAllBottomSheets() { if (bottomSheetHelper != null) bottomSheetHelper.dismissAll(); }

    public void onCreatePost(String title, String content, String photoBase64) {
        Fragment f = pagerAdapter.getFragment(3);
        if (f instanceof PostsFragment) ((PostsFragment) f).createPost(title, content, photoBase64);
    }

    public void onVoteAction(String postId, String voteType) { setVote(postId, voteType); }
    public void onDeletePost(String postId) {
        Fragment pf = pagerAdapter.getFragment(3); if (pf instanceof PostsFragment) ((PostsFragment) pf).deletePost(postId);
    }
    public void onReportPost(String postId) {
        Fragment f = pagerAdapter.getFragment(3); if (f instanceof PostsFragment) ((PostsFragment) f).reportPost(postId);
    }

    private void startPersistentServiceIfNeeded() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            Intent i = new Intent(this, PersistentService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(i); else startService(i);
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) return true;
        }
        return false;
    }
}


