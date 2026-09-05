/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: User.java
 * Versión: v1.0.0
 * Descripción: Modelo de datos para la entidad de usuario del sistema de 
 *              autenticación (Supabase Auth).
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */
package binaryqva.educative.preusync.network.models;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private String id;

    @SerializedName("email")
    private String email;

    @SerializedName("aud")
    private String aud;

    @SerializedName("role")
    private String role;

    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getAud() { return aud; }
    public String getRole() { return role; }
}


