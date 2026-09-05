/**
 * ============================================================================
 * Proyecto: PreuSync
 * Interfaz: ScheduleDao.java
 * Versión: v1.0.0
 * Descripción: DAO para la gestión del horario escolar en la base de datos local.
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

import binaryqva.educative.preusync.data.local.entities.ScheduleEntity;

@Dao
public interface ScheduleDao {
    @Query("SELECT * FROM schedules WHERE groupName = :group ORDER BY shift ASC")
    List<ScheduleEntity> getScheduleByGroup(String group);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ScheduleEntity> schedules);

    @Query("DELETE FROM schedules WHERE groupName = :group")
    void deleteByGroup(String group);

    @Query("DELETE FROM schedules")
    void deleteAll();
}


