/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: CreatePostRequest.java
 * Versión: v1.0.0
 * Descripción: Modelo de petición para crear una nueva publicación.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */
package binaryqva.educative.preusync.network.requests;

import com.google.gson.annotations.SerializedName;

public class CreatePostRequest {
    @SerializedName("title")
    private String title;

    @SerializedName("details")
    private String details;

    @SerializedName("imageBase64")
    private String imageBase64;

    @SerializedName("mimetype")
    private String mimetype;

    public CreatePostRequest(String title, String details) {
        this.title = title;
        this.details = details;
    }

    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
    public void setMimetype(String mimetype) { this.mimetype = mimetype; }
}


