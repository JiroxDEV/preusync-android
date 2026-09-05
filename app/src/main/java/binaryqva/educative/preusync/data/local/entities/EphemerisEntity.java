/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: EphemerisEntity.java
 * Versión: v1.0.0
 * Descripción: Entidad de Room para la persistencia de efemérides.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.data.local.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ephemerides")
public class EphemerisEntity {
    @PrimaryKey
    @NonNull
    public String id;
    public String title;
    public String details;
    public String imageUrl;
    public int day;
    public int month;
    public String date;
    public String year;
    public int importance;
    public String createdAt;

    public EphemerisEntity(@NonNull String id, String title, String details, String imageUrl, int day, int month, String date, String year, int importance, String createdAt) {
        this.id = id; this.title = title; this.details = details; this.imageUrl = imageUrl;
        this.day = day; this.month = month; this.date = date; this.year = year;
        this.importance = importance; this.createdAt = createdAt;
    }
}


