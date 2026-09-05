/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: OnboardingTermsFragment.java
 * Versión: v1.2.8
 * Descripción: Fragmento para la aceptación de Términos y Condiciones.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */
package binaryqva.educative.preusync.ui.fragments.onboarding;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.ui.activities.OnboardingActivity;
import binaryqva.educative.preusync.ui.viewmodels.OnboardingViewModel;
import binaryqva.educative.preusync.utils.theme.ThemeManager;

/**
 * Presenta el acuerdo legal de uso de la plataforma. 
 * Bloquea el avance del onboarding hasta que el usuario marque la casilla.
 */
public class OnboardingTermsFragment extends Fragment {
    
    private OnboardingViewModel viewModel;
    private MaterialButton nextButton;
    private CheckBox checkBox;
    public static boolean termsAccepted = false;
    
    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding_terms, container, false);
        
        viewModel = new ViewModelProvider(requireActivity()).get(OnboardingViewModel.class);
        
        checkBox = view.findViewById(R.id.checkbox1);
        nextButton = view.findViewById(R.id.materialButton4);
        TextView termsText = view.findViewById(R.id.termsTextView);
        
        termsText.setText(HtmlCompat.fromHtml(getString(R.string.terms_PreuSync), HtmlCompat.FROM_HTML_MODE_LEGACY));
        
        checkBox.setOnCheckedChangeListener((btn, isChecked) -> viewModel.setTermsAccepted(isChecked));
        
        view.findViewById(R.id.materialButton3).setOnClickListener(v -> { 
            if (getActivity() instanceof OnboardingActivity) ((OnboardingActivity) getActivity()).previousPage(); 
        });
        
        nextButton.setOnClickListener(v -> { 
            Boolean accepted = viewModel.getTermsAccepted().getValue();
            if (accepted != null && accepted && getActivity() instanceof OnboardingActivity) {
                ((OnboardingActivity) getActivity()).nextPage();
            }
        });

        setupObservers();
        return view;
    }
    
    private void setupObservers() {
        viewModel.getTermsAccepted().observe(getViewLifecycleOwner(), this::updateUI);
    }
    
    private void updateUI(boolean termsAccepted) {
        OnboardingTermsFragment.termsAccepted = termsAccepted;
        int color = ThemeManager.getThemeColor(requireContext(), termsAccepted ? R.attr.colorAccent : R.attr.colorControlNormal);
        nextButton.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    @Override
    public void onResume() { 
        super.onResume(); 
        Boolean accepted = viewModel.getTermsAccepted().getValue();
        boolean isAccepted = accepted != null && accepted;
        checkBox.setChecked(isAccepted); 
        updateUI(isAccepted); 
    }
}


