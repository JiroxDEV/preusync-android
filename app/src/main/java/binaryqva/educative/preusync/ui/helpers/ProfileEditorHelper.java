/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: ProfileEditorHelper.java
 * Versión: v3.1.2
 * Descripción: Ayudante para la edición de perfiles, refactorizado para el 
 *              uso de modelos tipados (Profile).
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.helpers;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.network.models.Profile;
import binaryqva.educative.preusync.ui.viewmodels.ProfileViewModel;
import binaryqva.educative.preusync.utils.common.AvatarHelper;
import binaryqva.educative.preusync.utils.common.AppUtils;
import binaryqva.educative.preusync.utils.common.DialogHelper;
import binaryqva.educative.preusync.utils.common.FilePickerHelper;
import binaryqva.educative.preusync.utils.common.FileUtils;
import binaryqva.educative.preusync.utils.common.PreferenceManager;
import binaryqva.educative.preusync.utils.common.RoleHelper;
import binaryqva.educative.preusync.utils.common.Validator;
import binaryqva.educative.preusync.utils.theme.ThemeManager;
import binaryqva.educative.preusync.utils.ui.ButtonAnimator;

/**
 * Controla el flujo de actualización de datos de cuenta.
 */
public class ProfileEditorHelper {

    private final Context context;
    private final Activity activity;
    private final PreferenceManager preferencesManager;
    private final ProfileViewModel viewModel;
    private final FilePickerHelper filePickerHelper;

    private BottomSheetDialog editBottomSheet;
    private BottomSheetDialog avatarSelectorBottomSheet;

    private EditText firstNameEditText, lastNameEditText, usernameEditText;
    private EditText oldPasswordEditText, newPasswordEditText, confirmPasswordEditText;
    private EditText idNumberEditText;
    private AutoCompleteTextView roleAutoComplete;
    private MaterialSwitch advancedSwitch;
    private Button saveButton, cancelButton;
    private ImageView editAvatarImageView;
    private LinearLayout studentLayout, tutorLayout, teacherLayout;
    private ViewGroup advancedFieldsLayout;
    private TextInputLayout firstNameTextInputLayout, lastNameTextInputLayout;
    private TextInputLayout usernameTextInputLayout, oldPasswordTextInputLayout;
    private TextInputLayout newPasswordTextInputLayout, confirmPasswordTextInputLayout;
    private TextInputLayout idNumberTextInputLayout, roleTextInputLayout;
    private TextInputLayout schoolTextInputLayout, groupTextInputLayout;
    private TextInputLayout tuteeTextInputLayout, responsibilitiesTextInputLayout;
    private EditText schoolEditText, groupEditText, tuteeEditText, responsibilitiesEditText;

    private boolean ignoreSwitchChange = false;
    private String pathBase64 = "";
    private boolean isEditAvatarInitial = true;
    private String currentRoleValue = RoleHelper.ROLE_STUDENT;

    public interface OnSaveListener {
        void onSave(HashMap<String, Object> updateData);
        void onCancel();
    }

    public ProfileEditorHelper(Activity activity, ProfileViewModel viewModel, FilePickerHelper filePickerHelper) {
        this.context = activity; this.activity = activity;
        this.viewModel = viewModel; this.filePickerHelper = filePickerHelper;
        this.preferencesManager = PreferenceManager.getInstance(context);
    }

    public void showEditBottomSheet(Profile userData, OnSaveListener listener) {
        if (userData == null) return;
        if (editBottomSheet == null) initializeEditBottomSheet();
        fillData(userData);
        cancelButton.setOnClickListener(v -> { editBottomSheet.dismiss(); if (listener != null) listener.onCancel(); });
        saveButton.setOnClickListener(v -> {
            HashMap<String, Object> data = collectData();
            if (data != null && listener != null) listener.onSave(data);
        });
        editBottomSheet.show();
    }

    public void dismiss() {
        if (editBottomSheet != null) editBottomSheet.dismiss();
        if (avatarSelectorBottomSheet != null) avatarSelectorBottomSheet.dismiss();
    }

