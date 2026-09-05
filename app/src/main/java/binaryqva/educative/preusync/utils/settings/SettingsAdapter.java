/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: SettingsAdapter.java
 * Versión: v2.1.0
 * Descripción: Adaptador dinámico para las pantallas de ajustes. Gestiona
 *              múltiples tipos de controles (Switch, Selección, Botón).
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.settings;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.List;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.ui.activities.ServicesSettingsActivity;
import binaryqva.educative.preusync.utils.common.DialogHelper;
import binaryqva.educative.preusync.utils.ui.DividerItemDecoration;

/**
 * Motor de renderizado para los menús de configuración. 
 * Soporta la anidación de grupos de secciones y actualización reactiva de estados.
 */
public class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	
	private final List<SettingsItem> items;
	private LayoutInflater inflater;
	
	public SettingsAdapter(List<SettingsItem> items) { this.items = items; }
	public List<SettingsItem> getItems() { return items; }
	
	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		if (inflater == null) inflater = LayoutInflater.from(parent.getContext());
		switch (viewType) {
			case SettingsItem.TYPE_HEADER: return new HeaderViewHolder(inflater.inflate(R.layout.item_settings_header, parent, false));
			case SettingsItem.TYPE_SWITCH: return new SwitchViewHolder(inflater.inflate(R.layout.item_settings_switch, parent, false));
			case SettingsItem.TYPE_SELECT: return new SelectViewHolder(inflater.inflate(R.layout.item_settings_select, parent, false));
			case SettingsItem.TYPE_BUTTON: return new ButtonViewHolder(inflater.inflate(R.layout.item_settings_button, parent, false));
			case SettingsItem.TYPE_INFO: return new InfoViewHolder(inflater.inflate(R.layout.item_settings_info, parent, false));
			case SettingsItem.TYPE_NAVIGATION: return new NavigationViewHolder(inflater.inflate(R.layout.item_settings_navigation, parent, false));
			case SettingsItem.TYPE_SECTION_GROUP: return new SectionViewHolder(inflater.inflate(R.layout.item_settings_section, parent, false));
			default: return new InfoViewHolder(inflater.inflate(R.layout.item_settings_info, parent, false));
		}
	}
	
	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		SettingsItem item = items.get(position);
		if (holder instanceof HeaderViewHolder) bindHeader((HeaderViewHolder) holder, (HeaderItem) item);
		else if (holder instanceof SwitchViewHolder) bindSwitch((SwitchViewHolder) holder, (SwitchItem) item);
		else if (holder instanceof SelectViewHolder) bindSelect((SelectViewHolder) holder, (SelectItem) item);
		else if (holder instanceof ButtonViewHolder) bindButton((ButtonViewHolder) holder, (ButtonItem) item);
		else if (holder instanceof InfoViewHolder) ((InfoViewHolder) holder).infoText.setText(((InfoItem) item).text);
		else if (holder instanceof NavigationViewHolder) bindNav((NavigationViewHolder) holder, (NavigationItem) item);
		else if (holder instanceof SectionViewHolder) bindSection((SectionViewHolder) holder, (SectionGroup) item);
	}

    private void bindHeader(HeaderViewHolder h, HeaderItem i) {
        h.title.setText(i.title);
        h.subtitle.setText(i.subtitle);
        h.subtitle.setVisibility(i.subtitle != null && !i.subtitle.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void bindSwitch(SwitchViewHolder h, SwitchItem i) {
        h.title.setText(i.title);
        h.switchWidget.setOnCheckedChangeListener(null);
        h.switchWidget.setChecked(i.currentValue);
        h.switchWidget.setOnCheckedChangeListener((btn, checked) -> { i.setCurrentValue(checked); if (i.onCheckedChange != null) i.onCheckedChange.accept(checked); });
        h.itemView.setOnClickListener(v -> h.switchWidget.toggle());
    }

    private void bindSelect(SelectViewHolder h, SelectItem i) {
        h.title.setText(i.title);
        String label = i.currentValue;
        for (SelectItem.SelectOption o : i.options) if (o.value.equals(i.currentValue)) { label = o.label; break; }
        h.value.setText(label);
        h.itemView.setOnClickListener(v -> showSelectDialog(v.getContext(), i, h));
    }

    private void showSelectDialog(Context ctx, SelectItem item, SelectViewHolder h) {
        List<DialogHelper.RadioOption> opts = new ArrayList<>();
        for (SelectItem.SelectOption o : item.options) opts.add(new DialogHelper.RadioOption(o.label, o.value));

        Activity act = (Activity) ctx;
        Runnable cancel = (act instanceof ServicesSettingsActivity) ? () -> ((ServicesSettingsActivity) act).stopPreviewSound() : null;

        DialogHelper.showRadioDialogWithPreview(act, item.title, opts, item.currentValue,
            val -> {
                if (!val.equals(item.currentValue)) {
                    item.setCurrentValue(val);
                    for (SelectItem.SelectOption o : item.options) if (o.value.equals(val)) { h.value.setText(o.label); break; }
                    if (item.onSelected != null) item.onSelected.accept(val);
                }
            },
            v -> { if (act instanceof ServicesSettingsActivity) ((ServicesSettingsActivity) act).playPreviewSound(v); }, cancel);
    }

    private void bindButton(ButtonViewHolder h, ButtonItem i) { h.title.setText(i.title); h.itemView.setOnClickListener(i.onClick); }
    private void bindNav(NavigationViewHolder h, NavigationItem i) { h.icon.setImageResource(i.iconRes); h.title.setText(i.title); h.itemView.setOnClickListener(i.onClick); }

    private void bindSection(SectionViewHolder h, SectionGroup i) {
        h.innerRecyclerView.setLayoutManager(new LinearLayoutManager(h.itemView.getContext()));
        h.innerRecyclerView.setAdapter(i.innerAdapter);
        if (h.innerRecyclerView.getItemDecorationCount() == 0) {
            h.innerRecyclerView.addItemDecoration(new DividerItemDecoration(h.itemView.getContext(), androidx.appcompat.R.attr.colorControlHighlight, 16, 16));
        }
    }
	
	@Override public int getItemCount() { return items.size(); }
	@Override public int getItemViewType(int pos) { return items.get(pos).getType(); }
	
	// ==================== VIEW HOLDERS ====================
	
	static class HeaderViewHolder extends RecyclerView.ViewHolder {
		TextView title, subtitle;
		HeaderViewHolder(View v) { super(v); title = v.findViewById(R.id.sectionTitle); subtitle = v.findViewById(R.id.sectionSubtitle); }
	}
	
	static class SwitchViewHolder extends RecyclerView.ViewHolder {
		TextView title; MaterialSwitch switchWidget;
		SwitchViewHolder(View v) { super(v); title = v.findViewById(R.id.title); switchWidget = v.findViewById(R.id.switchWidget); }
	}
	
	static class SelectViewHolder extends RecyclerView.ViewHolder {
		TextView title, value;
		SelectViewHolder(View v) { super(v); title = v.findViewById(R.id.title); value = v.findViewById(R.id.value); }
	}
	
	static class ButtonViewHolder extends RecyclerView.ViewHolder {
		TextView title; ButtonViewHolder(View v) { super(v); title = v.findViewById(R.id.title); }
	}
	
	static class InfoViewHolder extends RecyclerView.ViewHolder {
		TextView infoText; InfoViewHolder(View v) { super(v); infoText = v.findViewById(R.id.infoText); }
	}
	
	static class NavigationViewHolder extends RecyclerView.ViewHolder {
		ImageView icon; TextView title;
		NavigationViewHolder(View v) { super(v); icon = v.findViewById(R.id.icon); title = v.findViewById(R.id.title); }
	}
	
	static class SectionViewHolder extends RecyclerView.ViewHolder {
		RecyclerView innerRecyclerView; SectionViewHolder(View v) { super(v); innerRecyclerView = v.findViewById(R.id.innerRecyclerView); }
	}
}


