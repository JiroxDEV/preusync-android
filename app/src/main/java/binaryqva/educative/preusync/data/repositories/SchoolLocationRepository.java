/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: SchoolLocationRepository.java
 * Versión: v1.0.0
 * Descripción: Repositorio para la gestión de ubicaciones y escuelas a nivel nacional.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.data.repositories;

import android.content.Context;

import java.util.List;

import binaryqva.educative.preusync.network.models.ApiResponse;
import binaryqva.educative.preusync.network.models.Municipality;
import binaryqva.educative.preusync.network.models.Province;
import binaryqva.educative.preusync.network.models.School;
import binaryqva.educative.preusync.network.models.SchoolGroup;
import binaryqva.educative.preusync.network.retrofit.ApiService;
import binaryqva.educative.preusync.network.retrofit.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;

public class SchoolLocationRepository {

    private final ApiService apiService;

    public SchoolLocationRepository(Context context) {
        this.apiService = RetrofitClient.getApiService(context);
    }

    public void getProvinces(Callback<ApiResponse<List<Province>>> callback) {
        apiService.getProvinces().enqueue(callback);
    }

    public void getMunicipalities(String provinceId, Callback<ApiResponse<List<Municipality>>> callback) {
        apiService.getMunicipalities(provinceId).enqueue(callback);
    }

    public void getSchools(String municipalityId, Callback<ApiResponse<List<School>>> callback) {
        apiService.getSchools(municipalityId).enqueue(callback);
    }

    public void getGroups(String schoolId, Callback<ApiResponse<List<SchoolGroup>>> callback) {
        apiService.getGroups(schoolId).enqueue(callback);
    }

    public void getResponsibilities(Callback<ApiResponse<List<String>>> callback) {
        apiService.getResponsibilities().enqueue(callback);
    }
}
