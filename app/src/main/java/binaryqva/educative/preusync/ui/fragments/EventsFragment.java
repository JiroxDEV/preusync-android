/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: EventsFragment.java
 * Versión: v2.1.0
 * Descripción: Contenedor de eventos que organiza las sub-secciones (escolares, 
 *              externos, efemérides y horario) mediante pestañas.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.ui.adapters.ViewPagerAdapter;
import binaryqva.educative.preusync.utils.theme.ThemeManager;

/**
 * Fragmento de alto nivel que implementa un TabLayoutMediator para gestionar 
 * la navegación horizontal entre los diferentes módulos de información académica.
 */
public class EventsFragment extends Fragment {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ViewPagerAdapter pagerAdapter;
    private TabLayoutMediator tabLayoutMediator;
    private int pendingTabPosition = -1;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);
        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        
        initializeLogic();
        return view;
    }

    private void initializeLogic() {
        int colorAccent = ThemeManager.getThemeColor(requireContext(), R.attr.colorAccent);
        int colorControlHighlight = ThemeManager.getThemeColor(requireContext(), R.attr.colorControlHighlight);

        // Definición de las cuatro secciones hijas.
        final List<Fragment> fragments = new ArrayList<>();
        fragments.add(new SchoolEventsFragment());
        fragments.add(new ExternalEventsFragment());
        fragments.add(new EphemeridesFragment());
        fragments.add(new ScheduleFragment());

        pagerAdapter = new ViewPagerAdapter(requireActivity(), fragments);
        viewPager.setAdapter(pagerAdapter);

        final String[] tabTitles = new String[]{
            getString(R.string.tab_school_events),
            getString(R.string.tab_external_events),
            getString(R.string.tab_ephemerides),
            getString(R.string.tab_schedule)
        };

        // Vinculación dinámica de títulos a las pestañas.
        tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position]));
        tabLayoutMediator.attach();

        // Estilización de las pestañas según el tema activo.
        tabLayout.setTabTextColors(colorAccent, colorAccent);
        tabLayout.setTabRippleColor(new android.content.res.ColorStateList(
                new int[][]{new int[]{android.R.attr.state_pressed}},
                new int[]{colorControlHighlight}));
        tabLayout.setSelectedTabIndicatorColor(colorAccent);

        // Soporte para navegación directa mediante Deep Links.
        if (pendingTabPosition != -1) {
            viewPager.setCurrentItem(pendingTabPosition, false);
            pendingTabPosition = -1;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tabLayoutMediator != null) tabLayoutMediator.detach();
    }

    /**
     * Cambia programáticamente la pestaña activa.
     */
    public void selectTab(int position) {
        if (viewPager != null) viewPager.setCurrentItem(position);
        else pendingTabPosition = position;
    }

    /**
     * Facilita el acceso a una instancia de fragmento hijo por su posición.
     */
    public Fragment getFragmentAtPosition(int position) {
        return (pagerAdapter != null) ? pagerAdapter.getFragment(position) : null;
    }
}


