/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: LoginRequest.java
 * Versión: v1.0.0
 * Descripción: Modelo de petición para el inicio de sesión.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */
package binaryqva.educative.preusync.network.requests;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    @SerializedName("username")
    private String username;

    @SerializedName("password")
    private String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}


