/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: AppVersion.java
 * Versión: v1.0.2
 * Descripción: Modelo de datos que representa la información de versión de la 
 *              aplicación para la gestión de actualizaciones.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */
package binaryqva.educative.preusync.network.models;

import com.google.gson.annotations.SerializedName;

public class AppVersion {
    @SerializedName("version_code")
    private int versionCode;

    @SerializedName("version_name")
    private String versionName;

    @SerializedName("details")
    private String details;

    public int getVersionCode() { return versionCode; }
    public String getVersionName() { return versionName; }
    public String getDetails() { return details; }
}


