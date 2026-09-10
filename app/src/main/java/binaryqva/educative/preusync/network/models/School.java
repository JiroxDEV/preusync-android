/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: School.java
 * Versión: v1.0.0
 * Descripción: Modelo de datos para las instituciones educativas (Escuelas).
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.network.models;

import com.google.gson.annotations.SerializedName;

public class School {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("municipality_id")
    private String municipalityId;

    @SerializedName("is_available")
    private boolean isAvailable;

    public School(String id, String name, String municipalityId, boolean isAvailable) {
        this.id = id;
        this.name = name;
        this.municipalityId = municipalityId;
        this.isAvailable = isAvailable;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getMunicipalityId() { return municipalityId; }
    public boolean isAvailable() { return isAvailable; }

    @Override
    public String toString() { return name; }
}
