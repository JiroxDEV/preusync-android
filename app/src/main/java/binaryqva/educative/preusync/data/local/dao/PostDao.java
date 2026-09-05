/**
 * ============================================================================
 * Proyecto: PreuSync
 * Interfaz: PostDao.java
 * Versión: v1.0.0
 * Descripción: DAO para la gestión de posts en la base de datos local.
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

import binaryqva.educative.preusync.data.local.entities.PostEntity;

@Dao
public interface PostDao {
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    List<PostEntity> getAllPosts();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PostEntity> posts);

    @Query("DELETE FROM posts")
    void deleteAll();

    @Query("SELECT * FROM posts WHERE id = :id LIMIT 1")
    PostEntity getPostById(String id);
}


