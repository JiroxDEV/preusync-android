/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: VotePostRequest.java
 * Versión: v1.0.0
 * Descripción: Modelo de petición para registrar un voto en una publicación.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */
package binaryqva.educative.preusync.network.requests;

import com.google.gson.annotations.SerializedName;

public class VotePostRequest {
    @SerializedName("voteType")
    private String voteType; // 'like', 'dislike', 'none'

    public VotePostRequest(String voteType) {
        this.voteType = voteType;
    }
}


