/**
 * ============================================================================
 * Proyecto: PreuSync
 * Interfaz: SettingsItem.java
 * Versión: v1.0.1
 * Descripción: Interfaz base para elementos de la lista de ajustes.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.settings;

import androidx.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Define el contrato para cualquier componente visual integrable en los 
 * menús de configuración.
 */
public interface SettingsItem {
    // Definición de tipos de componentes soportados.
    int TYPE_HEADER = 0;
    int TYPE_SWITCH = 1;
    int TYPE_SELECT = 2;
    int TYPE_BUTTON = 3;
    int TYPE_INFO = 4;
    int TYPE_NAVIGATION = 5;
    int TYPE_SECTION_GROUP = 6;

    @IntDef({TYPE_HEADER, TYPE_SWITCH, TYPE_SELECT, TYPE_BUTTON, TYPE_INFO, TYPE_NAVIGATION, TYPE_SECTION_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    @interface SettingsItemType {}

    /**
     * Retorna el identificador de tipo para el reciclaje de vistas.
     */
    @SettingsItemType int getType();
}


