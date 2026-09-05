/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: OnboardingActivity.java
 * Versión: v1.2.1
 * Descripción: Flujo introductorio guiado con ViewPager: muestra características, términos y gestión de permisos iniciales.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import binaryqva.educative.preusync.R;
import androidx.lifecycle.ViewModelProvider;

import binaryqva.educative.preusync.ui.viewmodels.OnboardingViewModel;
import binaryqva.educative.preusync.ui.adapters.ViewPagerAdapter;
import binaryqva.educative.preusync.ui.fragments.onboarding.OnboardingWelcomeFragment;
import binaryqva.educative.preusync.ui.fragments.onboarding.OnboardingFeaturesFragment;
import binaryqva.educative.preusync.ui.fragments.onboarding.OnboardingTermsFragment;
import binaryqva.educative.preusync.ui.fragments.onboarding.OnboardingPermissionsFragment;
import binaryqva.educative.preusync.ui.fragments.onboarding.OnboardingFinalFragment;
import binaryqva.educative.preusync.utils.theme.ThemeManager;

/**
 * Gestor del flujo inicial de usuario. Utiliza un ViewPager2 para navegar
 * entre las diferentes secciones informativas y de configuración.
 */
public class OnboardingActivity extends BaseActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private List<Fragment> fragments;
    public int availablePage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        initializeLogic();
    }

    private void initializeLogic() {
        int colorAccent = ThemeManager.getThemeColor(this, R.attr.colorAccent);
        int colorHighlight = ThemeManager.getThemeColor(this, R.attr.colorControlHighlight);

        getWindow().setNavigationBarColor(ThemeManager.getThemeColor(this, R.attr.colorBackground));
        
        fragments = new ArrayList<>();
        fragments.add(new OnboardingWelcomeFragment());
        fragments.add(new OnboardingFeaturesFragment());
        fragments.add(new OnboardingTermsFragment());
        fragments.add(new OnboardingPermissionsFragment());
        fragments.add(new OnboardingFinalFragment());

        viewPager.setAdapter(new ViewPagerAdapter(this, fragments));

        // Vinculación de indicadores visuales (dots) con las páginas.
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(getString(R.string.text_bullet))).attach();

        tabLayout.setTabTextColors(colorAccent, colorAccent);
        tabLayout.setSelectedTabIndicatorColor(colorAccent);
        tabLayout.setTabRippleColor(new android.content.res.ColorStateList(new int[][]{new int[]{android.R.attr.state_pressed}}, new int[]{colorHighlight}));

        // Sistema de bloqueo de páginas: El usuario no puede avanzar si no acepta términos o concede permisos.
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageScrolled(int p, float po, int pop) { checkPageAccess(); }
            @Override public void onPageSelected(int p) { checkPageAccess(); }
        });
    }

    public void nextPage() {
        if (viewPager.getCurrentItem() < fragments.size() - 1) viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
    }

    public void previousPage() {
        if (viewPager.getCurrentItem() > 0) viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
    }

    /**
     * Valida que el usuario tenga permitido el acceso a la página solicitada.
     */
    public void checkPageAccess() {
        int current = viewPager.getCurrentItem();
        if (current == 2) availablePage = OnboardingTermsFragment.termsAccepted ? 3 : 2;
        if (current > availablePage) viewPager.setCurrentItem(availablePage, true);
    }
}


