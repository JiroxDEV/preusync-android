/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: SettingsActivity.java
 * Versión: v2.0.1
 * Descripción: Actividad principal de ajustes que organiza las diferentes 
 *              categorías de configuración.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;

import java.util.ArrayList;
import java.util.List;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.utils.settings.NavigationItem;
import binaryqva.educative.preusync.utils.settings.SettingsItem;

/**
 * Punto de entrada principal para la configuración de la aplicación.
 * Hereda de BaseSettingsActivity para mantener una estética consistente.
 */
public class SettingsActivity extends BaseSettingsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getTitleResId() {
        return R.string.title_settings;
    }

    /**
     * Construye la lista de categorías principales de la configuración.
     */
    @Override
    protected List<SettingsItem> buildItems() {
        List<SettingsItem> items = new ArrayList<>();

        // Navegación a ajustes generales (Idioma, Red, etc).
        items.add(new NavigationItem(getString(R.string.title_general), R.drawable.ic_nav_home,
                v -> startActivity(new Intent(this, GeneralSettingsActivity.class))));

        // Navegación a ajustes estéticos (Temas, Colores Dinámicos).
        items.add(new NavigationItem(getString(R.string.title_appearance), R.drawable.ic_palette,
                v -> startActivity(new Intent(this, AppearanceSettingsActivity.class))));

        // Navegación a configuración de sincronización y notificaciones.
        items.add(new NavigationItem(getString(R.string.title_services), R.drawable.ic_services,
                v -> startActivity(new Intent(this, ServicesSettingsActivity.class))));

        // Navegación a información legal y de autoría.
        items.add(new NavigationItem(getString(R.string.title_about), R.drawable.ic_info,
                v -> startActivity(new Intent(this, AboutActivity.class))));

        return items;
    }
}


