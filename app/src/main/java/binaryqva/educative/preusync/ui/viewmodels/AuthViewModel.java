/**
 * ============================================================================
 * Proyecto: Preusync
 * Clase: AuthViewModel.java
 * Versión: v6.0.0
 * Descripción: ViewModel de Autenticación. Gestiona la lógica de registro
 *              jerárquico nacional y validación de sesión.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.HashMap;
import java.util.List;

import binaryqva.educative.preusync.data.repositories.AuthRepository;
import binaryqva.educative.preusync.data.repositories.SchoolLocationRepository;
import binaryqva.educative.preusync.network.models.ApiResponse;
import binaryqva.educative.preusync.network.models.AuthResponse;
import binaryqva.educative.preusync.network.models.Municipality;
import binaryqva.educative.preusync.network.models.Province;
import binaryqva.educative.preusync.network.models.School;
import binaryqva.educative.preusync.network.requests.SignupRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final SchoolLocationRepository locationRepository;

    private final MutableLiveData<List<Province>> provinces = new MutableLiveData<>();
    private final MutableLiveData<List<Municipality>> municipalities = new MutableLiveData<>();
    private final MutableLiveData<List<School>> schools = new MutableLiveData<>();

    private final MutableLiveData<ApiResponse<AuthResponse>> loginResult = new MutableLiveData<>();
    private final MutableLiveData<ApiResponse<AuthResponse>> signupResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLocationLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepository(application);
        this.locationRepository = new SchoolLocationRepository(application);
    }

    // Getters
    public LiveData<List<Province>> getProvinces() { return provinces; }
    public LiveData<List<Municipality>> getMunicipalities() { return municipalities; }
    public LiveData<List<School>> getSchools() { return schools; }
    public LiveData<ApiResponse<AuthResponse>> getLoginResult() { return loginResult; }
    public LiveData<ApiResponse<AuthResponse>> getSignupResult() { return signupResult; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getIsLocationLoading() { return isLocationLoading; }
    public LiveData<String> getError() { return error; }

    // --- Lógica de Ubicación ---

    public void loadProvinces() {
        isLocationLoading.setValue(true);
        locationRepository.getProvinces(new Callback<ApiResponse<List<Province>>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<List<Province>>> call, @NonNull Response<ApiResponse<List<Province>>> response) {
                isLocationLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) provinces.setValue(response.body().getData());
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<List<Province>>> call, @NonNull Throwable t) { isLocationLoading.setValue(false); error.setValue("Fallo al cargar provincias"); }
        });
    }

    public void loadMunicipalities(String provinceId) {
        isLocationLoading.setValue(true);
        locationRepository.getMunicipalities(provinceId, new Callback<ApiResponse<List<Municipality>>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<List<Municipality>>> call, @NonNull Response<ApiResponse<List<Municipality>>> response) {
                isLocationLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) municipalities.setValue(response.body().getData());
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<List<Municipality>>> call, @NonNull Throwable t) { isLocationLoading.setValue(false); error.setValue("Fallo al cargar municipios"); }
        });
    }

    public void loadSchools(String municipalityId) {
        isLocationLoading.setValue(true);
        locationRepository.getSchools(municipalityId, new Callback<ApiResponse<List<School>>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<List<School>>> call, @NonNull Response<ApiResponse<List<School>>> response) {
                isLocationLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) schools.setValue(response.body().getData());
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<List<School>>> call, @NonNull Throwable t) { isLocationLoading.setValue(false); error.setValue("Fallo al cargar escuelas"); }
        });
    }

    // --- Lógica de Auth ---

    public void login(String username, String password) {
        isLoading.setValue(true);
        authRepository.login(username, password, new Callback<ApiResponse<AuthResponse>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<AuthResponse>> call, @NonNull Response<ApiResponse<AuthResponse>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) loginResult.setValue(response.body());
                else error.setValue("Credenciales inválidas");
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<AuthResponse>> call, @NonNull Throwable t) { isLoading.setValue(false); error.setValue(t.getMessage()); }
        });
    }

    public void signup(String username, String password, String firstName, String lastName, String idCard, String role, HashMap<String, Object> extraData) {
        isLoading.setValue(true);
        SignupRequest request = new SignupRequest(username, password, firstName, lastName);
        request.setIdCard(idCard); request.setRole(role);
        
        if (extraData.containsKey("schoolId")) request.setSchoolId((String) extraData.get("schoolId"));
        if (extraData.containsKey("group")) request.setGroup((String) extraData.get("group"));
        if (extraData.containsKey("avatar")) request.setAvatar((String) extraData.get("avatar"));
        
        authRepository.signup(request, new Callback<ApiResponse<AuthResponse>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<AuthResponse>> call, @NonNull Response<ApiResponse<AuthResponse>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) signupResult.setValue(response.body());
                else error.setValue("Error en el registro");
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<AuthResponse>> call, @NonNull Throwable t) { isLoading.setValue(false); error.setValue(t.getMessage()); }
        });
    }
}
