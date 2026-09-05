/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: DonateActivity.java
 * Versión: v1.0.1
 * Descripción: Pantalla de donaciones para el apoyo al desarrollo del proyecto.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.activities;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.utils.theme.ThemeManager;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.activity.EdgeToEdge;

/**
 * Pantalla informativa que muestra los canales oficiales de soporte económico.
 */
public class DonateActivity extends BaseActivity {

    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_donate);

        backButton = findViewById(R.id.backButton);

        initialize(savedInstanceState);
        initializeLogic();
    }

    private void initialize(Bundle savedInstanceState) {
        backButton.setOnClickListener(v -> finish());
    }

    private void initializeLogic() {
        int colorBackground = ThemeManager.getThemeColor(this, R.attr.colorBackground);
        getWindow().setNavigationBarColor(colorBackground);
    }
}


