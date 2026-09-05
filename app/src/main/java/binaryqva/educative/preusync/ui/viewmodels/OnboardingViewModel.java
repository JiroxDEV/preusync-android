/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: OnboardingViewModel.java
 * Versión: v1.0.0
 * Descripción: Gestiona el estado y la lógica del flujo de Onboarding. 
 *              Centraliza la aceptación de términos, permisos y el progreso de las páginas.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */
package binaryqva.educative.preusync.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Gestiona el estado y la lógica del flujo de Onboarding.
 * Centraliza la aceptación de términos, permisos y el progreso de las páginas.
 */
public class OnboardingViewModel extends ViewModel {

    private final MutableLiveData<Boolean> termsAccepted = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> permissionsGranted = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> currentAvailablePage = new MutableLiveData<>(0);

    public LiveData<Boolean> getTermsAccepted() {
        return termsAccepted;
    }

    public void setTermsAccepted(boolean accepted) {
        termsAccepted.setValue(accepted);
        updateAvailablePage();
    }

    public LiveData<Boolean> getPermissionsGranted() {
        return permissionsGranted;
    }

    public void setPermissionsGranted(boolean granted) {
        permissionsGranted.setValue(granted);
        updateAvailablePage();
    }

    public LiveData<Integer> getCurrentAvailablePage() {
        return currentAvailablePage;
    }

    /**
     * Calcula dinámicamente hasta qué página puede avanzar el usuario.
     */
    private void updateAvailablePage() {
        int available = 2; // Siempre pueden llegar a términos
        
        Boolean accepted = termsAccepted.getValue();
        if (accepted != null && accepted) {
            available = 3; // Pueden llegar a permisos
            
            Boolean granted = permissionsGranted.getValue();
            if (granted != null && granted) {
                available = 4; // Pueden llegar al final
            }
        }
        
        // Solo actualizamos si es mayor o si retrocede por deselección de términos.
        currentAvailablePage.setValue(available);
    }

    public void notifyWelcomeFinished() {
        if (currentAvailablePage.getValue() < 1) currentAvailablePage.setValue(1);
    }

    public void notifyFeaturesFinished() {
        if (currentAvailablePage.getValue() < 2) currentAvailablePage.setValue(2);
    }
}


