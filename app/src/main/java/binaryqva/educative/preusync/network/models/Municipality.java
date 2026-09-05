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

    @SerializedName("provinceId")
    private String provinceId;

    public Municipality(String id, String name, String provinceId) {
        this.id = id;
        this.name = name;
        this.provinceId = provinceId;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getProvinceId() { return provinceId; }

    @Override
    public String toString() { return name; }
}
