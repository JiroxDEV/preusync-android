/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: RoleHelper.java
 * Versión: v1.0.0
 * Descripción: Ayudante para la gestión de roles de usuario (Estudiante, etc).
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.common;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

import binaryqva.educative.preusync.R;

/**
 * Centraliza la lógica de identificación y visualización de roles.
 * Asegura que los valores técnicos se mantengan invariantes a la traducción.
 */
public class RoleHelper {

    // Identificadores técnicos de rol (siempre en inglés para la API).
    public static final String ROLE_STUDENT = "student";
    public static final String ROLE_TEACHER = "teacher";
    public static final String ROLE_TUTOR = "tutor";

    private static final String[] ROLE_VALUES = { ROLE_STUDENT, ROLE_TEACHER, ROLE_TUTOR };
    private static final int[] ROLE_STRING_RESOURCES = { R.string.role_student, R.string.role_teacher, R.string.role_tutor };

    /**
     * Recupera el listado de roles localizado para componentes de UI (spinners).
     */
    public static List<String> getLocalizedRoles(Context context) {
        List<String> list = new ArrayList<>();
        for (int res : ROLE_STRING_RESOURCES) list.add(context.getString(res));
        return list;
    }

    /**
     * Mapea un nombre localizado al valor técnico correspondiente.
     */
    public static String getRoleValueFromLocalized(String loc, Context ctx) {
        List<String> list = getLocalizedRoles(ctx);
        for (int i = 0; i < list.size(); i++) if (list.get(i).equals(loc)) return ROLE_VALUES[i];
        return ROLE_STUDENT;
    }

    /**
     * Mapea un valor técnico al nombre localizado.
     */
    public static String getLocalizedFromRoleValue(String val, Context ctx) {
        for (int i = 0; i < ROLE_VALUES.length; i++) if (ROLE_VALUES[i].equals(val)) return ctx.getString(ROLE_STRING_RESOURCES[i]);
        return ctx.getString(R.string.role_student);
    }

    /**
     * Obtiene el icono representativo para un rol determinado.
     */
    public static int getRoleIconResId(String val) {
        switch (val != null ? val : "") {
            case ROLE_STUDENT: return R.drawable.ic_school;
            case ROLE_TEACHER: return R.drawable.ic_work;
            case ROLE_TUTOR: return R.drawable.ic_person;
            default: return R.drawable.ic_nav_profile;
        }
    }
}


