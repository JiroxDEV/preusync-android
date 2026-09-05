/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: VerifyPasswordRequest.java
 * Versión: v1.0.0
 * Descripción: Modelo de petición para verificar la contraseña del usuario.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */
package binaryqva.educative.preusync.network.requests;

import com.google.gson.annotations.SerializedName;

public class VerifyPasswordRequest {
    @SerializedName("password")
    private String password;

    public VerifyPasswordRequest(String password) {
        this.password = password;
    }
}


