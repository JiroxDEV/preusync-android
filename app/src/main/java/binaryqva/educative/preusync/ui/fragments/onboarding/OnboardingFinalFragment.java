package binaryqva.educative.preusync.ui.fragments.onboarding;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.utils.common.PreferenceManager;
import binaryqva.educative.preusync.utils.theme.ThemeManager;

public class OnboardingFinalFragment extends Fragment {

    private CheckBox hasAccountCheckBox;
    private PreferenceManager prefs;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding_final, container, false);

        hasAccountCheckBox = view.findViewById(R.id.checkbox1);
        prefs = PreferenceManager.getInstance(requireContext());

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

    public boolean isHasAccountSelected() {
        return hasAccountCheckBox != null && hasAccountCheckBox.isChecked();
    }
}
