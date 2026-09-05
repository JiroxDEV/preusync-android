/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: Post.java
 * Versión: v1.1.0
 * Descripción: Modelo de datos para las publicaciones de la comunidad, 
 *              incluyendo métricas de interacción y detalles del autor.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */
package binaryqva.educative.preusync.network.models;

import com.google.gson.annotations.SerializedName;

/**
 * Representa una publicación en el feed de la comunidad.
 */
public class Post {

    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("details")
    private String details;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("author")
    private String author;

    @SerializedName("authorAvatar")
    private String authorAvatar;

    @SerializedName("likes")
    private long likes;

    @SerializedName("dislikes")
    private long dislikes;

    @SerializedName("score")
    private int score;

    @SerializedName("userVote")
    private String userVote; // 'up', 'down', 'none'

    @SerializedName("createdAt")
    private String createdAt;

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDetails() { return details; }
    public String getImageUrl() { return imageUrl; }
    public String getAuthor() { return author; }
    public String getAuthorAvatar() { return authorAvatar; }
    public long getLikes() { return likes; }
    public long getDislikes() { return dislikes; }
    public int getScore() { return score; }
    public String getUserVote() { return userVote; }
    public String getCreatedAt() { return createdAt; }

    // ==================== MAPEADORES DE ENTIDAD (ROOM) ====================

    public Post() {}

    public Post(binaryqva.educative.preusync.data.local.entities.PostEntity entity) {
        if (entity == null) return;
        this.id = entity.id;
        this.title = entity.title;
        this.details = entity.details;
        this.imageUrl = entity.imageUrl;
        this.author = entity.author;
        this.authorAvatar = entity.authorAvatar;
        this.likes = entity.likes;
        this.dislikes = entity.dislikes;
        this.score = entity.score;
        this.userVote = entity.userVote;
        this.createdAt = entity.createdAt;
    }

    public binaryqva.educative.preusync.data.local.entities.PostEntity toEntity() {
        return new binaryqva.educative.preusync.data.local.entities.PostEntity(
            id, title, details, imageUrl, author, authorAvatar, likes, dislikes, score, userVote, createdAt
        );
    }
}


