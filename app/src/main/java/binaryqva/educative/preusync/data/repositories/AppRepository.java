/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: AppRepository.java
 * Versión: v1.1.0
 * Descripción: Repositorio para la gestión de datos globales de la aplicación, 
 *              como el control de versiones y el sistema de reportes técnicos.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */
package binaryqva.educative.preusync.data.repositories;

import android.content.Context;

import binaryqva.educative.preusync.network.models.ApiResponse;
import binaryqva.educative.preusync.network.models.AppVersion;
import binaryqva.educative.preusync.network.requests.BugReportRequest;
import binaryqva.educative.preusync.network.retrofit.ApiService;
import binaryqva.educative.preusync.network.retrofit.RetrofitClient;
import retrofit2.Callback;

/**
 * Gestiona utilidades transversales a toda la aplicación.
 */
public class AppRepository {
    
    private final ApiService apiService;

    public AppRepository(Context context) {
        this.apiService = RetrofitClient.getApiService(context);
    }

    /**
     * Obtiene la última versión disponible de la aplicación.
     */
    public void getLatestVersion(Callback<ApiResponse<AppVersion>> callback) {
        apiService.getLatestVersion().enqueue(callback);
    }

    /**
     * Envía un informe de error técnico al servidor.
     */
    public void sendBugReport(BugReportRequest request, Callback<ApiResponse<Void>> callback) {
        apiService.sendBugReport(request).enqueue(callback);
    }
}


