/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: RefreshSessionRequest.java
 * Versión: v1.0.0
 * Descripción: Modelo de petición para renovar el token de sesión.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */
package binaryqva.educative.preusync.network.requests;

import com.google.gson.annotations.SerializedName;

public class RefreshSessionRequest {
    @SerializedName("refreshToken")
    private String refreshToken;

    public RefreshSessionRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}


