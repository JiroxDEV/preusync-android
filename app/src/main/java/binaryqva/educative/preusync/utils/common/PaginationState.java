/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: PaginationState.java
 * Versión: v2.0.0
 * Descripción: Contenedor inmutable y genérico para el estado de carga.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Modela el estado actual de una lista de datos genérica.
 */
public class PaginationState<T> {
    public static final int STATE_LOADING = 0;
    public static final int STATE_CONTENT = 1;
    public static final int STATE_EMPTY = 2;
    public static final int STATE_ERROR = 3;

    public final List<T> items;
    public final int state;
    public final boolean hasMore;
    public final String errorMessage;

    public PaginationState(List<T> items, int state, boolean hasMore, String error) {
        this.items = items != null ? items : new ArrayList<>();
        this.state = state; this.hasMore = hasMore; this.errorMessage = error;
    }

    public static <T> PaginationState<T> loading() {
        return new PaginationState<>(new ArrayList<>(), STATE_LOADING, true, null);
    }

    public static <T> PaginationState<T> content(List<T> i, boolean m) {
        return new PaginationState<>(i, STATE_CONTENT, m, null);
    }

    public static <T> PaginationState<T> empty() {
        return new PaginationState<>(new ArrayList<>(), STATE_EMPTY, false, null);
    }

    public static <T> PaginationState<T> error(String msg) {
        return new PaginationState<>(new ArrayList<>(), STATE_ERROR, false, msg);
    }
}


