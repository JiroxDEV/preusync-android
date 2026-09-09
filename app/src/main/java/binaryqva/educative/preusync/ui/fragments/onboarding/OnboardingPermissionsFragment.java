package binaryqva.educative.preusync.ui.fragments.onboarding;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.ui.viewmodels.OnboardingViewModel;
import binaryqva.educative.preusync.utils.theme.ThemeManager;

public class OnboardingPermissionsFragment extends Fragment {

    private OnboardingViewModel viewModel;
    private RecyclerView permissionsRecyclerView;
    private final ArrayList<HashMap<String, Object>> permissionsList = new ArrayList<>();

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding_permissions, container, false);
        
        viewModel = new ViewModelProvider(requireActivity()).get(OnboardingViewModel.class);
        permissionsRecyclerView = view.findViewById(R.id.permissions);

        setupPermissionsList();
        return view;
    }

    private void setupPermissionsList() {
        permissionsList.clear();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            addPermission(getString(R.string.permission_battery_optimization_title), 
                         getString(R.string.permission_battery_optimization_desc), 
                         "IGNORE_BATTERY_OPTIMIZATIONS");
        }
        if (Build.VERSION.SDK_INT >= 33) {
            addPermission(getString(R.string.permission_notifications_title), 
                         getString(R.string.permission_notifications_desc), 
                         "POST_NOTIFICATIONS");
        }

        permissionsRecyclerView.setAdapter(new PermissionsAdapter(permissionsList));
        permissionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void addPermission(String name, String desc, String key) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("nombre", name); map.put("detalles", desc); map.put("permiso", key); map.put("permitido", false);
        permissionsList.add(map);
    }

    @Override
    public void onResume() { 
        super.onResume(); 
        refreshPermissions(); 
    }

    private void refreshPermissions() {
        for (HashMap<String, Object> item : permissionsList) updatePermissionStatus(requireContext(), item);
        if (permissionsRecyclerView.getAdapter() != null) permissionsRecyclerView.getAdapter().notifyDataSetChanged();
        checkPermissions();
    }

    private void updatePermissionStatus(Context ctx, HashMap<String, Object> item) {
        String key = (String) item.get("permiso");
        boolean ok = false;
        if ("IGNORE_BATTERY_OPTIMIZATIONS".equals(key)) {
            ok = ((PowerManager) ctx.getSystemService(Context.POWER_SERVICE)).isIgnoringBatteryOptimizations(ctx.getPackageName());
        } else if ("POST_NOTIFICATIONS".equals(key)) {
            ok = Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        item.put("permitido", ok);
    }

    private void checkPermissions() {
        boolean allRequired = true;
        for (HashMap<String, Object> p : permissionsList) {
            if (!"IGNORE_BATTERY_OPTIMIZATIONS".equals(p.get("permiso")) && !(Boolean) p.get("permitido")) { 
                allRequired = false; 
                break; 
            }
        }
        viewModel.setPermissionsGranted(allRequired);
    }

    private class PermissionsAdapter extends RecyclerView.Adapter<PermissionsAdapter.ViewHolder> {
        private final ArrayList<HashMap<String, Object>> data;
        PermissionsAdapter(ArrayList<HashMap<String, Object>> arr) { this.data = arr; }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup pr, int vt) {
            return new ViewHolder(LayoutInflater.from(pr.getContext()).inflate(R.layout.item_permission, pr, false));
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder h, int p) {
            HashMap<String, Object> item = data.get(p);
            h.name.setText((String) item.get("nombre"));
            h.desc.setText((String) item.get("detalles"));

            boolean granted = (Boolean) item.get("permitido");
            boolean required = "POST_NOTIFICATIONS".equals(item.get("permiso"));

            h.tagReq.setVisibility(required ? View.VISIBLE : View.GONE);
            h.tagOpt.setVisibility(!required ? View.VISIBLE : View.GONE);
            h.radio.setButtonTintList(ColorStateList.valueOf(ThemeManager.getThemeColor(requireContext(), granted ? R.attr.colorAccent : R.attr.colorError)));
            h.radio.setChecked(granted);

            if (!granted) h.card.setOnClickListener(v -> requestPermission((String) item.get("permiso")));
            else h.card.setOnClickListener(null);
        }

        private void requestPermission(String key) {
            if ("IGNORE_BATTERY_OPTIMIZATIONS".equals(key)) {
                startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).setData(Uri.parse("package:" + requireContext().getPackageName())));
            } else if ("POST_NOTIFICATIONS".equals(key)) {
                if (Build.VERSION.SDK_INT >= 33) requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1000);
            }
        }

        @Override public int getItemCount() { return data.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, desc, tagReq, tagOpt; RadioButton radio; MaterialCardView card;
            ViewHolder(View v) { super(v); name = v.findViewById(R.id.name); desc = v.findViewById(R.id.details); radio = v.findViewById(R.id.radioButton); tagReq = v.findViewById(R.id.tagRequired); tagOpt = v.findViewById(R.id.tagOptional); card = v.findViewById(R.id.cardView); }
        }
    }
}
