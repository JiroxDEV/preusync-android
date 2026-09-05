/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: ViewPagerAdapter.java
 * Versión: v1.4.1
 * Descripción: Adaptador para ViewPager2 que gestiona la instanciación de
 *              fragmentos.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

/**
 * Provee un mecanismo robusto para el intercambio de fragmentos en el HomeActivity
 * y EventsFragment. Asegura que los estados de los fragmentos se mantengan.
 */
public class ViewPagerAdapter extends FragmentStateAdapter {
    
    private final List<Fragment> fragments;

    public ViewPagerAdapter(FragmentActivity activity, List<Fragment> fragments) {
        super(activity);
        this.fragments = fragments;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Retorna la instancia pre-construida para mayor agilidad de navegación.
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

    /**
     * Recupera una instancia de fragmento específica.
     */
    public Fragment getFragment(int position) {
        if (position >= 0 && position < fragments.size()) return fragments.get(position);
        return null;
    }

    /**
     * Obtiene la lista completa de fragmentos administrados.
     */
    public List<Fragment> getFragments() {
        return fragments;
    }
}


