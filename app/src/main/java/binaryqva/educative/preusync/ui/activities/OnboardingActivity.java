/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: OnboardingActivity.java
 * Versión: v2.0.1
 * Descripción: Flujo introductorio modernizado. Centraliza la navegación y 
 *              el control de estados mediante ViewModel.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.ui.viewmodels.OnboardingViewModel;
import binaryqva.educative.preusync.ui.adapters.ViewPagerAdapter;
import binaryqva.educative.preusync.ui.fragments.onboarding.OnboardingWelcomeFragment;
import binaryqva.educative.preusync.ui.fragments.onboarding.OnboardingFeaturesFragment;
import binaryqva.educative.preusync.ui.fragments.onboarding.OnboardingTermsFragment;
import binaryqva.educative.preusync.ui.fragments.onboarding.OnboardingPermissionsFragment;
import binaryqva.educative.preusync.ui.fragments.onboarding.OnboardingFinalFragment;
import binaryqva.educative.preusync.utils.common.PreferenceManager;

public class OnboardingActivity extends BaseActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private MaterialButton buttonBack;
    private ExtendedFloatingActionButton buttonNext;
    
    private OnboardingViewModel viewModel;
    private ViewPagerAdapter pagerAdapter;
    private List<Fragment> fragments;
    private PreferenceManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding);

        viewModel = new ViewModelProvider(this).get(OnboardingViewModel.class);
        prefs = PreferenceManager.getInstance(this);
        
        bindViews();
        setupViewPager();
        setupObservers();
    }

    private void bindViews() {
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        buttonBack = findViewById(R.id.buttonBack);
        buttonNext = findViewById(R.id.buttonNext);

        buttonBack.setOnClickListener(v -> previousPage());
        buttonNext.setOnClickListener(v -> handleNextClick());
    }

    private void setupViewPager() {
        fragments = new ArrayList<>();
        fragments.add(new OnboardingWelcomeFragment());
        fragments.add(new OnboardingFeaturesFragment());
        fragments.add(new OnboardingTermsFragment());
        fragments.add(new OnboardingPermissionsFragment());
        fragments.add(new OnboardingFinalFragment());

        pagerAdapter = new ViewPagerAdapter(this, fragments);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setUserInputEnabled(true);

        // Transformación de página moderna y fluida con duración extendida
        viewPager.setPageTransformer((page, position) -> {
            float absPos = Math.abs(position);
            page.setAlpha(1.0f - absPos);
            
            float scale = 0.85f + (1.0f - absPos) * 0.15f;
            page.setScaleX(scale);
            page.setScaleY(scale);
            
            page.setTranslationX(position * -page.getWidth() * 0.5f);
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {}).attach();

        // Deshabilitar clics en las pestañas para evitar navegación no autorizada
        LinearLayout tabStrip = (LinearLayout) tabLayout.getChildAt(0);
        for (int i = 0; i < tabStrip.getChildCount(); i++) {
            tabStrip.getChildAt(i).setClickable(false);
            tabStrip.getChildAt(i).setOnTouchListener((v, event) -> true);
        }

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateNavigationButtons(position);
            }
        });
    }

    private void setupObservers() {
        viewModel.getTermsAccepted().observe(this, accepted -> updateButtonState());
        viewModel.getPermissionsGranted().observe(this, granted -> updateButtonState());
    }

    private void updateNavigationButtons(int position) {
        buttonBack.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
        
        if (position == fragments.size() - 1) {
            buttonNext.setText(getString(R.string.button_get_started));
            buttonNext.setIconResource(R.drawable.ic_check);
        } else {
            buttonNext.setText(getString(R.string.button_next));
            buttonNext.setIconResource(R.drawable.ic_arrow_forward);
        }
        updateButtonState();
    }

    private void updateButtonState() {
        int current = viewPager.getCurrentItem();
        boolean enabled = true;

        if (current == 2) enabled = Boolean.TRUE.equals(viewModel.getTermsAccepted().getValue());
        if (current == 3) enabled = Boolean.TRUE.equals(viewModel.getPermissionsGranted().getValue());

        buttonNext.setEnabled(enabled);
        buttonNext.setAlpha(enabled ? 1.0f : 0.5f);
    }

    private void handleNextClick() {
        int current = viewPager.getCurrentItem();
        if (current < fragments.size() - 1) {
            viewPager.setCurrentItem(current + 1, true);
        } else {
            finishOnboarding();
        }
    }

    public void nextPage() { handleNextClick(); }
    public void previousPage() { if (viewPager.getCurrentItem() > 0) viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true); }

    private void finishOnboarding() {
        prefs.setOnboardingCompleted(true);
        Fragment lastFragment = pagerAdapter.getFragment(fragments.size() - 1);
        boolean hasAccount = false;
        if (lastFragment instanceof OnboardingFinalFragment) {
            hasAccount = ((OnboardingFinalFragment) lastFragment).isHasAccountSelected();
        }

        Intent intent = new Intent(this, AuthActivity.class);
        intent.putExtra("extra_is_registering", !hasAccount);
        startActivity(intent);
        finish();
    }
}
