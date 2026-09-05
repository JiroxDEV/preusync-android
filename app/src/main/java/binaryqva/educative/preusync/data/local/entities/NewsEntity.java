/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: NewsEntity.java
 * Versión: v1.0.0
 * Descripción: Entidad de Room para la persistencia de noticias.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "news")
public class NewsEntity {
    @PrimaryKey
    @NonNull
    public String id;
    public String headline;
    public String details;
    public String imageUrl;
    public String source;
    public String url;
    public int importance;
    public String createdAt;

    public NewsEntity(@NonNull String id, String headline, String details, String imageUrl, String source, String url, int importance, String createdAt) {
        this.id = id; 
        this.headline = headline; 
        this.details = details; 
        this.imageUrl = imageUrl;
        this.source = source; 
        this.url = url; 
        this.importance = importance; 
        this.createdAt = createdAt;
    }
}


