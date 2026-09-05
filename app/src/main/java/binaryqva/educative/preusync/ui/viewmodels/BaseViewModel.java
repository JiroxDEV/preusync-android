/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase Abstracta: BaseViewModel.java
 * Versión: v1.0.0
 * Descripción: ViewModel base que estandariza la gestión de estados de UI y 
 *              la comunicación con los repositorios.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import binaryqva.educative.preusync.utils.common.PaginationState;

/**
 * Abstracción genérica para ViewModels que gestionan estados de carga y datos.
 * @param <T> Tipo de dato principal gestionado por el ViewModel.
 */
public abstract class BaseViewModel<T> extends AndroidViewModel {

    protected final MutableLiveData<PaginationState<T>> paginationState = new MutableLiveData<>(PaginationState.loading());
    protected final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    protected final MutableLiveData<String> error = new MutableLiveData<>(null);

    public BaseViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<PaginationState<T>> getPaginationState() {
        return paginationState;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    /**
     * Actualiza el estado de carga global.
     */
    protected void setLoading(boolean loading) {
        isLoading.setValue(loading);
    }

    /**
     * Notifica un error a la UI.
     */
    protected void setError(String message) {
        error.setValue(message);
    }

    /**
     * Resetea el estado para una nueva carga.
     */
    public abstract void refresh();
}


