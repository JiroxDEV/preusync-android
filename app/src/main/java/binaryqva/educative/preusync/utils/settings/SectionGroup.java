/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: SectionGroup.java
 * Versión: v1.0.1
 * Descripción: Contenedor para agrupar múltiples elementos de ajuste.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.settings;

import android.content.Context;
import java.util.List;

/**
 * Implementa una sección visual que agrupa controles bajo un mismo contexto 
 * o temática, permitiendo una jerarquía de menús organizada.
 */
public class SectionGroup implements SettingsItem {
    public final String title;
    public final SettingsAdapter innerAdapter;

    public SectionGroup(String title, List<SettingsItem> children) {
        this.title = title;
        this.innerAdapter = new SettingsAdapter(children);
    }

    @Override
    public int getType() { return SettingsItem.TYPE_SECTION_GROUP; }
}


