/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: Municipality.java
 * Versión: v1.0.0
 * Descripción: Modelo de datos para los municipios de una provincia.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.network.models;

import com.google.gson.annotations.SerializedName;

public class Municipality {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("province_id")
    private String provinceId;

    @SerializedName("is_available")
    private boolean isAvailable;

    public Municipality(String id, String name, String provinceId, boolean isAvailable) {
        this.id = id;
        this.name = name;
        this.provinceId = provinceId;
        this.isAvailable = isAvailable;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getProvinceId() { return provinceId; }
    public boolean isAvailable() { return isAvailable; }

    @Override
    public String toString() { return name; }
}
