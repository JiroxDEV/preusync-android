/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: BugReportRequest.java
 * Versión: v1.0.0
 * Descripción: Modelo de petición para enviar reportes de errores.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */
package binaryqva.educative.preusync.network.requests;

import com.google.gson.annotations.SerializedName;

public class BugReportRequest {
    @SerializedName("title")
    private String title;

    @SerializedName("error")
    private String error;

    @SerializedName("deviceInfo")
    private String deviceInfo;

    public BugReportRequest(String title, String error, String deviceInfo) {
        this.title = title;
        this.error = error;
        this.deviceInfo = deviceInfo;
    }
}


