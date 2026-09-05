/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: LogConsoleActivity.java
 * Versión: v1.6.1
 * Descripción: Consola de depuración en tiempo real. Permite visualizar,
 *              filtrar y exportar la bitácora interna de la aplicación.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.debug;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import java.util.List;
import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.ui.activities.BaseActivity;

/**
 * Herramienta de diagnóstico interna. 
 * Se accede mediante el Easter Egg en la pantalla "Acerca de".
 */
public class LogConsoleActivity extends BaseActivity {

    private LogAdapter adapter;
    private RecyclerView recyclerView;
    private TextInputEditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_log_console);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.logRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);

        setupLogs();
    }

    private void setupLogs() {
        List<AppLogger.LogEntry> logs = AppLogger.getLogs();
        adapter = new LogAdapter(logs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);

        // BUSCADOR EN TIEMPO REAL:
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { adapter.filter(s.toString()); }
        });

        // ACCIONES DE LA CONSOLA:
        findViewById(R.id.clearButton).setOnClickListener(v -> {
            AppLogger.clearLogs(); adapter.update(AppLogger.getLogs());
            Toast.makeText(this, getString(R.string.toast_logs_cleared), Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.copyButton).setOnClickListener(v -> exportLogsToClipboard());
    }

    /**
     * Concatena todos los registros actuales y los copia al portapapeles del sistema.
     */
    private void exportLogsToClipboard() {
        StringBuilder sb = new StringBuilder();
        for (AppLogger.LogEntry e : AppLogger.getLogs()) {
            sb.append(String.format("[%s/%s]: %s\n", e.level.name(), e.tag, e.message));
        }
        ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cb != null) {
            cb.setPrimaryClip(ClipData.newPlainText("App Logs", sb.toString()));
            Toast.makeText(this, getString(R.string.toast_logs_copied), Toast.LENGTH_SHORT).show();
        }
    }
}