    public boolean isAdvancedMode() { return advancedSwitch != null && advancedSwitch.isChecked(); }
    public String getCurrentPassword() { return oldPasswordEditText != null ? oldPasswordEditText.getText().toString().trim() : ""; }

    private void initializeEditBottomSheet() {
        editBottomSheet = new BottomSheetDialog(activity);
        View v = LayoutInflater.from(context).inflate(R.layout.bottomsheet_edit_account, null);
        ButtonAnimator.applyTo(v);
        editBottomSheet.setContentView(v);
        editBottomSheet.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        editBottomSheet.setCancelable(false);
        bindViews(v);
        setupStyling(v);
        setupLogic();
    }

    private void bindViews(View v) {
        firstNameEditText = v.findViewById(R.id.firstNameEditText); lastNameEditText = v.findViewById(R.id.lastNameEditText);
        firstNameTextInputLayout = v.findViewById(R.id.firstNameTextInputLayout); lastNameTextInputLayout = v.findViewById(R.id.lastNameTextInputLayout);
        usernameEditText = v.findViewById(R.id.registerUsernameEditText); oldPasswordEditText = v.findViewById(R.id.oldPasswordEditText);
        newPasswordEditText = v.findViewById(R.id.newPasswordEditText); confirmPasswordEditText = v.findViewById(R.id.confirmPasswordEditText);
        idNumberEditText = v.findViewById(R.id.idEditText); roleAutoComplete = v.findViewById(R.id.roleAutoComplete);
        usernameTextInputLayout = v.findViewById(R.id.registerUsernameTextInputLayout); oldPasswordTextInputLayout = v.findViewById(R.id.oldPasswordTextInputLayout);
        newPasswordTextInputLayout = v.findViewById(R.id.newPasswordTextInputLayout); confirmPasswordTextInputLayout = v.findViewById(R.id.confirmPasswordTextInputLayout);
        idNumberTextInputLayout = v.findViewById(R.id.idTextInputLayout); roleTextInputLayout = v.findViewById(R.id.roleTextInputLayout);
        studentLayout = v.findViewById(R.id.studentLayout); tutorLayout = v.findViewById(R.id.tutorLayout); teacherLayout = v.findViewById(R.id.teacherLayout);
        schoolEditText = v.findViewById(R.id.schoolEditText); groupEditText = v.findViewById(R.id.groupEditText);
        tuteeEditText = v.findViewById(R.id.tuteeEditText); responsibilitiesEditText = v.findViewById(R.id.responsibilitiesEditText);
        schoolTextInputLayout = v.findViewById(R.id.schoolTextInputLayout); groupTextInputLayout = v.findViewById(R.id.groupTextInputLayout);
        tuteeTextInputLayout = v.findViewById(R.id.tuteeTextInputLayout); responsibilitiesTextInputLayout = v.findViewById(R.id.responsibilitiesTextInputLayout);
        editAvatarImageView = v.findViewById(R.id.avatarImageView); advancedSwitch = v.findViewById(R.id.advancedMaterialSwitch);
        saveButton = v.findViewById(R.id.buttonSave); cancelButton = v.findViewById(R.id.buttonCancel);
        advancedFieldsLayout = v.findViewById(R.id.advancedLayout);
    }

    private void setupStyling(View v) {
        int cB = ThemeManager.getThemeColor(context, R.attr.colorBackground);
        int cBC = ThemeManager.getThemeColor(context, R.attr.colorBackgroundCard);
        int cH = ThemeManager.getColorControlHighlight(context);
        int d = (int) context.getResources().getDisplayMetrics().density;
        v.findViewById(R.id.editAccountContainer).setBackground(createRoundedBg(cB, d * 14));
        v.findViewById(R.id.actionsLinearLayout).setBackground(createRippleBg(cBC, cH, d * 14));
    }

