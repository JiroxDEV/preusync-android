/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: OnboardingFinalFragment.java
 * Versión: v1.0.3
 * Descripción: Fragmento final del onboarding que prepara la transición a la 
 *              pantalla de autenticación.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.fragments.onboarding;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.ui.activities.AuthActivity;
import binaryqva.educative.preusync.utils.common.PreferenceManager;
import binaryqva.educative.preusync.utils.theme.ThemeManager;

/**
 * Concluye el tutorial inicial. Permite al usuario indicar si ya posee
 * una cuenta para pre-configurar la siguiente pantalla.
 */
public class OnboardingFinalFragment extends Fragment {

    private CheckBox hasAccountCheckBox;
    private PreferenceManager prefs;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding_final, container, false);

        hasAccountCheckBox = view.findViewById(R.id.checkbox1);
        prefs = PreferenceManager.getInstance(requireContext());

        view.findViewById(R.id.materialButton2).setOnClickListener(v -> completeOnboarding());

        setupUI();
        return view;
    }

    private void setupUI() {
        int colorAccent = ThemeManager.getThemeColor(requireContext(), R.attr.colorAccent);
        int colorNormal = ThemeManager.getThemeColor(requireContext(), R.attr.colorControlNormal);

        hasAccountCheckBox.setButtonTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked}, new int[]{-android.R.attr.state_checked}}, new int[]{colorAccent, colorNormal}));
        hasAccountCheckBox.setChecked(prefs.hasAccount());
        hasAccountCheckBox.setOnCheckedChangeListener((btn, isChecked) -> prefs.setHasAccount(isChecked));
    }

    /**
     * Marca el tutorial como completado y lanza la actividad de autenticación.
     */
    private void completeOnboarding() {
        prefs.setOnboardingCompleted(true);
        Intent intent = new Intent(requireContext(), AuthActivity.class);
        intent.putExtra("registrando", String.valueOf(!hasAccountCheckBox.isChecked()));
        startActivity(intent);
        requireActivity().finish();
    }
}


