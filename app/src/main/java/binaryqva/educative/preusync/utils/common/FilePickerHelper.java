/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: FilePickerHelper.java
 * Versión: v1.1.0
 * Descripción: Ayudante para la selección de archivos y conversión a Base64.
 *              Soporta una amplia gama de tipos MIME y extensiones.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.common;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import androidx.fragment.app.Fragment;

import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import binaryqva.educative.preusync.R;

/**
 * Facilita la apertura del selector de documentos del sistema y procesa
 * el archivo seleccionado para su transmisión al backend.
 */
public class FilePickerHelper {

    public interface OnFilePickedListener {
        void onFilePicked(String filePath, String fileName, String mimeType, String base64Content);
        void onPickCancelled();
    }

    private final Activity activity;
    private final Fragment fragment;
    private OnFilePickedListener listener;

    // Mapa de extensiones personalizadas no cubiertas por el MimeTypeMap nativo.
    private static final Map<String, String> EXTRA_MIME_TYPES = new HashMap<>();

    static {
        EXTRA_MIME_TYPES.put("webp", "image/webp");
        EXTRA_MIME_TYPES.put("bmp", "image/bmp");
        EXTRA_MIME_TYPES.put("doc", "application/msword");
        EXTRA_MIME_TYPES.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        EXTRA_MIME_TYPES.put("xls", "application/vnd.ms-excel");
        EXTRA_MIME_TYPES.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        EXTRA_MIME_TYPES.put("json", "application/json");
        EXTRA_MIME_TYPES.put("csv", "text/csv");
        EXTRA_MIME_TYPES.put("mp3", "audio/mpeg");
        EXTRA_MIME_TYPES.put("mp4", "video/mp4");
        EXTRA_MIME_TYPES.put("apk", "application/vnd.android.package-archive");
    }

    public FilePickerHelper(Activity activity) { this.activity = activity; this.fragment = null; }
    public FilePickerHelper(Fragment fragment) { this.fragment = fragment; this.activity = fragment.getActivity(); }

    /**
     * Lanza el Intent para seleccionar un archivo filtrado por tipo MIME.
     */
    public void pickFile(String mimeType, OnFilePickedListener listener) {
        this.listener = listener;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType(mimeType);
        intent = Intent.createChooser(intent, activity != null ? activity.getString(R.string.button_select) : "Seleccionar archivo");
        if (fragment != null) fragment.startActivityForResult(intent, 100);
        else activity.startActivityForResult(intent, 100);
    }

    /**
     * Procesa la respuesta del selector de archivos en el onActivityResult.
     */
    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 100 || listener == null) return false;
        if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) { listener.onPickCancelled(); return true; }

        Uri uri = data.getData();
        String dataPath = "/data/data/" + activity.getPackageName();
        
        try (Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor == null) { listener.onPickCancelled(); return true; }

            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            cursor.moveToFirst();
            String fileName = cursor.getString(nameIndex);

            // Copiado del stream a un archivo temporal interno para acceso directo.
            File outFile = new File(dataPath, fileName);
            try (InputStream in = activity.getContentResolver().openInputStream(uri);
                 OutputStream out = new FileOutputStream(outFile)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) out.write(buffer, 0, len);
            }

            String filePath = outFile.getAbsolutePath();
            String base64 = readFileBase64(filePath);
            listener.onFilePicked(filePath, fileName, getMimeType(fileName), base64);
        } catch (IOException e) {
            e.printStackTrace();
            listener.onPickCancelled();
        }
        return true;
    }

    private String getMimeType(String fileName) {
        if (fileName == null || fileName.isEmpty()) return "application/octet-stream";
        String ext = getFileExtension(fileName).toLowerCase();
        if (ext.isEmpty()) return "application/octet-stream";
        if (EXTRA_MIME_TYPES.containsKey(ext)) return EXTRA_MIME_TYPES.get(ext);
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        return mime != null ? mime : "application/octet-stream";
    }

    private String getFileExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return (dot == -1 || dot == fileName.length() - 1) ? "" : fileName.substring(dot + 1);
    }

    private String readFileBase64(String path) {
        File file = new File(path);
        try (FileInputStream inFile = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            inFile.read(data);
            return new String(Base64.encodeBase64(data));
        } catch (IOException e) { return ""; }
    }
}


