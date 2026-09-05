/**
 * ============================================================================
 * Proyecto: PreuSync
 * Interfaz: EphemerisDao.java
 * Versión: v1.0.0
 * Descripción: DAO para la gestión de efemérides en la base de datos local.
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

import binaryqva.educative.preusync.data.local.entities.EphemerisEntity;

@Dao
public interface EphemerisDao {
    @Query("SELECT * FROM ephemerides WHERE date = :date ORDER BY importance DESC")
    List<EphemerisEntity> getEphemerisByDate(String date);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<EphemerisEntity> ephemerides);

    @Query("DELETE FROM ephemerides")
    void deleteAll();
}


