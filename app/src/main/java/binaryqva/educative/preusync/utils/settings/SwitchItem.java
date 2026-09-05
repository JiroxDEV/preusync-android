/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: SwitchItem.java
 * Versión: v1.0.4
 * Descripción: Control de tipo interruptor (Encendido/Apagado) para ajustes.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.settings;

import java.util.function.Consumer;

/**
 * Modela una configuración booleana.
 */
public class SwitchItem implements SettingsItem {
	public final String title;
	public boolean currentValue;
	public final Consumer<Boolean> onCheckedChange;
	
	public SwitchItem(String title, boolean current, Consumer<Boolean> onCheckedChange) {
		this.title = title; this.currentValue = current; this.onCheckedChange = onCheckedChange;
	}
	
	public void setCurrentValue(boolean v) { this.currentValue = v; }
	
	@Override public int getType() { return SettingsItem.TYPE_SWITCH; }
}


