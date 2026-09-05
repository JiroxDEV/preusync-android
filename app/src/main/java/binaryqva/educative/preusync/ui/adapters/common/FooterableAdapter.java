/**
 * ============================================================================
 * Proyecto: PreuSync
 * Interfaz: FooterableAdapter.java
 * Versión: v1.2.1
 * Descripción: Interfaz para adaptadores con soporte de pie de página (footer), 
 *              estandarizando estados de carga y error.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.adapters.common;

/**
 * Define el contrato para adaptadores que implementan scroll infinito.
 * Permite gestionar la visibilidad de indicadores de progreso o botones de 
 * reintento en la base del RecyclerView.
 */
public interface FooterableAdapter {
    
    // Estados posibles del pie de página.
    int FOOTER_NONE = 0;    // Sin footer (fin de datos o lista oculta).
    int FOOTER_LOADING = 1; // Indicador de progreso activo.
    int FOOTER_ERROR = 2;   // Botón de reintento tras fallo de red.

    /**
     * Recupera el estado actual del footer.
     */
    int getFooterState();

    /**
     * Cambia el estado del footer y dispara la notificación de cambio al adaptador.
     */
    void setFooterState(int state);
}


