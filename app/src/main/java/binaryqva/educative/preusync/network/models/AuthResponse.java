/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: AuthResponse.java
 * Versión: v1.3.2
 * Descripción: Contenedor para la respuesta de autenticación, incluyendo 
 *              datos de usuario, perfil y tokens de sesión.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */
package binaryqva.educative.preusync.network.models;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("user")
    private User user;

    @SerializedName("profile")
    private Profile profile;

    @SerializedName("sessionToken")
    private String sessionToken;

    @SerializedName("refreshToken")
    private String refreshToken;

    public User getUser() { return user; }
    public Profile getProfile() { return profile; }
    public String getSessionToken() { return sessionToken; }
    public String getRefreshToken() { return refreshToken; }
}


