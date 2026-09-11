/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: AboutActivity.java
 * Versión: v8.1.1
 * Descripción: Muestra información de la app, versión, créditos y comprobación de actualizaciones; acceso a consola de depuración.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.debug.AppLogger;
import binaryqva.educative.preusync.debug.LogConsoleActivity;
import binaryqva.educative.preusync.network.models.ApiResponse;
import binaryqva.educative.preusync.network.models.AppVersion;
import binaryqva.educative.preusync.network.retrofit.RetrofitClient;
import binaryqva.educative.preusync.utils.common.AppUtils;
import binaryqva.educative.preusync.utils.common.DialogHelper;
import binaryqva.educative.preusync.utils.theme.ThemeManager;

/**
 * Presenta los datos técnicos y de autoría. Implementa un Easter Egg para 
 * desarrolladores que habilita la consola de depuración interna.
 */
public class AboutActivity extends BaseActivity {
	
	private static final String TAG = "AboutActivity";
	
	private ImageButton backButton;
	private ImageButton donateButton;
	private TextView versionText;
	private MaterialButton checkUpdateButton;
	
	private String versionName;
	private long versionCode;
	
	private final Intent intent = new Intent();

	// Variables para el control del Easter Egg.
	private int clickCount = 0;
	private long lastClickTime = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_about);
		
		backButton = findViewById(R.id.backButton);
		donateButton = findViewById(R.id.donateButton);
		versionText = findViewById(R.id.versionText);
		checkUpdateButton = findViewById(R.id.updateCheckButton);
		
		initialize(savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle savedInstanceState) {
		backButton.setOnClickListener(v -> finish());
		
		donateButton.setOnClickListener(v -> {
			intent.setAction(Intent.ACTION_VIEW);
			intent.setClass(getApplicationContext(), DonateActivity.class);
			startActivity(intent);
		});
		
		// Obtención de la versión instalada mediante el PackageManager.
		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			versionName = packageInfo.versionName;
			versionCode = packageInfo.versionCode;
		} catch (Exception ignored) {
			versionName = "Desconocida";
		}
		versionText.setText(versionName + " (" + versionCode + ")");

		// EASTER EGG: Comentado para producción
		/*
		versionText.setOnClickListener(v -> {
			long currentTime = System.currentTimeMillis();
			if (currentTime - lastClickTime < 500) clickCount++;
			else clickCount = 1;
			lastClickTime = currentTime;

			if (clickCount == 5) {
				clickCount = 0;
				AppLogger.d(TAG, " Easter Egg activado: abriendo consola de logs");
				startActivity(new Intent(this, LogConsoleActivity.class));
			}
		});
		*/
		
		checkUpdateButton.setOnClickListener(v -> checkForUpdates());
	}
	
	private void initializeLogic() {
		int colorBackground = ThemeManager.getThemeColor(this, R.attr.colorBackground);
		getWindow().setNavigationBarColor(colorBackground);
	}
	
	/**
	 * Consulta a la API para verificar si existe una versión superior en el servidor.
	 */
	private void checkForUpdates() {
		AlertDialog progress = DialogHelper.showProgressDialog(this, getString(R.string.label_checking_updates), false);
		
		RetrofitClient.getApiService(this).getLatestVersion().enqueue(new retrofit2.Callback<ApiResponse<AppVersion>>() {
			@Override
			public void onResponse(retrofit2.Call<ApiResponse<AppVersion>> call, retrofit2.Response<ApiResponse<AppVersion>> response) {
				progress.dismiss();
				if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
					AppVersion data = response.body().getData();
					long serverCode = data.getVersionCode();
					String serverName = data.getVersionName();
					String details = data.getDetails();
					
					if (serverCode > versionCode) {
						showUpdateDialog(serverName, details);
					} else {
						Toast.makeText(AboutActivity.this, getString(R.string.toast_you_have_latest_version), Toast.LENGTH_SHORT).show();
					}
				} else AppUtils.showMessage(AboutActivity.this, getString(R.string.error_check_update_failed));
			}
			
			@Override
			public void onFailure(retrofit2.Call<ApiResponse<AppVersion>> call, Throwable t) {
				progress.dismiss();
				DialogHelper.showErrorDialog(AboutActivity.this, getString(R.string.error_update_check_connection));
			}
		});
	}

	private void showUpdateDialog(String name, String details) {
		DialogHelper.showConfirmDialog(
				this, getString(R.string.title_new_version),
				getString(R.string.message_new_version_details, name, details),
				getString(R.string.button_update), getString(R.string.button_cancel),
				() -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.update_download_url)))),
				null
		);
	}
}


