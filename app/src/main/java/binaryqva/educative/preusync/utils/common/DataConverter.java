/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: DataConverter.java
 * Versión: v1.0.8
 * Descripción: Utilidad para la conversión y validación segura de tipos de datos.
 *              Previene excepciones de casteo mediante valores por defecto.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.common;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Proporciona métodos estáticos para transformar datos crudos de la API 
 * en tipos de Java de forma segura y consistente.
 */
public final class DataConverter {

    private DataConverter() { /* Previene instanciación */ }

    // ==================== PROCESAMIENTO DE IDENTIFICADORES (ID) ====================

    /**
     * Convierte un objeto ID (numérico o String/UUID) en una cadena limpia.
     */
    public static String parseId(Object idObj) {
        if (idObj == null) return "";
        if (idObj instanceof Number) {
            return String.valueOf(((Number) idObj).longValue());
        }
        return idObj.toString().trim();
    }

    /**
     * Parsea un objeto a long de forma segura.
     */
    public static long parseLong(Object obj, long defaultValue) {
        if (obj == null) return defaultValue;
        try {
            if (obj instanceof Number) return ((Number) obj).longValue();
            String str = obj.toString().trim();
            if (str.isEmpty()) return defaultValue;
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parsea un objeto a entero (int) con valor de respaldo.
     */
    public static int parseInt(Object obj, int defaultValue) {
        if (obj == null) return defaultValue;
        try {
            if (obj instanceof Number) return ((Number) obj).intValue();
            String str = obj.toString().trim();
            if (str.isEmpty()) return defaultValue;
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parsea un objeto a doble precisión (double).
     */
    public static double parseDouble(Object obj, double defaultValue) {
        if (obj == null) return defaultValue;
        try {
            if (obj instanceof Number) return ((Number) obj).doubleValue();
            String str = obj.toString().trim();
            if (str.isEmpty()) return defaultValue;
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Evalúa un objeto como booleano soportando múltiples formatos de entrada.
     * (true/false, 1/0, yes/no, on/off).
     */
    public static boolean parseBoolean(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Boolean) return (Boolean) obj;
        if (obj instanceof Number) return ((Number) obj).intValue() != 0;
        String str = obj.toString().trim().toLowerCase();
        return "true".equals(str) || "1".equals(str) || "yes".equals(str) || "on".equals(str);
    }

    // ==================== CONVERSIÓN DE CADENAS (STRING) ====================

    /**
     * Asegura que el objeto sea una cadena de texto no nula.
     */
    public static String toString(Object obj) {
        return obj == null ? "" : obj.toString().trim();
    }

    /**
     * Retorna el String del objeto o un valor por defecto si está vacío.
     */
    public static String toString(Object obj, String defaultValue) {
        String str = toString(obj);
        return str.isEmpty() ? defaultValue : str;
    }

    // ==================== UTILIDADES JSON ====================

    public static String getString(JSONObject json, String key) {
        try {
            return json.has(key) ? json.getString(key) : "";
        } catch (JSONException e) {
            return "";
        }
    }

    public static long getLong(JSONObject json, String key, long defaultValue) {
        try {
            return json.has(key) ? json.getLong(key) : defaultValue;
        } catch (JSONException e) {
            return defaultValue;
        }
    }

    public static int getInt(JSONObject json, String key, int defaultValue) {
        try {
            return json.has(key) ? json.getInt(key) : defaultValue;
        } catch (JSONException e) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(JSONObject json, String key, boolean defaultValue) {
        try {
            return json.has(key) ? json.getBoolean(key) : defaultValue;
        } catch (JSONException e) {
            return defaultValue;
        }
    }

    /**
     * Transforma un JSONArray en una lista tipada de Strings.
     */
    public static List<String> jsonArrayToStringList(JSONArray array) {
        List<String> list = new ArrayList<>();
        if (array == null) return list;
        for (int i = 0; i < array.length(); i++) {
            try {
                list.add(array.getString(i));
            } catch (JSONException ignored) {}
        }
        return list;
    }

    /**
     * Mapea de forma recursiva un JSONObject a un HashMap de Java.
     */
    public static HashMap<String, Object> jsonObjectToMap(JSONObject json) {
        HashMap<String, Object> map = new HashMap<>();
        if (json == null) return map;
        try {
            JSONArray keys = json.names();
            if (keys == null) return map;
            for (int i = 0; i < keys.length(); i++) {
                String key = keys.getString(i);
                Object value = json.get(key);
                if (value instanceof JSONObject) {
                    map.put(key, jsonObjectToMap((JSONObject) value));
                } else if (value instanceof JSONArray) {
                    map.put(key, jsonArrayToList((JSONArray) value));
                } else {
                    map.put(key, value);
                }
            }
        } catch (JSONException ignored) { }
        return map;
    }

    /**
     * Convierte un JSONArray en una lista genérica de objetos.
     */
    public static List<Object> jsonArrayToList(JSONArray array) {
        List<Object> list = new ArrayList<>();
        if (array == null) return list;
        for (int i = 0; i < array.length(); i++) {
            try {
                Object value = array.get(i);
                if (value instanceof JSONObject) {
                    list.add(jsonObjectToMap((JSONObject) value));
                } else if (value instanceof JSONArray) {
                    list.add(jsonArrayToList((JSONArray) value));
                } else {
                    list.add(value);
                }
            } catch (JSONException ignored) { }
        }
        return list;
    }

    // ==================== COMPATIBILIDAD HEREDADA (Deprecated) ====================

    /**
     * @deprecated Use {@link #parseId(Object)}.
     */
    @Deprecated
    public static long parsePostId(Object idObj) {
        return parseLong(idObj, 0L);
    }

    /**
     * @deprecated Use {@link #toString(Object)}.
     */
    @Deprecated
    public static String objectToString(Object obj) {
        return toString(obj);
    }
}