    private void setupLogic() {
        List<String> roles = RoleHelper.getLocalizedRoles(context);
        roleAutoComplete.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, roles));
        roleAutoComplete.setOnItemClickListener((p, view, pos, id) -> {
            currentRoleValue = RoleHelper.getRoleValueFromLocalized(roles.get(pos), context);
            updateRoleFields(currentRoleValue);
            if (!isEditAvatarInitial) {
                pathBase64 = "{\"tipo\":\"rol\",\"rol\":\"" + currentRoleValue + "\"}";
                editAvatarImageView.setImageBitmap(AvatarHelper.generateRoleAvatar(context, currentRoleValue, ThemeManager.getColorAccent(context)));
            }
        });
        advancedSwitch.setOnCheckedChangeListener((btn, isChecked) -> {
            if (ignoreSwitchChange) { ignoreSwitchChange = false; return; }
            if (isChecked) showAdvancedWarning(); else advancedFieldsLayout.setVisibility(View.GONE);
        });
        editAvatarImageView.setOnClickListener(v -> showAvatarSelector());
        initializeAvatarSelector();
    }

    private void showAdvancedWarning() {
        DialogHelper.showConfirmDialog(activity, context.getString(R.string.profile_warning_title), context.getString(R.string.profile_warning_message),
            context.getString(R.string.profile_continue), context.getString(R.string.button_cancel), () -> advancedFieldsLayout.setVisibility(View.VISIBLE),
            () -> { ignoreSwitchChange = true; advancedSwitch.setChecked(false); advancedFieldsLayout.setVisibility(View.GONE); }).setCancelable(false);
    }

    private void fillData(Profile p) {
        firstNameEditText.setText(p.getFirstName());
        lastNameEditText.setText(p.getLastName());
        usernameEditText.setText(p.getUsername());
        idNumberEditText.setText(p.getIdCard());

        currentRoleValue = p.getRole() != null ? p.getRole() : RoleHelper.ROLE_STUDENT;
        roleAutoComplete.setText(RoleHelper.getLocalizedFromRoleValue(currentRoleValue, context), false);
        updateRoleFields(currentRoleValue);

        if (currentRoleValue.equals(RoleHelper.ROLE_STUDENT)) {
            schoolEditText.setText(p.getSchool()); groupEditText.setText(p.getGroup());
        } else if (currentRoleValue.equals(RoleHelper.ROLE_TUTOR)) tuteeEditText.setText(p.getTutee());
        else responsibilitiesEditText.setText(p.getResponsibilities());

        renderAvatar(p);
    }

    private void renderAvatar(Profile p) {
        String url = p.getAvatarUrl();
        isEditAvatarInitial = (url == null || url.isEmpty() || url.contains("\"tipo\":\"inicial\""));
        if (isEditAvatarInitial) updateEditInitialAvatar();
        else if (url.startsWith("{")) {
            try {
                JSONObject j = new JSONObject(url);
                if (j.getString("tipo").equals("rol")) editAvatarImageView.setImageBitmap(AvatarHelper.generateRoleAvatar(context, j.getString("rol"), ThemeManager.getColorAccent(context)));
            } catch (Exception ignored) {}
        } else Glide.with(context).load(url).into(editAvatarImageView);
    }

    public HashMap<String, Object> collectData() {
        if (!validateForm()) return null;
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("first_name", firstNameEditText.getText().toString().trim());
        data.put("last_name", lastNameEditText.getText().toString().trim());
        data.put("role", currentRoleValue);
        if (currentRoleValue.equals(RoleHelper.ROLE_STUDENT)) {
            data.put("school", schoolEditText.getText().toString().trim()); data.put("group", groupEditText.getText().toString().trim());
        } else if (currentRoleValue.equals(RoleHelper.ROLE_TUTOR)) data.put("tutee", tuteeEditText.getText().toString().trim());
        else data.put("responsibilities", responsibilitiesEditText.getText().toString().trim());
        if (advancedSwitch.isChecked()) {
            data.put("username", usernameEditText.getText().toString().trim());
            data.put("id_card", idNumberEditText.getText().toString().trim());
            String pass = newPasswordEditText.getText().toString().trim();
            if (!pass.isEmpty()) data.put("password", pass);
        }
        if (!pathBase64.isEmpty()) data.put("avatar_url", pathBase64);
        return data;
    }

    private boolean validateForm() {
        boolean ok = true;
        if (!Validator.isValidFirstName(firstNameEditText.getText().toString())) { firstNameTextInputLayout.setError(context.getString(R.string.profile_invalid_first_name)); ok = false; }
        if (!Validator.isValidLastName(lastNameEditText.getText().toString())) { lastNameTextInputLayout.setError(context.getString(R.string.profile_invalid_last_name)); ok = false; }
        return ok;
    }

    private void initializeAvatarSelector() {
        avatarSelectorBottomSheet = new BottomSheetDialog(activity);
        View v = LayoutInflater.from(context).inflate(R.layout.bottomsheet_avatar_selector, null);
        avatarSelectorBottomSheet.setContentView(v);
        avatarSelectorBottomSheet.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        v.findViewById(R.id.uploadImageLayout).setOnClickListener(view -> filePickerHelper.pickFile("image/jpeg", new FilePickerHelper.OnFilePickedListener() {
            @Override public void onFilePicked(String path, String name, String mime, String base64) {
                pathBase64 = base64; isEditAvatarInitial = false;
                editAvatarImageView.setImageBitmap(FileUtils.decodeSampleBitmapFromPath(path, 256, 256));
                avatarSelectorBottomSheet.dismiss();
            }
            @Override public void onPickCancelled() {}
        }));
        v.findViewById(R.id.initialLayout).setOnClickListener(view -> { isEditAvatarInitial = true; updateEditInitialAvatar(); avatarSelectorBottomSheet.dismiss(); });
        v.findViewById(R.id.roleLayout).setOnClickListener(view -> {
            isEditAvatarInitial = false; pathBase64 = "{\"tipo\":\"rol\",\"rol\":\"" + currentRoleValue + "\"}";
            editAvatarImageView.setImageBitmap(AvatarHelper.generateRoleAvatar(context, currentRoleValue, ThemeManager.getColorAccent(context)));
            avatarSelectorBottomSheet.dismiss();
        });
        v.findViewById(R.id.avatarSelectorContainer).setBackground(createRoundedBg(ThemeManager.getThemeColor(context, R.attr.colorBackground), (int)(14 * context.getResources().getDisplayMetrics().density)));
    }

    private void showAvatarSelector() { if (avatarSelectorBottomSheet != null) avatarSelectorBottomSheet.show(); }

    private void updateEditInitialAvatar() {
        if (!isEditAvatarInitial) return;
        String name = firstNameEditText.getText().toString().trim();
        String initial = name.isEmpty() ? usernameEditText.getText().toString().trim() : name;
        initial = initial.isEmpty() ? "?" : initial.substring(0, 1).toUpperCase();
        pathBase64 = "{\"tipo\":\"inicial\",\"inicial\":\"" + initial + "\"}";
        editAvatarImageView.setImageBitmap(AvatarHelper.generateInitialAvatar(context, initial, ThemeManager.getColorAccent(context)));
    }

    private void updateRoleFields(String val) {
        studentLayout.setVisibility(val.equals(RoleHelper.ROLE_STUDENT) ? View.VISIBLE : View.GONE);
        tutorLayout.setVisibility(val.equals(RoleHelper.ROLE_TUTOR) ? View.VISIBLE : View.GONE);
        teacherLayout.setVisibility(val.equals(RoleHelper.ROLE_TEACHER) ? View.VISIBLE : View.GONE);
    }

    private android.graphics.drawable.Drawable createRoundedBg(int c, float r) {
        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        gd.setColor(c); gd.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0}); return gd;
    }

    private android.graphics.drawable.Drawable createRippleBg(int c, int rip, float r) {
        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        gd.setColor(c); gd.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0});
        return new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(rip), gd, null);
    }
}


