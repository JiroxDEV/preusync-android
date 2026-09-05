/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: SelectItem.java
 * Versión: v1.0.0
 * Descripción: Selector de opciones (Radio) para los ajustes.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.settings;

import java.util.List;
import java.util.function.Consumer;

/**
 * Representa una configuración que requiere elegir entre varias alternativas.
 */
public class SelectItem implements SettingsItem {
    public final String title;
    public final List<SelectOption> options;
    public String currentValue;
    public final Consumer<String> onSelected;

    public SelectItem(String title, List<SelectOption> options, String current, Consumer<String> onSelected) {
        this.title = title; this.options = options; this.currentValue = current; this.onSelected = onSelected;
    }

    public void setCurrentValue(String v) { this.currentValue = v; }

    @Override public int getType() { return SettingsItem.TYPE_SELECT; }

    public static class SelectOption {
        public final String label, value;
        public SelectOption(String l, String v) { this.label = l; this.value = v; }
    }
}


