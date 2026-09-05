/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: OnboardingFeaturesFragment.java
 * Versión: v1.0.1
 * Descripción: Sección informativa que destaca las funciones principales
 *              de la aplicación.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */
package binaryqva.educative.preusync.ui.fragments.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.ui.activities.OnboardingActivity;
import binaryqva.educative.preusync.ui.viewmodels.OnboardingViewModel;

/**
 * Sección informativa sobre las capacidades de la plataforma.
 */
public class OnboardingFeaturesFragment extends Fragment {

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding_features, container, false);
        
        OnboardingViewModel viewModel = new ViewModelProvider(requireActivity()).get(OnboardingViewModel.class);
        viewModel.notifyFeaturesFinished();
        
        view.findViewById(R.id.materialButton3).setOnClickListener(v -> {
            if (getActivity() instanceof OnboardingActivity) ((OnboardingActivity) getActivity()).previousPage();
        });

        view.findViewById(R.id.materialButton4).setOnClickListener(v -> {
            if (getActivity() instanceof OnboardingActivity) ((OnboardingActivity) getActivity()).nextPage();
        });

        return view;
    }
}


