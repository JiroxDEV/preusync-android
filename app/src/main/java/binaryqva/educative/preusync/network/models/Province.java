/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: Province.java
 * Versión: v1.0.0
 * Descripción: Modelo de datos para las provincias del país.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.network.models;

import com.google.gson.annotations.SerializedName;

public class Province {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("is_available")
    private boolean isAvailable;

    public Province(String id, String name, boolean isAvailable) {
        this.id = id;
        this.name = name;
        this.isAvailable = isAvailable;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public boolean isAvailable() { return isAvailable; }
    
    @Override
    public String toString() { return name; }
}
