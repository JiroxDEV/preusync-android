/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: Ephemeris.java
 * Versión: v2.0.0
 * Descripción: Modelo de datos para efemérides con soporte Room.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.network.models;

import com.google.gson.annotations.SerializedName;
import binaryqva.educative.preusync.data.local.entities.EphemerisEntity;

public class Ephemeris {

    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("details")
    private String details;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("day")
    private int day;

    @SerializedName("month")
    private int month;

    @SerializedName("date")
    private String date;

    @SerializedName("year")
    private String year;

    @SerializedName("importance")
    private int importance;

    @SerializedName("createdAt")
    private String createdAt;

    public Ephemeris() {}

    public Ephemeris(EphemerisEntity entity) {
        if (entity == null) return;
        this.id = entity.id;
        this.title = entity.title;
        this.details = entity.details;
        this.imageUrl = entity.imageUrl;
        this.day = entity.day;
        this.month = entity.month;
        this.date = entity.date;
        this.year = entity.year;
        this.importance = entity.importance;
        this.createdAt = entity.createdAt;
    }

    public EphemerisEntity toEntity() {
        return new EphemerisEntity(id, title, details, imageUrl, day, month, date, year, importance, createdAt);
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDetails() { return details; }
    public String getImageUrl() { return imageUrl; }
    public int getDay() { return day; }
    public int getMonth() { return month; }
    public String getDate() { return date; }
    public String getYear() { return year; }
    public int getImportance() { return importance; }
    public String getCreatedAt() { return createdAt; }
}


