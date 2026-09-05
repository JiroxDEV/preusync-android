/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: PostEntity.java
 * Versión: v1.0.0
 * Descripción: Entidad de Room para la persistencia de publicaciones de la comunidad.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "posts")
public class PostEntity {
    @PrimaryKey
    @NonNull
    public String id;
    public String title;
    public String details;
    public String imageUrl;
    public String author;
    public String authorAvatar;
    public long likes;
    public long dislikes;
    public int score;
    public String userVote;
    public String createdAt;

    public PostEntity(@NonNull String id, String title, String details, String imageUrl, String author, 
                      String authorAvatar, long likes, long dislikes, int score, String userVote, String createdAt) {
        this.id = id;
        this.title = title;
        this.details = details;
        this.imageUrl = imageUrl;
        this.author = author;
        this.authorAvatar = authorAvatar;
        this.likes = likes;
        this.dislikes = dislikes;
        this.score = score;
        this.userVote = userVote;
        this.createdAt = createdAt;
    }
}


