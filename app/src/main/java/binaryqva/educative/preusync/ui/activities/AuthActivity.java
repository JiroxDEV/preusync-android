/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: AuthActivity.java
 * Versión: v16.0.0
 * Descripción: Actividad de Autenticación con registro dividido en pasos.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.List;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.network.models.AuthResponse;
import binaryqva.educative.preusync.network.models.Municipality;
import binaryqva.educative.preusync.network.models.Province;
import binaryqva.educative.preusync.network.models.School;
import binaryqva.educative.preusync.ui.viewmodels.AuthViewModel;
import binaryqva.educative.preusync.utils.common.AppUtils;
import binaryqva.educative.preusync.utils.common.DialogHelper;
import binaryqva.educative.preusync.utils.common.FilePickerHelper;
import binaryqva.educative.preusync.utils.common.FileUtils;
import binaryqva.educative.preusync.utils.common.PreferenceManager;
import binaryqva.educative.preusync.utils.common.RoleHelper;
import binaryqva.educative.preusync.utils.common.Validator;
import binaryqva.educative.preusync.utils.theme.ThemeManager;

public class AuthActivity extends BaseActivity {

    private ViewFlipper authViewFlipper, registerStepFlipper;
    private MaterialButtonToggleGroup authToggle;
    private ExtendedFloatingActionButton authFab;
    private MaterialButton backStepButton;
    private View step1Ind, step2Ind, step3Ind;
    
    private EditText usernameET, passwordET, regUsernameET, regPasswordET, confirmPasswordET,
            firstNameET, lastNameET, idCardET, groupET, tuteeET, responsibilitiesET;
    private TextInputLayout usernameTIL, passwordTIL, regUsernameTIL, regPasswordTIL,
            confirmPasswordTIL, firstNameTIL, lastNameTIL, idCardTIL, roleTIL,
            provinceTIL, municipalityTIL, schoolTIL;
    
    private AutoCompleteTextView roleAC, provinceAC, municipalityAC, schoolAC;
    private View studentLayout, tutorLayout, teacherLayout;
    private ImageView avatarIV;

    private AuthViewModel viewModel;
    private PreferenceManager prefs;
    private FilePickerHelper filePicker;
    private AlertDialog progressDialog;
    
    private String selectedSchoolId;
    private String avatarBase64 = "";
    private boolean isRegistering = false;
    private int currentRegStep = 0;
    private String currentRole = RoleHelper.ROLE_STUDENT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        prefs = PreferenceManager.getInstance(this);
        filePicker = new FilePickerHelper(this);

        bindViews();
        setupListeners();
        observeViewModel();
        initializeLogic();
        
