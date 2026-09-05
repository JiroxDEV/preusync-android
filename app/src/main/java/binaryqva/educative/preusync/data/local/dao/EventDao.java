/**
 * ============================================================================
 * Proyecto: PreuSync
 * Interfaz: EventDao.java
 * Versión: v1.0.0
 * Descripción: DAO para la gestión de eventos en la base de datos local.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import binaryqva.educative.preusync.data.local.entities.EventEntity;

@Dao
public interface EventDao {
    @Query("SELECT * FROM events WHERE eventType = :type ORDER BY date ASC, time ASC")
    List<EventEntity> getEventsByType(String type);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<EventEntity> events);

    @Query("DELETE FROM events WHERE eventType = :type")
    void deleteByType(String type);

    @Query("DELETE FROM events")
    void deleteAll();
}


