/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: AuthActivity.java
 * Versión: v14.0.0
 * Descripción: Actividad de Autenticación. Implementa registro nacional
 *              jerárquico (Provincia-Municipio-Escuela) con flujo MVVM.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.debug.AppLogger;
import binaryqva.educative.preusync.network.models.AuthResponse;
import binaryqva.educative.preusync.network.models.Municipality;
import binaryqva.educative.preusync.network.models.Province;
import binaryqva.educative.preusync.network.models.School;
import binaryqva.educative.preusync.ui.viewmodels.AuthViewModel;
import binaryqva.educative.preusync.utils.common.AppUtils;
import binaryqva.educative.preusync.utils.common.AvatarHelper;
import binaryqva.educative.preusync.utils.common.DialogHelper;
import binaryqva.educative.preusync.utils.common.FilePickerHelper;
import binaryqva.educative.preusync.utils.common.FileUtils;
import binaryqva.educative.preusync.utils.common.PreferenceManager;
import binaryqva.educative.preusync.utils.common.RoleHelper;
import binaryqva.educative.preusync.utils.common.Validator;
import binaryqva.educative.preusync.utils.theme.ThemeManager;

public class AuthActivity extends BaseActivity {
    
    private static final String TAG = "AuthActivity";
    
    private ViewGroup loginLayout, registerLayout;
    private MaterialButton noAccountText, existingAccountText;
    private MaterialCardView avatarCardView;
    private ImageButton settingsButton;
    private ImageView avatarImageView;
    private EditText usernameEditText, passwordEditText, registerUsernameEditText,
    registerPasswordEditText, confirmPasswordEditText, firstNameEditText,
    lastNameEditText, idCardEditText, groupEditText, tuteeEditText, responsibilitiesEditText;
    private TextInputLayout usernameTextInputLayout, passwordTextInputLayout,
    registerUsernameTextInputLayout, registerPasswordTextInputLayout,
    confirmPasswordTextInputLayout, firstNameTextInputLayout,
    lastNameTextInputLayout, idCardTextInputLayout, roleTextInputLayout, 
    provinceTextInputLayout, municipalityTextInputLayout, schoolTextInputLayout,
    groupTextInputLayout, tuteeTextInputLayout, responsibilitiesTextInputLayout;
    
    private AutoCompleteTextView roleAutoComplete, provinceAutoComplete, municipalityAutoComplete, schoolAutoComplete;
    private LinearLayout dynamicFieldsLayout, studentLayout, tutorLayout, teacherLayout;
    private ExtendedFloatingActionButton authFab;
    
    private String avatarBase64 = "";
    private boolean isRegistering = false;
    private String currentRoleValue = RoleHelper.ROLE_STUDENT;
    private String selectedSchoolId;
    
