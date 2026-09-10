/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: SchoolGroup.java
 * Versión: v1.0.0
 * Descripción: Modelo de datos para los grupos escolares dentro de una escuela.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.network.models;

import com.google.gson.annotations.SerializedName;

public class SchoolGroup {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("school_id")
    private String schoolId;

    @SerializedName("is_available")
    private boolean isAvailable;

    public SchoolGroup(String id, String name, String schoolId, boolean isAvailable) {
        this.id = id;
        this.name = name;
        this.schoolId = schoolId;
        this.isAvailable = isAvailable;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getSchoolId() { return schoolId; }
    public boolean isAvailable() { return isAvailable; }

    @Override
    public String toString() { return name; }
}
