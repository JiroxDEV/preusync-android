/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: InfoItem.java
 * Versión: v1.0.0
 * Descripción: Elemento de solo lectura para mostrar información en ajustes.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.settings;

/**
 * Útil para avisos legales, versiones o descripciones estáticas.
 */
public class InfoItem implements SettingsItem {
    public final String text;

    public InfoItem(String text) { this.text = text; }

    @Override
    public int getType() { return SettingsItem.TYPE_INFO; }
}


