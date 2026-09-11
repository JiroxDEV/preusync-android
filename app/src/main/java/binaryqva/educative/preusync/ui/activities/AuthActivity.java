/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: AuthActivity.java
 * Versión: v19.0.0
 * Descripción: Actividad de Autenticación avanzada con registro nacional
 *              jerárquico, validación de roles y diseño minimalista.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.network.models.AuthResponse;
import binaryqva.educative.preusync.network.models.Municipality;
import binaryqva.educative.preusync.network.models.Province;
import binaryqva.educative.preusync.network.models.School;
import binaryqva.educative.preusync.network.models.SchoolGroup;
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

    private ViewGroup authRootLayout;
    private ViewFlipper authViewFlipper, registerStepFlipper;
    private MaterialButtonToggleGroup authToggle;
    private ExtendedFloatingActionButton authFab;
    private MaterialButton backStepButton;
    private View step1Ind, step2Ind, step3Ind;
    
    private EditText usernameET, passwordET, regUsernameET, regPasswordET, confirmPasswordET,
            firstNameET, lastNameET, idCardET, tuteeET, responsibilitiesET;
    private TextInputLayout usernameTIL, passwordTIL, regUsernameTIL, regPasswordTIL,
            confirmPasswordTIL, firstNameTIL, lastNameTIL, idCardTIL, roleTIL,
            provinceTIL, municipalityTIL, schoolTIL, groupTIL, tuteeTIL, responsibilitiesTIL;
    
    private AutoCompleteTextView roleAC, provinceAC, municipalityAC, schoolAC, groupAC;
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
    private final List<String> selectedResponsibilities = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

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
        authRootLayout = findViewById(R.id.authRootLayout);
        authViewFlipper = findViewById(R.id.authViewFlipper);
        registerStepFlipper = findViewById(R.id.registerStepFlipper);
        authToggle = findViewById(R.id.authToggleGroup);
        authFab = findViewById(R.id.authFab);
        backStepButton = findViewById(R.id.backStepButton);
        
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
        tuteeET = findViewById(R.id.tuteeEditText);
        responsibilitiesET = findViewById(R.id.responsibilitiesEditText);
        avatarIV = findViewById(R.id.avatarImageView);

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
        groupTIL = findViewById(R.id.groupTextInputLayout);
        tuteeTIL = findViewById(R.id.tuteeTextInputLayout);
        responsibilitiesTIL = findViewById(R.id.responsibilitiesTextInputLayout);

        roleAC = findViewById(R.id.roleAutoComplete);
        provinceAC = findViewById(R.id.provinceAutoComplete);
        municipalityAC = findViewById(R.id.municipalityAutoComplete);
        schoolAC = findViewById(R.id.schoolAutoComplete);
        groupAC = findViewById(R.id.groupAutoComplete);

        authViewFlipper.setInAnimation(this, android.R.anim.fade_in);
        authViewFlipper.setOutAnimation(this, android.R.anim.fade_out);
    }

    private void setupListeners() {
        authToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            TransitionManager.beginDelayedTransition(authRootLayout);
            if (checkedId == R.id.toggleLogin) switchToLogin();
            else switchToRegister();
        });

        authFab.setOnClickListener(v -> handleAuthAction());
        backStepButton.setOnClickListener(v -> goBackStep());
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        setupSelectors();
        setupInputWatchers();
    }

    private void setupSelectors() {
        List<String> roles = RoleHelper.getLocalizedRoles(this);
        roleAC.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roles));
        roleAC.setText(RoleHelper.getLocalizedFromRoleValue(RoleHelper.ROLE_STUDENT, this), false);
        roleAC.setOnItemClickListener((p, v, pos, id) -> {
            currentRole = RoleHelper.getRoleValueFromLocalized(roles.get(pos), this);
            updateRoleFields();
        });

        provinceAC.setOnTouchListener((v, event) -> { if (viewModel.getProvinces().getValue() == null) viewModel.loadProvinces(); return false; });
        provinceAC.setOnItemClickListener((p, v, pos, id) -> {
            Object item = p.getItemAtPosition(pos);
            if (item instanceof Province) { viewModel.loadMunicipalities(((Province) item).getId()); clearDownstreamSelectors(true, true, true); }
        });

        municipalityAC.setOnTouchListener((v, event) -> { 
            if (provinceAC.getText().toString().isEmpty()) provinceTIL.setError(getString(R.string.error_select_province));
            return false; 
        });
        municipalityAC.setOnItemClickListener((p, v, pos, id) -> {
            Object item = p.getItemAtPosition(pos);
            if (item instanceof Municipality) { viewModel.loadSchools(((Municipality) item).getId()); clearDownstreamSelectors(false, true, true); }
        });

        schoolAC.setOnTouchListener((v, event) -> {
            if (municipalityAC.getText().toString().isEmpty()) municipalityTIL.setError(getString(R.string.error_select_municipality));
            return false;
        });
        schoolAC.setOnItemClickListener((p, v, pos, id) -> {
            Object item = p.getItemAtPosition(pos);
            if (item instanceof School) { selectedSchoolId = ((School) item).getId(); viewModel.loadGroups(selectedSchoolId); clearDownstreamSelectors(false, false, true); }
        });

        groupAC.setOnTouchListener((v, event) -> {
            if (schoolAC.getText().toString().isEmpty()) schoolTIL.setError(getString(R.string.error_select_school));
            return false;
        });

        responsibilitiesET.setOnClickListener(v -> showResponsibilitiesDialog());
        findViewById(R.id.avatarCardView).setOnClickListener(v -> showAvatarSelector());
    }

    private void clearDownstreamSelectors(boolean mun, boolean sch, boolean grp) {
        if (mun) { municipalityAC.setText(""); municipalityTIL.setEnabled(false); }
        if (sch) { schoolAC.setText(""); schoolTIL.setEnabled(false); selectedSchoolId = null; }
        if (grp) { groupAC.setText(""); groupTIL.setEnabled(false); }
        provinceTIL.setError(null); municipalityTIL.setError(null); schoolTIL.setError(null);
    }

    private void setupInputWatchers() {
        tuteeET.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String u = s.toString().trim();
                if (u.length() >= 4) viewModel.checkUserExists(u);
                else tuteeTIL.setHelperText(getString(R.string.label_min_characters));
            }
        });
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(this, loading -> {
            if (loading) progressDialog = DialogHelper.showProgressDialog(this, isRegistering ? getString(R.string.label_creating_account) : getString(R.string.label_logging_in), false);
            else if (progressDialog != null) progressDialog.dismiss();
        });

        viewModel.getProvinces().observe(this, list -> setSelectorAdapter(provinceAC, list));
        viewModel.getMunicipalities().observe(this, list -> { setSelectorAdapter(municipalityAC, list); municipalityTIL.setEnabled(true); });
        viewModel.getSchools().observe(this, list -> { setSelectorAdapter(schoolAC, list); schoolTIL.setEnabled(true); });
        viewModel.getGroups().observe(this, list -> { setSelectorAdapter(groupAC, list); groupTIL.setEnabled(true); });
        
        viewModel.getUserExistsResult().observe(this, exists -> {
            if (currentRole.equals(RoleHelper.ROLE_TUTOR) && !tuteeET.getText().toString().isEmpty()) {
                boolean ok = Boolean.TRUE.equals(exists);
                tuteeTIL.setHelperText(ok ? getString(R.string.label_tutee_verified) + " ✅" : getString(R.string.label_tutee_not_found) + " ❌");
                tuteeTIL.setHelperTextColor(ColorStateList.valueOf(ThemeManager.getThemeColor(this, ok ? R.attr.colorAccent : R.attr.colorError)));
            }
        });

        viewModel.getLoginResult().observe(this, res -> { if (res != null && res.isSuccess()) loginSuccess(res.getData()); });
        viewModel.getSignupResult().observe(this, res -> { if (res != null && res.isSuccess()) loginSuccess(res.getData()); });
        viewModel.getError().observe(this, err -> { if (err != null) DialogHelper.showErrorDialog(this, err); });
    }

    private <T> void setSelectorAdapter(AutoCompleteTextView ac, List<T> list) {
        if (list == null || list.isEmpty()) {
            ac.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new String[]{getString(R.string.label_not_available_temp)}));
            return;
        }
        
        List<T> availableItems = new ArrayList<>();
        for (T item : list) {
            boolean available = true;
            if (item instanceof Province) available = ((Province) item).isAvailable();
            else if (item instanceof Municipality) available = ((Municipality) item).isAvailable();
            else if (item instanceof School) available = ((School) item).isAvailable();
            else if (item instanceof SchoolGroup) available = ((SchoolGroup) item).isAvailable();
            if (available) availableItems.add(item);
        }

        if (availableItems.isEmpty()) {
            ac.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new String[]{getString(R.string.label_not_available_temp)}));
        } else {
            ac.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, availableItems));
        }
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
    }

    private void updateStepUi() {
        registerStepFlipper.setDisplayedChild(currentRegStep);
        backStepButton.setVisibility(currentRegStep == 0 ? View.GONE : View.VISIBLE);
        authFab.setText(currentRegStep == 2 ? R.string.button_register : R.string.button_next);
        authFab.setIconResource(currentRegStep == 2 ? R.drawable.ic_check : R.drawable.ic_arrow_forward);
        
        step1Ind.setBackgroundResource(currentRegStep >= 0 ? R.drawable.bg_onboarding_indicator_active : R.drawable.bg_onboarding_indicator_inactive);
        step2Ind.setBackgroundResource(currentRegStep >= 1 ? R.drawable.bg_onboarding_indicator_active : R.drawable.bg_onboarding_indicator_inactive);
        step3Ind.setBackgroundResource(currentRegStep >= 2 ? R.drawable.bg_onboarding_indicator_active : R.drawable.bg_onboarding_indicator_inactive);
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
        groupTIL.setVisibility(currentRole.equals(RoleHelper.ROLE_STUDENT) ? View.VISIBLE : View.GONE);
        tuteeTIL.setVisibility(currentRole.equals(RoleHelper.ROLE_TUTOR) ? View.VISIBLE : View.GONE);
        responsibilitiesTIL.setVisibility(currentRole.equals(RoleHelper.ROLE_TEACHER) ? View.VISIBLE : View.GONE);
    }

    private void showResponsibilitiesDialog() {
        viewModel.loadResponsibilities();
        viewModel.getResponsibilities().observe(this, list -> {
            if (list == null || list.isEmpty()) return;
            String[] items = list.toArray(new String[0]);
            boolean[] checked = new boolean[items.length];
            for (int i = 0; i < items.length; i++) checked[i] = selectedResponsibilities.contains(items[i]);
            new AlertDialog.Builder(this).setTitle(getString(R.string.title_select_responsibilities))
                .setMultiChoiceItems(items, checked, (dialog, which, isChecked) -> { if (isChecked) selectedResponsibilities.add(items[which]); else selectedResponsibilities.remove(items[which]); })
                .setPositiveButton(R.string.button_done, (dialog, which) -> responsibilitiesET.setText(String.join(", ", selectedResponsibilities))).show();
        });
    }

    private void showAvatarSelector() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View v = LayoutInflater.from(this).inflate(R.layout.bottomsheet_avatar_selector, null);
        dialog.setContentView(v);
        v.findViewById(R.id.uploadImageLayout).setOnClickListener(view -> filePicker.pickFile("image/*", new FilePickerHelper.OnFilePickedListener() {
            @Override public void onFilePicked(String path, String name, String mime, String base64) {
                avatarBase64 = base64; avatarIV.setImageBitmap(FileUtils.decodeSampleBitmapFromPath(path, 512, 512));
                dialog.dismiss();
            }
            @Override public void onPickCancelled() {}
        }));
        v.findViewById(R.id.initialLayout).setOnClickListener(view -> {
            String name = regUsernameET.getText().toString().trim();
            if (name.isEmpty()) name = "?";
            String initial = name.substring(0, 1).toUpperCase();
            avatarIV.setImageBitmap(AvatarHelper.generateInitialAvatar(this, initial, ThemeManager.getColorAccent(this)));
            avatarBase64 = "{\"tipo\":\"inicial\",\"inicial\":\"" + initial + "\"}";
            dialog.dismiss();
        });
        v.findViewById(R.id.roleLayout).setOnClickListener(view -> {
            avatarIV.setImageBitmap(AvatarHelper.generateRoleAvatar(this, currentRole, ThemeManager.getColorAccent(this)));
            avatarBase64 = "{\"tipo\":\"rol\",\"rol\":\"" + currentRole + "\"}";
            dialog.dismiss();
        });
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (filePicker != null) filePicker.handleActivityResult(requestCode, resultCode, data);
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
            if (provinceAC.getText().toString().isEmpty() || provinceAC.getText().toString().contains(getString(R.string.label_not_available_temp))) {
                provinceTIL.setError(getString(R.string.error_select_province)); return false;
            }
            if (municipalityAC.getText().toString().isEmpty() || municipalityAC.getText().toString().contains(getString(R.string.label_not_available_temp))) {
                municipalityTIL.setError(getString(R.string.error_select_municipality)); return false;
            }
            if (selectedSchoolId == null || schoolAC.getText().toString().contains(getString(R.string.label_not_available_temp))) {
                schoolTIL.setError(getString(R.string.error_select_school)); return false;
            }
            if (currentRole.equals(RoleHelper.ROLE_STUDENT)) {
                if (groupAC.getText().toString().isEmpty() || groupAC.getText().toString().contains(getString(R.string.label_not_available_temp))) {
                    groupTIL.setError(getString(R.string.error_group_required)); return false;
                }
            }
            if (currentRole.equals(RoleHelper.ROLE_TUTOR) && !Boolean.TRUE.equals(viewModel.getUserExistsResult().getValue())) {
                tuteeTIL.setError("Especifique un educando válido"); return false;
            }
            return true;
        }
    }

    private void submitRegister() {
        HashMap<String, Object> extra = new HashMap<>();
        extra.put("schoolId", selectedSchoolId);
        if (currentRole.equals(RoleHelper.ROLE_STUDENT)) {
            String sel = groupAC.getText().toString();
            extra.put("groupName", sel);
            List<SchoolGroup> list = viewModel.getGroups().getValue();
            if (list != null) for (SchoolGroup g : list) if (g.getName().equals(sel)) { extra.put("groupId", g.getId()); break; }
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

    private void initializeLogic() {
        int color = ThemeManager.getThemeColor(this, R.attr.colorBackground);
        getWindow().setStatusBarColor(color); getWindow().setNavigationBarColor(color);
    }
}
