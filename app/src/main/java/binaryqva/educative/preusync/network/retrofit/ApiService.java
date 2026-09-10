/**
 * ============================================================================
 * Proyecto: PreuSync
 * Interfaz: ApiService.java
 * Versión: v2.0.0
 * Descripción: Interfaz de Retrofit que define los endpoints de la API REST. 
 *              Refactorizada para usar el modelo unificado Event.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */
package binaryqva.educative.preusync.network.retrofit;

import java.util.HashMap;
import java.util.List;

import binaryqva.educative.preusync.network.models.ApiResponse;
import binaryqva.educative.preusync.network.models.AppVersion;
import binaryqva.educative.preusync.network.models.AuthResponse;
import binaryqva.educative.preusync.network.models.Ephemeris;
import binaryqva.educative.preusync.network.models.News;
import binaryqva.educative.preusync.network.models.Post;
import binaryqva.educative.preusync.network.models.Profile;
import binaryqva.educative.preusync.network.models.Schedule;
import binaryqva.educative.preusync.network.models.Event;
import binaryqva.educative.preusync.network.models.Municipality;
import binaryqva.educative.preusync.network.models.Province;
import binaryqva.educative.preusync.network.models.School;
import binaryqva.educative.preusync.network.models.SchoolGroup;
import binaryqva.educative.preusync.network.requests.BugReportRequest;
import binaryqva.educative.preusync.network.requests.CreatePostRequest;
import binaryqva.educative.preusync.network.requests.LoginRequest;
import binaryqva.educative.preusync.network.requests.RefreshSessionRequest;
import binaryqva.educative.preusync.network.requests.ReportPostRequest;
import binaryqva.educative.preusync.network.requests.SignupRequest;
import binaryqva.educative.preusync.network.requests.UpdateProfileRequest;
import binaryqva.educative.preusync.network.requests.VerifyPasswordRequest;
import binaryqva.educative.preusync.network.requests.VotePostRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Interfaz de Retrofit que define los endpoints de la API de PreuSync.
 */
public interface ApiService {

    // ==================== VERSIÓN ====================
    @GET("version/latest")
    Call<ApiResponse<AppVersion>> getLatestVersion();

    // ==================== AUTENTICACIÓN ====================
    @POST("auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest body);

    @POST("auth/signup")
    Call<ApiResponse<AuthResponse>> signup(@Body SignupRequest body);

    @POST("auth/refresh")
    Call<ApiResponse<AuthResponse>> refreshSession(@Body RefreshSessionRequest body);

    @GET("auth/me")
    Call<ApiResponse<Profile>> getProfile();

    @PUT("auth/me")
    Call<ApiResponse<Profile>> updateProfile(@Body UpdateProfileRequest body);

    @POST("auth/verify-password")
    Call<ApiResponse<Boolean>> verifyPassword(@Body VerifyPasswordRequest body);

    @POST("auth/logout")
    Call<ApiResponse<Void>> logout();

    // ==================== PERFIL ====================
    @GET("profile/posts")
    Call<ApiResponse<List<Post>>> getUserPosts(@Query("start") int start, @Query("count") int count);

    @GET("profile/stats")
    Call<ApiResponse<HashMap<String, Object>>> getUserStats();

    @GET("auth/user/{username}")
    Call<ApiResponse<Profile>> getUserByUsername(@Path("username") String username);

    @GET("auth/exists/{username}")
    Call<ApiResponse<Boolean>> checkUserExists(@Path("username") String username);

    // ==================== PUBLICACIONES (POSTS) ====================
    @GET("posts/{id}")
    Call<ApiResponse<Post>> getPostById(@Path("id") String postId);

    @GET("posts/range")
    Call<ApiResponse<List<Post>>> getPostsRange(@Query("start") int start, @Query("count") int count);

    @GET("posts/top")
    Call<ApiResponse<List<Post>>> getTopPosts(@Query("limit") int limit);

    @POST("posts")
    Call<ApiResponse<Post>> createPost(@Body CreatePostRequest body);

    @DELETE("posts/{id}")
    Call<ApiResponse<Void>> deletePost(@Path("id") String postId);

    @POST("posts/{id}/vote")
    Call<ApiResponse<HashMap<String, Object>>> votePost(@Path("id") String postId, @Body VotePostRequest body);

    @POST("posts/{id}/report")
    Call<ApiResponse<Void>> reportPost(@Path("id") String postId, @Body ReportPostRequest body);

    // ==================== REPORTES DE ERRORES ====================
    @POST("reports")
    Call<ApiResponse<Void>> sendBugReport(@Body BugReportRequest body);

    // ==================== NOTICIAS ====================
    @GET("news/{id}")
    Call<ApiResponse<News>> getNewsById(@Path("id") String newsId);

    @GET("news/range")
    Call<ApiResponse<List<News>>> getNewsRange(@Query("start") int start, @Query("count") int count);

    @GET("news/top")
    Call<ApiResponse<List<News>>> getTopNews(@Query("limit") int limit);

    // ==================== EVENTOS ====================
    @GET("events/school")
    Call<ApiResponse<List<Event>>> getSchoolEvents(@Query("start") int start, @Query("count") int count, @Query("schoolId") String schoolId);

    @GET("events/external")
    Call<ApiResponse<List<Event>>> getExternalEvents(@Query("start") int start, @Query("count") int count, @Query("schoolId") String schoolId);

    @GET("events/{id}")
    Call<ApiResponse<Event>> getEventById(@Path("id") String eventId);

    // ==================== EFEMÉRIDES ====================
    @GET("ephemeris")
    Call<ApiResponse<List<Ephemeris>>> getEphemeris(@Query("date") String date);

    @GET("ephemeris/{id}")
    Call<ApiResponse<Ephemeris>> getEphemerisById(@Path("id") String ephemerisId);

    // ==================== HORARIO (SCHEDULE) ====================
    @GET("schedule")
    Call<ApiResponse<List<Schedule>>> getSchedule(@Query("group") String group, @Query("schoolId") String schoolId);

    @GET("groups")
    Call<ApiResponse<List<SchoolGroup>>> getGroups(@Query("schoolId") String schoolId);

    // ==================== LOCALIZACIONES (NACIONAL) ====================
    @GET("schools/provinces")
    Call<ApiResponse<List<Province>>> getProvinces();

    @GET("schools/municipalities")
    Call<ApiResponse<List<Municipality>>> getMunicipalities(@Query("provinceId") String provinceId);

    @GET("schools/institutions")
    Call<ApiResponse<List<School>>> getSchools(@Query("municipalityId") String municipalityId);

    @GET("schools/responsibilities")
    Call<ApiResponse<List<String>>> getResponsibilities();
}


