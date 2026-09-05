/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase Abstracta: BaseFragment.java
 * Versión: v1.0.1
 * Descripción: Clase base para todos los fragmentos. Proporciona una estructura 
 *              común para la inicialización de vistas y observadores.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */
package binaryqva.educative.preusync.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import binaryqva.educative.preusync.debug.AppLogger;

/**
 * Clase base para todos los fragmentos de la aplicación PreuSync.
 * Proporciona una estructura común para la inicialización de vistas y observadores.
 */
public abstract class BaseFragment extends Fragment {

    protected final String TAG = "PreuSync_" + getClass().getSimpleName();

    /**
     * @return El ID del recurso de layout para inflar.
     */
    @LayoutRes
    protected abstract int getLayoutResId();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        AppLogger.d(TAG, "onCreateView");
        return inflater.inflate(getLayoutResId(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppLogger.d(TAG, "onViewCreated");
        setupViews(view);
        setupObservers();
    }

    /**
     * Inicializa las vistas del fragmento. Se llama en onViewCreated.
     * @param view La vista raíz del fragmento.
     */
    protected abstract void setupViews(View view);

    /**
     * Configura los observadores de LiveData del ViewModel.
     * Se recomienda sobrescribir este método en fragmentos que usen ViewModels.
     */
    protected void setupObservers() {
        // Implementación opcional
    }

    /**
     * Muestra u oculta un estado de carga.
     * Debe ser implementado si el fragmento tiene un indicador de progreso.
     * @param show true para mostrar, false para ocultar.
     */
    protected void showLoading(boolean show) {
        // Implementación opcional
    }

    /**
     * Muestra un mensaje de error al usuario.
     * @param message El mensaje de error.
     */
    protected void showError(String message) {
        // Implementación opcional (ej. Toast, Snackbar)
    }
}


