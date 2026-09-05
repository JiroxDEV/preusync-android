/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: ButtonItem.java
 * Versión: v1.0.0
 * Descripción: Botón interactivo dentro de una lista de ajustes.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.settings;

import android.view.View;

/**
 * Permite ejecutar acciones directas (como vaciar caché) desde los menús.
 */
public class ButtonItem implements SettingsItem {
    public final String title;
    public final View.OnClickListener onClick;

    public ButtonItem(String title, View.OnClickListener onClick) { this.title = title; this.onClick = onClick; }

    @Override
    public int getType() { return SettingsItem.TYPE_BUTTON; }
}


