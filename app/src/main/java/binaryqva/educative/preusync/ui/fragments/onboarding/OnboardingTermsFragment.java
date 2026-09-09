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

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.ui.viewmodels.OnboardingViewModel;
import binaryqva.educative.preusync.utils.theme.ThemeManager;

public class OnboardingTermsFragment extends Fragment {
    
    private OnboardingViewModel viewModel;
    private CheckBox checkBox;
    
    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding_terms, container, false);
        
        viewModel = new ViewModelProvider(requireActivity()).get(OnboardingViewModel.class);
        
        checkBox = view.findViewById(R.id.checkbox1);
        TextView termsText = view.findViewById(R.id.termsTextView);
        
        termsText.setText(HtmlCompat.fromHtml(getString(R.string.terms_PreuSync), HtmlCompat.FROM_HTML_MODE_LEGACY));
        
        checkBox.setOnCheckedChangeListener((btn, isChecked) -> viewModel.setTermsAccepted(isChecked));
        
        int colorAccent = ThemeManager.getThemeColor(requireContext(), R.attr.colorAccent);
        int colorNormal = ThemeManager.getThemeColor(requireContext(), R.attr.colorControlNormal);
        checkBox.setButtonTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked}, new int[]{-android.R.attr.state_checked}}, new int[]{colorAccent, colorNormal}));

        return view;
    }

    @Override
    public void onResume() { 
        super.onResume(); 
        Boolean accepted = viewModel.getTermsAccepted().getValue();
        checkBox.setChecked(accepted != null && accepted); 
    }
}
