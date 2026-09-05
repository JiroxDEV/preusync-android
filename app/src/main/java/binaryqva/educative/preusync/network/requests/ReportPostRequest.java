/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: ReportPostRequest.java
 * Versión: v1.0.0
 * Descripción: Modelo de petición para reportar una publicación.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */
package binaryqva.educative.preusync.network.requests;

import com.google.gson.annotations.SerializedName;

public class ReportPostRequest {
    @SerializedName("reason")
    private String reason;

    public ReportPostRequest(String reason) {
        this.reason = reason;
    }
}


