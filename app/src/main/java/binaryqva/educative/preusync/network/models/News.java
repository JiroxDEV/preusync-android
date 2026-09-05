/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: News.java
 * Versión: v1.1.0
 * Descripción: Modelo de datos para las noticias institucionales y externas, 
 *              incluyendo niveles de importancia y fuentes.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */
package binaryqva.educative.preusync.network.models;

import com.google.gson.annotations.SerializedName;

/**
 * Representa una noticia o aviso en la plataforma.
 */
public class News {

    @SerializedName("id")
    private String id;

    @SerializedName("headline")
    private String headline;

    @SerializedName("details")
    private String details;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("source")
    private String source;

    @SerializedName("url")
    private String url;

    @SerializedName("importance")
    private int importance;

    @SerializedName("createdAt")
    private String createdAt;

    // Getters
    public String getId() { return id; }
    public String getHeadline() { return headline; }
    public String getDetails() { return details; }
    public String getImageUrl() { return imageUrl; }
    public String getSource() { return source; }
    public String getUrl() { return url; }
    public int getImportance() { return importance; }
    public String getCreatedAt() { return createdAt; }

    // ==================== MAPEADORES DE ENTIDAD (ROOM) ====================
    
    public News() {}

    public News(binaryqva.educative.preusync.data.local.entities.NewsEntity entity) {
        if (entity == null) return;
        this.id = entity.id;
        this.headline = entity.headline;
        this.details = entity.details;
        this.imageUrl = entity.imageUrl;
        this.source = entity.source;
        this.url = entity.url;
        this.importance = entity.importance;
        this.createdAt = entity.createdAt;
    }

    public binaryqva.educative.preusync.data.local.entities.NewsEntity toEntity() {
        return new binaryqva.educative.preusync.data.local.entities.NewsEntity(
            id, headline, details, imageUrl, source, url, importance, createdAt
        );
    }
}


