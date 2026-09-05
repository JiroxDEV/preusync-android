/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: Event.java
 * Versión: v2.0.0
 * Descripción: Modelo de datos genérico para eventos (Escolares y Externos).
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.network.models;

import com.google.gson.annotations.SerializedName;
import binaryqva.educative.preusync.data.local.entities.EventEntity;

/**
 * Representa una actividad o evento programado.
 */
public class Event {

    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("details")
    private String details;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("date")
    private String date;

    @SerializedName("time")
    private String time;

    @SerializedName("location")
    private String location;

    @SerializedName("eventType")
    private String eventType; // 'school' or 'external'

    @SerializedName("createdAt")
    private String createdAt;

    public Event() {}

    public Event(EventEntity entity) {
        if (entity == null) return;
        this.id = entity.id;
        this.title = entity.title;
        this.details = entity.details;
        this.imageUrl = entity.imageUrl;
        this.date = entity.date;
        this.time = entity.time;
        this.location = entity.location;
        this.eventType = entity.eventType;
        this.createdAt = entity.createdAt;
    }

    public EventEntity toEntity() {
        return new EventEntity(id, title, details, imageUrl, date, time, location, eventType, createdAt);
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDetails() { return details; }
    public String getImageUrl() { return imageUrl; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getLocation() { return location; }
    public String getEventType() { return eventType; }
    public String getCreatedAt() { return createdAt; }
}


