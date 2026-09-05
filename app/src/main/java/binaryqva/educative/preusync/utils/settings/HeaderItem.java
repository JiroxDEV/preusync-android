/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: HeaderItem.java
 * Versión: v1.0.0
 * Descripción: Título de sección para los menús de ajustes.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.settings;

/**
 * Representa un encabezado visual con título y descripción opcional.
 */
public class HeaderItem implements SettingsItem {
    public final String title;
    public final String subtitle;

    public HeaderItem(String title) { this(title, null); }
    public HeaderItem(String title, String subtitle) { this.title = title; this.subtitle = subtitle; }

    @Override
    public int getType() { return SettingsItem.TYPE_HEADER; }
}


