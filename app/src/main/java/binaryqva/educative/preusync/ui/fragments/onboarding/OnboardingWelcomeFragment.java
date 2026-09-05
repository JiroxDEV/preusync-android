/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: OnboardingWelcomeFragment.java
 * Versión: v1.0.1
 * Descripción: Primera pantalla del onboarding que presenta la marca PreuSync.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */
package binaryqva.educative.preusync.ui.fragments.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.ui.activities.OnboardingActivity;
import binaryqva.educative.preusync.ui.viewmodels.OnboardingViewModel;

/**
 * Primera pantalla que ve un usuario nuevo. Presenta la marca PreuSync.
 */
public class OnboardingWelcomeFragment extends Fragment {

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding_welcome, container, false);
        
        OnboardingViewModel viewModel = new ViewModelProvider(requireActivity()).get(OnboardingViewModel.class);
        viewModel.notifyWelcomeFinished();
        
        MaterialButton btn = view.findViewById(R.id.materialButton2);
        TextView title = view.findViewById(R.id.welcomeTitle);
        TextView desc = view.findViewById(R.id.welcomeDescription);

        title.setText(R.string.onboarding_title_1);
        desc.setText(R.string.onboarding_desc_1);
        btn.setText(R.string.button_get_started);
        
        btn.setOnClickListener(v -> {
            if (getActivity() instanceof OnboardingActivity) ((OnboardingActivity) getActivity()).nextPage();
        });

        return view;
    }
}