    private AuthViewModel viewModel;
    private AlertDialog progressDialog;
    private PreferenceManager preferencesManager;
    private BottomSheetDialog avatarSelector;
    private FilePickerHelper filePickerHelper;
    private boolean isAvatarInitial = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);
        
        preferencesManager = PreferenceManager.getInstance(this);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        filePickerHelper = new FilePickerHelper(this);
        
        bindViews();
        handleSessionExpiredIntent(getIntent());
        initialize();
        initializeLogic();
        observeViewModel();
    }
    
    private void bindViews() {
        loginLayout = findViewById(R.id.mainLayout);
        registerLayout = findViewById(R.id.registerLayout);
        noAccountText = findViewById(R.id.noAccountText);
        existingAccountText = findViewById(R.id.existingAccountText);
        avatarCardView = findViewById(R.id.avatarCardView);
        settingsButton = findViewById(R.id.settingsButton);
        avatarImageView = findViewById(R.id.avatarImageView);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerUsernameEditText = findViewById(R.id.registerUsernameEditText);
        registerPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        idCardEditText = findViewById(R.id.idEditText);
        groupEditText = findViewById(R.id.groupEditText);
        tuteeEditText = findViewById(R.id.tuteeEditText);
        responsibilitiesEditText = findViewById(R.id.responsibilitiesEditText);

        usernameTextInputLayout = findViewById(R.id.usernameTextInputLayout);
        passwordTextInputLayout = findViewById(R.id.passwordTextInputLayout);
        registerUsernameTextInputLayout = findViewById(R.id.registerUsernameTextInputLayout);
        registerPasswordTextInputLayout = findViewById(R.id.newPasswordTextInputLayout);
        confirmPasswordTextInputLayout = findViewById(R.id.confirmPasswordTextInputLayout);
        firstNameTextInputLayout = findViewById(R.id.firstNameTextInputLayout);
        lastNameTextInputLayout = findViewById(R.id.lastNameTextInputLayout);
        idCardTextInputLayout = findViewById(R.id.idTextInputLayout);
        
        roleAutoComplete = findViewById(R.id.roleAutoComplete);
        roleTextInputLayout = findViewById(R.id.roleTextInputLayout);
        provinceAutoComplete = findViewById(R.id.provinceAutoComplete);
        provinceTextInputLayout = findViewById(R.id.provinceTextInputLayout);
        municipalityAutoComplete = findViewById(R.id.municipalityAutoComplete);
        municipalityTextInputLayout = findViewById(R.id.municipalityTextInputLayout);
        schoolAutoComplete = findViewById(R.id.schoolAutoComplete);
        schoolTextInputLayout = findViewById(R.id.schoolTextInputLayout);
        
        dynamicFieldsLayout = findViewById(R.id.dynamicFieldsLayout);
        studentLayout = findViewById(R.id.studentLayout);
        tutorLayout = findViewById(R.id.tutorLayout);
        teacherLayout = findViewById(R.id.teacherLayout);
        
        groupTextInputLayout = findViewById(R.id.groupTextInputLayout);
        tuteeTextInputLayout = findViewById(R.id.tuteeTextInputLayout);
        responsibilitiesTextInputLayout = findViewById(R.id.responsibilitiesTextInputLayout);
        authFab = findViewById(R.id.authFab);
    }

    private void initialize() {
        Validator.initializePasswordValidator(this);
        noAccountText.setOnClickListener(v -> switchToRegister());
        existingAccountText.setOnClickListener(v -> switchToLogin());
        avatarCardView.setOnClickListener(v -> avatarSelector.show());
        settingsButton.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        
        setupRoleSelector();
        setupLocationLogic();
        setupTextWatchers();
        
        authFab.setOnClickListener(v -> {
            AppUtils.hideKeyboard(this);
            if (isRegistering) { if (validateRegistrationForm()) performSignup(); }
            else { if (validateLoginForm()) performLogin(); }
        });
    }

    private void setupRoleSelector() {
        List<String> roleList = RoleHelper.getLocalizedRoles(this);
        roleAutoComplete.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roleList));
        roleAutoComplete.setText(roleList.get(0), false);
        roleAutoComplete.setOnItemClickListener((p, v, pos, id) -> {
            currentRoleValue = RoleHelper.getRoleValueFromLocalized(roleList.get(pos), this);
            updateDynamicFields();
            if (currentRoleValue.equals(RoleHelper.ROLE_STUDENT)) viewModel.loadProvinces();
        });
    }

    private void setupLocationLogic() {
        provinceAutoComplete.setOnItemClickListener((p, v, pos, id) -> {
            Province sel = (Province) p.getItemAtPosition(pos);
            viewModel.loadMunicipalities(sel.getId());
            municipalityAutoComplete.setText(""); schoolAutoComplete.setText("");
            municipalityTextInputLayout.setEnabled(false); schoolTextInputLayout.setEnabled(false);
            selectedSchoolId = null;
        });

        municipalityAutoComplete.setOnItemClickListener((p, v, pos, id) -> {
            Municipality sel = (Municipality) p.getItemAtPosition(pos);
            viewModel.loadSchools(sel.getId());
            schoolAutoComplete.setText("");
            schoolTextInputLayout.setEnabled(false);
            selectedSchoolId = null;
        });

        schoolAutoComplete.setOnItemClickListener((p, v, pos, id) -> {
            School sel = (School) p.getItemAtPosition(pos);
            selectedSchoolId = sel.getId();
        });
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(this, loading -> {
            if (loading) progressDialog = DialogHelper.showProgressDialog(this, isRegistering ? getString(R.string.label_creating_account) : getString(R.string.label_logging_in), false);
            else if (progressDialog != null) progressDialog.dismiss();
        });

        viewModel.getIsLocationLoading().observe(this, loading -> {
            // Podríamos mostrar un pequeño spinner en los campos, por ahora solo deshabilitamos
        });

        viewModel.getProvinces().observe(this, list -> {
            provinceAutoComplete.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, list));
            provinceTextInputLayout.setEnabled(true);
        });

        viewModel.getMunicipalities().observe(this, list -> {
            municipalityAutoComplete.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, list));
            municipalityTextInputLayout.setEnabled(true);
        });

        viewModel.getSchools().observe(this, list -> {
            schoolAutoComplete.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, list));
            schoolTextInputLayout.setEnabled(true);
        });

        viewModel.getLoginResult().observe(this, res -> { if (res != null && res.isSuccess()) handleSuccessResponse(res.getData()); });
        viewModel.getSignupResult().observe(this, res -> { if (res != null && res.isSuccess()) handleSuccessResponse(res.getData()); });
        viewModel.getError().observe(this, err -> { if (err != null) DialogHelper.showErrorDialog(this, err); });
    }

    private void performSignup() {
        HashMap<String, Object> extra = new HashMap<>();
        if (currentRoleValue.equals(RoleHelper.ROLE_STUDENT)) {
            extra.put("schoolId", selectedSchoolId);
            extra.put("group", groupEditText.getText().toString().trim());
        } else if (currentRoleValue.equals(RoleHelper.ROLE_TUTOR)) {
            extra.put("tutee", tuteeEditText.getText().toString().trim());
        } else if (currentRoleValue.equals(RoleHelper.ROLE_TEACHER)) {
            extra.put("responsibilities", responsibilitiesEditText.getText().toString().trim());
        }
        if (!avatarBase64.isEmpty()) extra.put("avatar", avatarBase64);
        
        viewModel.signup(registerUsernameEditText.getText().toString().trim(), registerPasswordEditText.getText().toString().trim(),
                         firstNameEditText.getText().toString().trim(), lastNameEditText.getText().toString().trim(),
                         idCardEditText.getText().toString().trim(), currentRoleValue, extra);
    }

    private boolean validateLoginForm() {
        boolean v1 = Validator.isValidUsername(usernameEditText.getText().toString());
        usernameTextInputLayout.setError(v1 ? null : getString(R.string.error_invalid_username));
        boolean v2 = Validator.isValidPassword(passwordEditText.getText().toString());
        passwordTextInputLayout.setError(v2 ? null : getString(R.string.error_invalid_password));
        return v1 && v2;
    }

    private boolean validateRegistrationForm() {
        boolean v1 = Validator.isValidUsername(registerUsernameEditText.getText().toString());
        registerUsernameTextInputLayout.setError(v1 ? null : getString(R.string.error_invalid_username));
        boolean v2 = Validator.isValidPassword(registerPasswordEditText.getText().toString());
        registerPasswordTextInputLayout.setError(v2 ? null : getString(R.string.error_invalid_password));
        boolean v3 = registerPasswordEditText.getText().toString().equals(confirmPasswordEditText.getText().toString());
        confirmPasswordTextInputLayout.setError(v3 ? null : getString(R.string.error_password_mismatch));
        boolean v4 = Validator.isValidFirstName(firstNameEditText.getText().toString());
        firstNameTextInputLayout.setError(v4 ? null : getString(R.string.error_invalid_first_name));
        boolean v5 = Validator.isValidCubanCI(idCardEditText.getText().toString());
        idCardTextInputLayout.setError(v5 ? null : getString(R.string.error_invalid_id));
        
        if (currentRoleValue.equals(RoleHelper.ROLE_STUDENT) && selectedSchoolId == null) {
            schoolTextInputLayout.setError(getString(R.string.error_school_required));
            return false;
        }
        return v1 && v2 && v3 && v4 && v5;
    }

    private void handleSuccessResponse(AuthResponse data) {
        preferencesManager.setSupabaseAccessToken(data.getSessionToken());
        preferencesManager.setSupabaseUserId(data.getUser().getId());
        preferencesManager.setSchoolId(data.getProfile().getSchoolId());
        preferencesManager.setUsername(data.getProfile().getUsername());
        preferencesManager.setHasAccount(true);
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private void initializeLogic() {
        int color = ThemeManager.getThemeColor(this, R.attr.colorBackground);
        getWindow().setStatusBarColor(color); getWindow().setNavigationBarColor(color);
        if (preferencesManager.getLoginSessionCount() == 0) switchToRegister(); else switchToLogin();
        initializeAvatarSelector();
        if (isRegistering && currentRoleValue.equals(RoleHelper.ROLE_STUDENT)) viewModel.loadProvinces();
    }

    private void updateDynamicFields() {
        studentLayout.setVisibility(currentRoleValue.equals(RoleHelper.ROLE_STUDENT) ? View.VISIBLE : View.GONE);
        tutorLayout.setVisibility(currentRoleValue.equals(RoleHelper.ROLE_TUTOR) ? View.VISIBLE : View.GONE);
        teacherLayout.setVisibility(currentRoleValue.equals(RoleHelper.ROLE_TEACHER) ? View.VISIBLE : View.GONE);
    }

    private void setupTextWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { if (isRegistering && isAvatarInitial) updateInitialAvatar(); }
        };
        firstNameEditText.addTextChangedListener(watcher);
        registerUsernameEditText.addTextChangedListener(watcher);
    }

    private void updateInitialAvatar() {
        String initial = firstNameEditText.getText().toString().trim();
        initial = initial.isEmpty() ? registerUsernameEditText.getText().toString().trim() : initial;
        initial = initial.isEmpty() ? "U" : initial.substring(0, 1).toUpperCase();
        avatarBase64 = "{\"tipo\":\"inicial\",\"inicial\":\"" + initial + "\"}";
        avatarImageView.setImageBitmap(AvatarHelper.generateInitialAvatar(this, initial, ThemeManager.getColorAccent(this)));
    }

    private void initializeAvatarSelector() {
        avatarSelector = new BottomSheetDialog(this);
        View v = getLayoutInflater().inflate(R.layout.bottomsheet_avatar_selector, null);
        avatarSelector.setContentView(v);
        v.findViewById(R.id.uploadImageLayout).setOnClickListener(view -> filePickerHelper.pickFile("image/jpeg", new FilePickerHelper.OnFilePickedListener() {
            @Override public void onFilePicked(String path, String name, String mime, String base64) {
                avatarBase64 = base64; isAvatarInitial = false;
                avatarImageView.setImageBitmap(FileUtils.decodeSampleBitmapFromPath(path, 1024, 1024));
                avatarSelector.dismiss();
            }
            @Override public void onPickCancelled() {}
        }));
        v.findViewById(R.id.initialLayout).setOnClickListener(view -> { isAvatarInitial = true; updateInitialAvatar(); avatarSelector.dismiss(); });
        v.findViewById(R.id.roleLayout).setOnClickListener(view -> { isAvatarInitial = false; avatarBase64 = "{\"tipo\":\"rol\",\"rol\":\"" + currentRoleValue + "\"}"; avatarImageView.setImageBitmap(AvatarHelper.generateRoleAvatar(this, currentRoleValue, ThemeManager.getColorAccent(this))); avatarSelector.dismiss(); });
    }

    private void switchToRegister() {
        isRegistering = true; authFab.setText(getString(R.string.button_register));
        loginLayout.animate().alpha(0f).setDuration(250).setListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator a) { loginLayout.setVisibility(View.GONE); registerLayout.setVisibility(View.VISIBLE); registerLayout.setAlpha(0f); registerLayout.animate().alpha(1f).setDuration(250).start(); if (currentRoleValue.equals(RoleHelper.ROLE_STUDENT)) viewModel.loadProvinces(); }
        }).start();
    }

    private void switchToLogin() {
        isRegistering = false; authFab.setText(getString(R.string.button_login));
        registerLayout.animate().alpha(0f).setDuration(250).setListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator a) { registerLayout.setVisibility(View.GONE); loginLayout.setVisibility(View.VISIBLE); loginLayout.setAlpha(0f); loginLayout.animate().alpha(1f).setDuration(250).start(); }
        }).start();
    }

    private void handleSessionExpiredIntent(Intent i) {
        if (i != null && i.getBooleanExtra("session_expired", false)) {
            usernameEditText.setText(""); passwordEditText.setText("");
            if (isRegistering) switchToLogin();
        }
    }

    private void performLogin() { viewModel.login(usernameEditText.getText().toString().trim(), passwordEditText.getText().toString().trim()); }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!filePickerHelper.handleActivityResult(requestCode, resultCode, data)) super.onActivityResult(requestCode, resultCode, data);
    }
}
