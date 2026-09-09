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

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.ui.viewmodels.OnboardingViewModel;

public class OnboardingWelcomeFragment extends Fragment {

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding_welcome, container, false);
        
        OnboardingViewModel viewModel = new ViewModelProvider(requireActivity()).get(OnboardingViewModel.class);
        viewModel.notifyWelcomeFinished();
        
        TextView title = view.findViewById(R.id.welcomeTitle);
        TextView desc = view.findViewById(R.id.welcomeDescription);

        title.setText(R.string.onboarding_title_1);
        desc.setText(R.string.onboarding_desc_1);
        
        return view;
    }
}
