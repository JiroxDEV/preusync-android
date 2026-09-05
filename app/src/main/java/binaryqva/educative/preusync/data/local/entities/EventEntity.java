/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: EventEntity.java
 * Versión: v1.0.0
 * Descripción: Entidad de Room para la persistencia de eventos (escolares y externos).
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "events")
public class EventEntity {
    @PrimaryKey
    @NonNull
    public String id;
    public String title;
    public String details;
    public String imageUrl;
    public String date;
    public String time;
    public String location;
    public String eventType;
    public String createdAt;

    public EventEntity(@NonNull String id, String title, String details, String imageUrl, String date, String time, String location, String eventType, String createdAt) {
        this.id = id; this.title = title; this.details = details; this.imageUrl = imageUrl;
        this.date = date; this.time = time; this.location = location;
        this.eventType = eventType; this.createdAt = createdAt;
    }
}


