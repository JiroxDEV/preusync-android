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

    @SerializedName("municipalityId")
    private String municipalityId;

    public School(String id, String name, String municipalityId) {
        this.id = id;
        this.name = name;
        this.municipalityId = municipalityId;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getMunicipalityId() { return municipalityId; }

    @Override
    public String toString() { return name; }
}
