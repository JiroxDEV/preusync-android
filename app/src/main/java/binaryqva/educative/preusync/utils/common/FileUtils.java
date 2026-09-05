/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: FileUtils.java
 * Versión: v1.0.2
 * Descripción: Utilidades para la gestión de archivos, directorios y procesamiento
 *              de imágenes (Bitmaps) con optimización de memoria.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Proporciona métodos robustos para operaciones de entrada/salida de archivos.
 */
public class FileUtils {

    /**
     * Crea un nuevo archivo asegurando la existencia de la ruta de directorios.
     */
    private static void createNewFile(String path) {
        int lastSep = path.lastIndexOf(File.separator);
        if (lastSep > 0) {
            String dirPath = path.substring(0, lastSep);
            makeDir(dirPath);
        }

        File file = new File(path);
        try {
            if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lee el contenido completo de un archivo de texto.
     */
    public static String readFile(String path) {
        createNewFile(path);
        StringBuilder sb = new StringBuilder();
        try (FileReader fr = new FileReader(new File(path))) {
            char[] buff = new char[1024];
            int length;
            while ((length = fr.read(buff)) > 0) {
                sb.append(new String(buff, 0, length));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * Escribe una cadena de texto en un archivo (sobrescribe contenido).
     */
    public static void writeFile(String path, String str) {
        createNewFile(path);
        try (FileWriter fileWriter = new FileWriter(new File(path), false)) {
            fileWriter.write(str);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Elimina un archivo o un directorio de forma recursiva.
     */
    public static void deleteFile(String path) {
        File file = new File(path);
        if (!file.exists()) return;
        if (file.isFile()) {
            file.delete();
            return;
        }
        File[] fileArr = file.listFiles();
        if (fileArr != null) {
            for (File subFile : fileArr) {
                deleteFile(subFile.getAbsolutePath());
            }
        }
        file.delete();
    }

    /**
     * Comprueba la existencia de una ruta en el sistema de archivos.
     */
    public static boolean isExistFile(String path) {
        return new File(path).exists();
    }

    /**
     * Crea un directorio si no existe previamente.
     */
    public static void makeDir(String path) {
        if (!isExistFile(path)) {
            new File(path).mkdirs();
        }
    }

    /**
     * Obtiene el directorio base de datos de la aplicación.
     */
    public static String getPackageDataDir(Context context) {
        String path = context.getExternalFilesDir(null).getAbsolutePath();
        return path != null ? path : "/sdcard/Android/data/binaryqva.educative.preusync/files";
    }

    // ==================== PROCESAMIENTO DE IMÁGENES ====================

    /**
     * Calcula el factor de escalado óptimo para cargar una imagen sin agotar la memoria.
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int width = options.outWidth;
        final int height = options.outHeight;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * Decodifica y escala un Bitmap desde una ruta de archivo.
     */
    public static Bitmap decodeSampleBitmapFromPath(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * Obtiene la rotación JPEG basándose en los metadatos EXIF.
     */
    public static int getJpegRotate(String filePath) {
        try {
            ExifInterface exif = new ExifInterface(filePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90: return 90;
                case ExifInterface.ORIENTATION_ROTATE_180: return 180;
                case ExifInterface.ORIENTATION_ROTATE_270: return 270;
                default: return 0;
            }
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * Genera un archivo temporal para capturas de cámara con timestamp.
     */
    public static File createNewPictureFile(Context context) {
        String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg";
        return new File(context.getExternalFilesDir(Environment.DIRECTORY_DCIM), fileName);
    }
}


