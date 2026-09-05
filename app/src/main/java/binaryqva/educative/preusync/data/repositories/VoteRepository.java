/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: VoteRepository.java
 * Versión: v2.1.0
 * Descripción: Repositorio para la gestión persistente de reacciones (votos) 
 *              del usuario y contadores de publicaciones.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.data.repositories;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import binaryqva.educative.preusync.debug.AppLogger;

/**
 * Administra el estado de los votos del usuario y los contadores de publicaciones.
 * Mantiene la sincronización entre la memoria reactiva (LiveData) y el disco.
 */
public class VoteRepository {

    private static final String TAG = "PreuSync_VoteRepository";
    private static final String PREF = "vote_store";
    private static final String KEY_STATES = "v_states_";
    private static final String KEY_COUNTS = "v_counts_";

    private static VoteRepository instance;
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    private final MutableLiveData<Map<String, Integer>> voteStates = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<Map<String, PostCounts>> postCounts = new MutableLiveData<>(new HashMap<>());

    private String currentUserId;

    private VoteRepository(Context ctx) { this.prefs = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE); }

    public static synchronized VoteRepository getInstance(Context ctx) {
        if (instance == null) instance = new VoteRepository(ctx.getApplicationContext());
        return instance;
    }

    /**
     * Vincula el repositorio al identificador del usuario activo.
     */
    public void init(String uid) {
        if (uid == null || uid.equals(currentUserId)) return;
        this.currentUserId = uid;
        loadVotesFromPrefs();
        AppLogger.i(TAG, "Repositorio de votos vinculado al usuario: " + uid);
    }

    public void loadVotesFromPrefs() {
        if (currentUserId == null) return;
        
        String statesJson = prefs.getString(KEY_STATES + currentUserId, null);
        if (statesJson != null) try {
            Type type = new TypeToken<Map<String, Integer>>(){}.getType();
            voteStates.setValue(gson.fromJson(statesJson, type));
        } catch (Exception e) { AppLogger.e(TAG, "Fallo al cargar estados de disco", e); }

        String countsJson = prefs.getString(KEY_COUNTS + currentUserId, null);
        if (countsJson != null) try {
            Type type = new TypeToken<Map<String, PostCounts>>(){}.getType();
            postCounts.setValue(gson.fromJson(countsJson, type));
        } catch (Exception e) { AppLogger.e(TAG, "Fallo al cargar contadores de disco", e); }
    }

    public LiveData<Map<String, Integer>> getVoteStates() { return voteStates; }
    public LiveData<Map<String, PostCounts>> getPostCounts() { return postCounts; }

    public void setVoteState(String id, int s) {
        Map<String, Integer> map = new HashMap<>(voteStates.getValue());
        map.put(id, s); voteStates.setValue(map);
        if (currentUserId != null) prefs.edit().putString(KEY_STATES + currentUserId, gson.toJson(map)).apply();
    }

    public int getVoteState(String id) { return (voteStates.getValue() != null && voteStates.getValue().containsKey(id)) ? voteStates.getValue().get(id) : 0; }

    public void setPostCounts(String id, long l, long d) {
        Map<String, PostCounts> map = new HashMap<>(postCounts.getValue());
        map.put(id, new PostCounts(l, d)); postCounts.setValue(map);
        if (currentUserId != null) prefs.edit().putString(KEY_COUNTS + currentUserId, gson.toJson(map)).apply();
    }

    public void setPostCountsMap(Map<String, PostCounts> map) {
        postCounts.setValue(new HashMap<>(map));
        if (currentUserId != null) prefs.edit().putString(KEY_COUNTS + currentUserId, gson.toJson(map)).apply();
    }

    public void clear() {
        if (currentUserId != null) prefs.edit().remove(KEY_STATES + currentUserId).remove(KEY_COUNTS + currentUserId).apply();
        voteStates.setValue(new HashMap<>()); postCounts.setValue(new HashMap<>());
        currentUserId = null;
    }

    public static class PostCounts {
        public final long likes, dislikes;
        public PostCounts(long l, long d) { this.likes = l; this.dislikes = d; }
    }
}


