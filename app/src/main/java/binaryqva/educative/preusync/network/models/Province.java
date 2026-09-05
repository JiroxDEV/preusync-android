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

    public Province(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    
    @Override
    public String toString() { return name; }
}
