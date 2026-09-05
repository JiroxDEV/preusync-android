/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: UpdateProfileRequest.java
 * Versión: v1.0.0
 * Descripción: Modelo de petición para actualizar el perfil del usuario.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */
package binaryqva.educative.preusync.network.requests;

import com.google.gson.annotations.SerializedName;

public class UpdateProfileRequest {
    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("role")
    private String role;

    @SerializedName("school")
    private String school;

    @SerializedName("group")
    private String group;

    @SerializedName("tutee")
    private String tutee;

    @SerializedName("responsibilities")
    private String responsibilities;

    @SerializedName("username")
    private String username;

    @SerializedName("id_card")
    private String idCard;

    @SerializedName("password")
    private String password;

    @SerializedName("avatar_url")
    private String avatarUrl;

    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setRole(String role) { this.role = role; }
    public void setSchool(String school) { this.school = school; }
    public void setGroup(String group) { this.group = group; }
    public void setTutee(String tutee) { this.tutee = tutee; }
    public void setResponsibilities(String responsibilities) { this.responsibilities = responsibilities; }
    public void setUsername(String username) { this.username = username; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
    public void setPassword(String password) { this.password = password; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}


