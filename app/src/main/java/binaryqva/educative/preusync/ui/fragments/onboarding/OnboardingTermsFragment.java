/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: OnboardingTermsFragment.java
 * Versión: v2.2.0
 * Descripción: Fragmento de términos de uso con optimización de scroll.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.fragments.onboarding;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.ui.viewmodels.OnboardingViewModel;
import binaryqva.educative.preusync.utils.markdown.MarkdownWebViewHelper;
import binaryqva.educative.preusync.utils.theme.ThemeManager;

public class OnboardingTermsFragment extends Fragment {
    
    private OnboardingViewModel viewModel;
    private CheckBox checkBox;
    
    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding_terms, container, false);
        
        viewModel = new ViewModelProvider(requireActivity()).get(OnboardingViewModel.class);
        
        checkBox = view.findViewById(R.id.checkbox1);
        TextView termsText = view.findViewById(R.id.termsTextView);
        
        MarkdownWebViewHelper.replaceWithWebView(requireContext(), termsText, getString(R.string.terms_PreuSync), "terms_cache", R.id.termsProgressBar);
        
        // Optimización de Scroll: Evitar que el ViewPager robe los eventos del WebView
        view.findViewById(R.id.onboardingTermsLayoutContainer).setOnTouchListener((v, event) -> {
            View webView = findWebView(v);
            if (webView != null) webView.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        checkBox.setOnCheckedChangeListener((btn, isChecked) -> viewModel.setTermsAccepted(isChecked));
        
        int colorAccent = ThemeManager.getThemeColor(requireContext(), R.attr.colorAccent);
        int colorNormal = ThemeManager.getThemeColor(requireContext(), R.attr.colorControlNormal);
        checkBox.setButtonTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked}, new int[]{-android.R.attr.state_checked}}, new int[]{colorAccent, colorNormal}));

        return view;
    }

    private View findWebView(View root) {
        if (root instanceof WebView) return root;
        if (root instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) root;
            for (int i = 0; i < g.getChildCount(); i++) {
                View v = findWebView(g.getChildAt(i));
                if (v != null) return v;
            }
        }
        return null;
    }

    @Override
    public void onResume() { 
        super.onResume(); 
        Boolean accepted = viewModel.getTermsAccepted().getValue();
        checkBox.setChecked(accepted != null && accepted); 
    }
}
