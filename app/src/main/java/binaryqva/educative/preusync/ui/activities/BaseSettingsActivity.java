/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase Abstracta: BaseSettingsActivity.java
 * Versión: v1.4.9
 * Descripción: Actividad base para las pantallas de ajustes, proporcionando 
 *              una estructura común.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.utils.settings.HeaderItem;
import binaryqva.educative.preusync.utils.settings.SectionGroup;
import binaryqva.educative.preusync.utils.settings.SettingsAdapter;
import binaryqva.educative.preusync.utils.settings.SettingsItem;
import binaryqva.educative.preusync.utils.theme.ThemeManager;

/**
 * Provee un motor de renderizado de ajustes basado en componentes modulares.
 * Se encarga de separar automáticamente los ítems en grupos visuales (Cards)
 * basándose en los HeaderItems definidos por la actividad hija.
 */
public abstract class BaseSettingsActivity extends BaseActivity {

    protected RecyclerView recyclerView;
    protected SettingsAdapter adapter;
    protected ImageButton backButton;
    protected TextView titleText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_template);
        
        backButton = findViewById(R.id.backButton);
        titleText = findViewById(R.id.titleText);
        recyclerView = findViewById(R.id.recyclerView);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        setupToolbar();
        setupRecyclerView();
        applyColors();
    }

    protected void setupToolbar() {
        backButton.setOnClickListener(v -> finish());
        titleText.setText(getTitleResId());
    }

    protected void setupRecyclerView() {
        // Obtención de la lista plana e inicio del proceso de agrupación visual.
        List<SettingsItem> groupedItems = groupItems(buildItems());
        adapter = new SettingsAdapter(groupedItems);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Algoritmo de agrupación: Los ítems se empaquetan en un SectionGroup (Card) 
     * hasta que se encuentra un nuevo HeaderItem, el cual se mantiene fuera.
     */
    private List<SettingsItem> groupItems(List<SettingsItem> flatItems) {
        List<SettingsItem> grouped = new ArrayList<>();
        List<SettingsItem> currentChildren = null;
        String currentHeaderTitle = null;

        for (SettingsItem item : flatItems) {
            if (item.getType() == SettingsItem.TYPE_HEADER) {
                // Si ya había una sección en curso, la guardamos antes de procesar el nuevo Header.
                if (currentChildren != null && !currentChildren.isEmpty()) {
                    grouped.add(new SectionGroup(currentHeaderTitle, currentChildren));
                }
                currentHeaderTitle = ((HeaderItem) item).title;
                currentChildren = new ArrayList<>();
                // El Header se añade fuera de la Card para mayor jerarquía visual.
                grouped.add(item);
            } else {
                if (currentChildren != null) currentChildren.add(item);
                else grouped.add(item); // Fallback para ítems huérfanos de cabecera.
            }
        }
        
        // Cierre del último bloque de ajustes.
        if (currentChildren != null && !currentChildren.isEmpty()) {
            grouped.add(new SectionGroup(currentHeaderTitle, currentChildren));
        }
        return grouped;
    }

    protected void applyColors() {
        int colorBackground = ThemeManager.getThemeColor(this, R.attr.colorBackground);
        getWindow().setNavigationBarColor(colorBackground);
        findViewById(R.id.mainContainer).setBackgroundColor(colorBackground);
    }

    protected abstract int getTitleResId();
    protected abstract List<SettingsItem> buildItems();

    /**
     * Refresca visualmente un ítem específico dentro de la lista de ajustes.
     */
    public void refreshItem(SettingsItem item) {
        int index = adapter.getItems().indexOf(item);
        if (index != -1) adapter.notifyItemChanged(index);
    }
}