        if (getIntent().getBooleanExtra("extra_is_registering", false)) {
            authToggle.check(R.id.toggleRegister);
            switchToRegister();
        }
    }

    private void bindViews() {
        authViewFlipper = findViewById(R.id.authViewFlipper);
        registerStepFlipper = findViewById(R.id.registerStepFlipper);
        authToggle = findViewById(R.id.authToggleGroup);
        authFab = findViewById(R.id.authFab);
        backStepButton = findViewById(R.id.backStepButton);
        avatarIV = findViewById(R.id.avatarImageView);
        
        step1Ind = findViewById(R.id.step1Indicator);
        step2Ind = findViewById(R.id.step2Indicator);
        step3Ind = findViewById(R.id.step3Indicator);

        usernameET = findViewById(R.id.usernameEditText);
        passwordET = findViewById(R.id.passwordEditText);
        regUsernameET = findViewById(R.id.registerUsernameEditText);
        regPasswordET = findViewById(R.id.newPasswordEditText);
        confirmPasswordET = findViewById(R.id.confirmPasswordEditText);
        firstNameET = findViewById(R.id.firstNameEditText);
        lastNameET = findViewById(R.id.lastNameEditText);
        idCardET = findViewById(R.id.idEditText);
        groupET = findViewById(R.id.groupEditText);
        tuteeET = findViewById(R.id.tuteeEditText);
        responsibilitiesET = findViewById(R.id.responsibilitiesEditText);

        usernameTIL = findViewById(R.id.usernameTextInputLayout);
        passwordTIL = findViewById(R.id.passwordTextInputLayout);
        regUsernameTIL = findViewById(R.id.registerUsernameTextInputLayout);
        regPasswordTIL = findViewById(R.id.newPasswordTextInputLayout);
        confirmPasswordTIL = findViewById(R.id.confirmPasswordTextInputLayout);
        firstNameTIL = findViewById(R.id.firstNameTextInputLayout);
        lastNameTIL = findViewById(R.id.lastNameTextInputLayout);
        idCardTIL = findViewById(R.id.idTextInputLayout);
        roleTIL = findViewById(R.id.roleTextInputLayout);
        provinceTIL = findViewById(R.id.provinceTextInputLayout);
        municipalityTIL = findViewById(R.id.municipalityTextInputLayout);
        schoolTIL = findViewById(R.id.schoolTextInputLayout);

        roleAC = findViewById(R.id.roleAutoComplete);
        provinceAC = findViewById(R.id.provinceAutoComplete);
        municipalityAC = findViewById(R.id.municipalityAutoComplete);
        schoolAC = findViewById(R.id.schoolAutoComplete);

        studentLayout = findViewById(R.id.studentLayout);
        tutorLayout = findViewById(R.id.tuteeTextInputLayout);
        teacherLayout = findViewById(R.id.responsibilitiesTextInputLayout);

        authViewFlipper.setInAnimation(this, android.R.anim.fade_in);
        authViewFlipper.setOutAnimation(this, android.R.anim.fade_out);
        registerStepFlipper.setInAnimation(this, R.anim.slide_in_right);
        registerStepFlipper.setOutAnimation(this, R.anim.slide_out_left);
    }

    private void setupListeners() {
        authToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.toggleLogin) switchToLogin();
            else switchToRegister();
        });

        authFab.setOnClickListener(v -> handleAuthAction());
        backStepButton.setOnClickListener(v -> goBackStep());

        findViewById(R.id.avatarCardView).setOnClickListener(v -> pickAvatar());
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        setupSelectors();
    }

    private void setupSelectors() {
        List<String> roles = RoleHelper.getLocalizedRoles(this);
        roleAC.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roles));
        roleAC.setOnItemClickListener((p, v, pos, id) -> {
            currentRole = RoleHelper.getRoleValueFromLocalized(roles.get(pos), this);
            updateRoleFields();
        });

        provinceAC.setOnItemClickListener((p, v, pos, id) -> {
            Province sel = (Province) p.getItemAtPosition(pos);
            viewModel.loadMunicipalities(sel.getId());
            municipalityAC.setText(""); schoolAC.setText("");
            municipalityTIL.setEnabled(false); schoolTIL.setEnabled(false);
            selectedSchoolId = null;
        });

        municipalityAC.setOnItemClickListener((p, v, pos, id) -> {
            Municipality sel = (Municipality) p.getItemAtPosition(pos);
            viewModel.loadSchools(sel.getId());
            schoolAC.setText("");
            schoolTIL.setEnabled(false);
            selectedSchoolId = null;
        });

        schoolAC.setOnItemClickListener((p, v, pos, id) -> {
            School sel = (School) p.getItemAtPosition(pos);
            selectedSchoolId = sel.getId();
        });
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(this, loading -> {
            if (loading) progressDialog = DialogHelper.showProgressDialog(this, isRegistering ? getString(R.string.label_creating_account) : getString(R.string.label_logging_in), false);
            else if (progressDialog != null) progressDialog.dismiss();
        });

        viewModel.getProvinces().observe(this, list -> provinceAC.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, list)));
        viewModel.getMunicipalities().observe(this, list -> { municipalityAC.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, list)); municipalityTIL.setEnabled(true); });
        viewModel.getSchools().observe(this, list -> { schoolAC.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, list)); schoolTIL.setEnabled(true); });

        viewModel.getLoginResult().observe(this, res -> { if (res != null && res.isSuccess()) loginSuccess(res.getData()); });
        viewModel.getSignupResult().observe(this, res -> { if (res != null && res.isSuccess()) loginSuccess(res.getData()); });
        viewModel.getError().observe(this, err -> { if (err != null) DialogHelper.showErrorDialog(this, err); });
    }

    private void switchToLogin() {
        isRegistering = false;
        authFab.setText(R.string.button_login);
        authFab.setIconResource(R.drawable.ic_check);
        backStepButton.setVisibility(View.GONE);
        
        authViewFlipper.setInAnimation(this, R.anim.slide_in_left);
        authViewFlipper.setOutAnimation(this, R.anim.slide_out_right);
        authViewFlipper.setDisplayedChild(0);
    }

    private void switchToRegister() {
        isRegistering = true;
        currentRegStep = 0;
        updateStepUi();
        
        authViewFlipper.setInAnimation(this, R.anim.slide_in_right);
        authViewFlipper.setOutAnimation(this, R.anim.slide_out_left);
        authViewFlipper.setDisplayedChild(1);
        if (viewModel.getProvinces().getValue() == null) viewModel.loadProvinces();
    }

    private void updateStepUi() {
        registerStepFlipper.setDisplayedChild(currentRegStep);
        backStepButton.setVisibility(currentRegStep == 0 ? View.GONE : View.VISIBLE);
        authFab.setText(currentRegStep == 2 ? R.string.button_register : R.string.button_next);
        authFab.setIconResource(currentRegStep == 2 ? R.drawable.ic_check : R.drawable.ic_plus);
        
        int accent = ThemeManager.getColorAccent(this);
        int highlight = ThemeManager.getColorControlHighlight(this);
        step1Ind.setBackgroundColor(currentRegStep >= 0 ? accent : highlight);
        step2Ind.setBackgroundColor(currentRegStep >= 1 ? accent : highlight);
        step3Ind.setBackgroundColor(currentRegStep >= 2 ? accent : highlight);
    }

    private void goBackStep() {
        if (currentRegStep > 0) {
            currentRegStep--;
            registerStepFlipper.setInAnimation(this, R.anim.slide_in_left);
            registerStepFlipper.setOutAnimation(this, R.anim.slide_out_right);
            updateStepUi();
        }
    }

    private void updateRoleFields() {
        studentLayout.setVisibility(currentRole.equals(RoleHelper.ROLE_STUDENT) ? View.VISIBLE : View.GONE);
        tutorLayout.setVisibility(currentRole.equals(RoleHelper.ROLE_TUTOR) ? View.VISIBLE : View.GONE);
        teacherLayout.setVisibility(currentRole.equals(RoleHelper.ROLE_TEACHER) ? View.VISIBLE : View.GONE);
    }

    private void handleAuthAction() {
        AppUtils.hideKeyboard(this);
        if (isRegistering) {
            if (validateStep(currentRegStep)) {
                if (currentRegStep < 2) {
                    currentRegStep++;
                    registerStepFlipper.setInAnimation(this, R.anim.slide_in_right);
                    registerStepFlipper.setOutAnimation(this, R.anim.slide_out_left);
                    updateStepUi();
                } else submitRegister();
            }
        } else {
            if (validateLogin()) viewModel.login(usernameET.getText().toString().trim(), passwordET.getText().toString().trim());
        }
    }

    private boolean validateStep(int step) {
        if (step == 0) {
            boolean v1 = Validator.isValidUsername(regUsernameET.getText().toString());
            regUsernameTIL.setError(v1 ? null : getString(R.string.error_invalid_username));
            boolean v2 = Validator.isValidPassword(regPasswordET.getText().toString());
            regPasswordTIL.setError(v2 ? null : getString(R.string.error_invalid_password));
            boolean v3 = regPasswordET.getText().toString().equals(confirmPasswordET.getText().toString());
            confirmPasswordTIL.setError(v3 ? null : getString(R.string.error_password_mismatch));
            return v1 && v2 && v3;
        } else if (step == 1) {
            boolean v4 = Validator.isValidFirstName(firstNameET.getText().toString());
            firstNameTIL.setError(v4 ? null : getString(R.string.error_invalid_first_name));
            boolean v5 = Validator.isValidLastName(lastNameET.getText().toString());
            lastNameTIL.setError(v5 ? null : getString(R.string.error_invalid_last_name));
            boolean v6 = Validator.isValidCubanCI(idCardET.getText().toString());
            idCardTIL.setError(v6 ? null : getString(R.string.error_invalid_id));
            return v4 && v5 && v6;
        } else {
            if (currentRole.equals(RoleHelper.ROLE_STUDENT) && selectedSchoolId == null) {
                schoolTIL.setError(getString(R.string.error_school_required)); return false;
            }
            return true;
        }
    }

    private void submitRegister() {
        HashMap<String, Object> extra = new HashMap<>();
        if (currentRole.equals(RoleHelper.ROLE_STUDENT)) {
            extra.put("schoolId", selectedSchoolId);
            extra.put("group", groupET.getText().toString().trim());
        } else if (currentRole.equals(RoleHelper.ROLE_TUTOR)) extra.put("tutee", tuteeET.getText().toString().trim());
        else if (currentRole.equals(RoleHelper.ROLE_TEACHER)) extra.put("responsibilities", responsibilitiesET.getText().toString().trim());
        if (!avatarBase64.isEmpty()) extra.put("avatar", avatarBase64);
        
        viewModel.signup(regUsernameET.getText().toString().trim(), regPasswordET.getText().toString().trim(),
                firstNameET.getText().toString().trim(), lastNameET.getText().toString().trim(),
                idCardET.getText().toString().trim(), currentRole, extra);
    }

    private boolean validateLogin() {
        boolean v = Validator.isValidUsername(usernameET.getText().toString());
        usernameTIL.setError(v ? null : getString(R.string.error_invalid_username));
        boolean p = Validator.isValidPassword(passwordET.getText().toString());
        passwordTIL.setError(p ? null : getString(R.string.error_invalid_password));
        return v && p;
    }

    private void loginSuccess(AuthResponse data) {
        prefs.setSupabaseAccessToken(data.getSessionToken());
        prefs.setSupabaseUserId(data.getUser().getId());
        prefs.setSchoolId(data.getProfile().getSchoolId());
        prefs.setUsername(data.getProfile().getUsername());
        prefs.setHasAccount(true);
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private void pickAvatar() {
        filePicker.pickFile("image/jpeg", new FilePickerHelper.OnFilePickedListener() {
            @Override public void onFilePicked(String path, String name, String mime, String base64) {
                avatarBase64 = base64;
                avatarIV.setImageBitmap(FileUtils.decodeSampleBitmapFromPath(path, 512, 512));
            }
            @Override public void onPickCancelled() {}
        });
    }

    private void initializeLogic() {
        int color = ThemeManager.getThemeColor(this, R.attr.colorBackground);
        getWindow().setStatusBarColor(color); getWindow().setNavigationBarColor(color);
    }
}
