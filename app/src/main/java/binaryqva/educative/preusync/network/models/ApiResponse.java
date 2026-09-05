/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: ApiResponse.java
 * Versión: v1.5.1
 * Descripción: Modelo genérico para el tratamiento de respuestas estandarizadas 
 *              desde la API REST (Supabase).
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */
package binaryqva.educative.preusync.network.models;

import com.google.gson.annotations.SerializedName;

/**
 * Clase genérica para envolver las respuestas de la API.
 * @param <T> Tipo de dato esperado en el campo 'data'.
 */
public class ApiResponse<T> {
    
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("data")
    private T data;
    
    @SerializedName("error")
    private String error;
    
    @SerializedName("message")
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }
}


