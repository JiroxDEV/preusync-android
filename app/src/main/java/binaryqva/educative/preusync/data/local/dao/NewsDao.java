/**
 * ============================================================================
 * Proyecto: PreuSync
 * Interfaz: NewsDao.java
 * Versión: v1.0.0
 * Descripción: DAO para la gestión de noticias en la base de datos local.
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

import binaryqva.educative.preusync.data.local.entities.NewsEntity;

@Dao
public interface NewsDao {
    @Query("SELECT * FROM news ORDER BY importance DESC, createdAt DESC")
    List<NewsEntity> getAllNews();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<NewsEntity> news);

    @Query("DELETE FROM news")
    void deleteAll();

    @Query("SELECT * FROM news WHERE id = :id LIMIT 1")
    NewsEntity getNewsById(String id);
}


