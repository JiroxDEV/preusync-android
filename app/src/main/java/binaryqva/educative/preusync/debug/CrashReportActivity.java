/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: CrashReportActivity.java
 * Versión: v1.6.0
 * Descripción: Interfaz de reporte de errores fatales. Permite al usuario
 *              enviar trazas de error (stacktraces) directamente al soporte.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.debug;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.HashMap;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.network.requests.BugReportRequest;
import binaryqva.educative.preusync.network.models.ApiResponse;
import binaryqva.educative.preusync.network.retrofit.RetrofitClient;
import binaryqva.educative.preusync.ui.activities.BaseActivity;
import binaryqva.educative.preusync.utils.common.AppUtils;
import binaryqva.educative.preusync.utils.common.DialogHelper;
import binaryqva.educative.preusync.utils.theme.ThemeManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Captura excepciones no controladas y presenta un diagnóstico al usuario.
 * Integra el envío de informes mediante el endpoint de reportes de la API.
 */
public class CrashReportActivity extends BaseActivity {

    private static final String TAG = "CrashReportActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_crash_report);

        getWindow().setNavigationBarColor(ThemeManager.getThemeColor(this, R.attr.colorBackground));

        // Ajuste de márgenes para evitar solapamiento con barras de sistema.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        bindLogic();
    }

    private void bindLogic() {
        TextView errorText = findViewById(R.id.errorText);
        EditText reportDescription = findViewById(R.id.reportDescriptionEditText);
        String stackTrace = getIntent().getStringExtra("error");
        errorText.setText(stackTrace != null ? stackTrace : "Información de error no disponible.");

        findViewById(R.id.sendReportButton).setOnClickListener(v -> {
            String desc = reportDescription.getText().toString().trim();
            if (desc.isEmpty()) { Toast.makeText(this, getString(R.string.hint_describe_error), Toast.LENGTH_SHORT).show(); return; }
            sendReportToBackend(desc, stackTrace);
        });

        findViewById(R.id.copyErrorButton).setOnClickListener(v -> {
            ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cb != null) { cb.setPrimaryClip(ClipData.newPlainText("Crash Log", errorText.getText().toString())); Toast.makeText(this, getString(R.string.toast_copied), Toast.LENGTH_SHORT).show(); }
        });

        findViewById(R.id.shareErrorButton).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, errorText.getText().toString());
            startActivity(Intent.createChooser(intent, "Compartir informe"));
        });

        findViewById(R.id.closeDebugButton).setOnClickListener(v -> finishAffinity());
    }

    /**
     * Transmite el diagnóstico técnico al backend para su seguimiento en GitHub.
     */
    private void sendReportToBackend(String description, String stackTrace) {
        AlertDialog progress = DialogHelper.showProgressDialog(this, "Transmitiendo reporte...", false);

        BugReportRequest request = new BugReportRequest(
            "Crash: " + Build.MODEL,
            (stackTrace != null ? stackTrace : "N/A") + "\n\nUser Description: " + description,
            "Modelo: " + Build.MODEL + "\nOS: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")\nApp: " + AppUtils.getAppVersionName(this)
        );

        RetrofitClient.getApiService(this).sendBugReport(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (progress != null) progress.dismiss();
                if (response.isSuccessful()) { Toast.makeText(CrashReportActivity.this, getString(R.string.toast_sent), Toast.LENGTH_LONG).show(); finishAffinity(); }
                else DialogHelper.showRetryDialog(CrashReportActivity.this, "Error de Servidor", "No se pudo procesar el reporte. ¿Reintentar?", () -> sendReportToBackend(description, stackTrace), null);
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                if (progress != null) progress.dismiss();
                DialogHelper.showRetryDialog(CrashReportActivity.this, "Error de Conexión", "Fallo de red al enviar reporte.", () -> sendReportToBackend(description, stackTrace), null);
            }
        });
    }
}


