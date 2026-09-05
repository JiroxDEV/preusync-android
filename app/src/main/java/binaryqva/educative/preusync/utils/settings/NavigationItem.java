/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: NavigationItem.java
 * Versión: v1.0.0
 * Descripción: Elemento de navegación para menús de ajustes.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.settings;

import android.view.View;

/**
 * Abre sub-pantallas de configuración o actividades externas.
 */
public class NavigationItem implements SettingsItem {
    public final String title;
    public final int iconRes;
    public final View.OnClickListener onClick;

    public NavigationItem(String title, int icon, View.OnClickListener onClick) {
        this.title = title; this.iconRes = icon; this.onClick = onClick;
    }

    @Override public int getType() { return SettingsItem.TYPE_NAVIGATION; }
}


