/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: SchoolEventsViewModel.java
 * Versión: v6.0.0
 * Descripción: ViewModel encargado de gestionar los eventos escolares. 
 *              Implementa el patrón BaseViewModel y soporte híbrido Room/API.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import binaryqva.educative.preusync.data.repositories.EventsRepository;
import binaryqva.educative.preusync.network.models.ApiResponse;
import binaryqva.educative.preusync.network.models.Event;
import binaryqva.educative.preusync.utils.common.AppUtils;
import binaryqva.educative.preusync.utils.common.PaginationState;
import binaryqva.educative.preusync.utils.common.PreferenceManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SchoolEventsViewModel extends BaseViewModel<Event> {

    private static final int PAGE_SIZE = 10;
    private final EventsRepository repository;
    private int currentPage = 0;
    private boolean isLoadingMore = false;

    public SchoolEventsViewModel(@NonNull Application application) {
        super(application);
        this.repository = new EventsRepository(application);
    }

    @Override
    public void refresh() {
        currentPage = 0;
        isLoadingMore = false;
        paginationState.setValue(PaginationState.loading());
        loadPage(0, true);
    }

    public void loadMore() {
        PaginationState<Event> current = paginationState.getValue();
        if (current == null || isLoadingMore || !current.hasMore || current.state == PaginationState.STATE_ERROR) return;
        isLoadingMore = true;
        loadPage(currentPage, false);
    }

    private void loadPage(int page, boolean isRefresh) {
        if (!AppUtils.isConnected(getApplication())) {
            if (isRefresh) {
                repository.getLocalEvents("school", localData -> {
                    if (localData != null && !localData.isEmpty()) {
                        paginationState.setValue(PaginationState.content(localData, false));
                    } else {
                        paginationState.setValue(PaginationState.error("Sin conexión y sin datos locales"));
                    }
                });
            }
            isLoadingMore = false;
            return;
        }

        int start = page * PAGE_SIZE;
        String schoolId = PreferenceManager.getInstance(getApplication()).getSupabaseUserId(); // O el ID de escuela guardado en el perfil
        // TODO: Asegurarse de que el perfil guardado incluya el schoolId. Por ahora usaremos el del PreferenceManager si se guardó.
        // Asumiendo que guardamos el schoolId en el perfil del usuario al loguear.
        
        repository.getSchoolEvents(start, PAGE_SIZE, schoolId, new Callback<ApiResponse<List<Event>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Event>>> call, @NonNull Response<ApiResponse<List<Event>>> response) {
                isLoadingMore = false;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    updateState(response.body().getData(), isRefresh);
                } else {
                    paginationState.setValue(PaginationState.error("Error del servidor"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Event>>> call, @NonNull Throwable t) {
                isLoadingMore = false;
                if (isRefresh) {
                    repository.getLocalEvents("school", localData -> {
                        if (localData != null && !localData.isEmpty()) {
                            paginationState.setValue(PaginationState.content(localData, false));
                        } else {
                            paginationState.setValue(PaginationState.error(t.getMessage()));
                        }
                    });
                }
            }
        });
    }

    private void updateState(List<Event> newItems, boolean isRefresh) {
        PaginationState<Event> current = paginationState.getValue();
        List<Event> allItems = (isRefresh || current == null) ? new ArrayList<>() : new ArrayList<>(current.items);
        allItems.addAll(newItems);
        if (isRefresh) currentPage = 1; else currentPage++;
        if (allItems.isEmpty()) paginationState.setValue(PaginationState.empty());
        else paginationState.setValue(PaginationState.content(allItems, newItems.size() >= PAGE_SIZE));
    }
}


