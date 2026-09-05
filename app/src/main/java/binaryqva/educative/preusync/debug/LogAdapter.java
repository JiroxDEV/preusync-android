/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: LogAdapter.java
 * Versión: v1.2.0
 * Descripción: Adaptador para la visualización de logs en la consola interna.
 *              Aplica colores según el nivel de severidad del log.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.debug;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import binaryqva.educative.preusync.R;

/**
 * Gestiona el listado dinámico de logs internos.
 */
public class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {

    private final List<AppLogger.LogEntry> allLogs;
    private final List<AppLogger.LogEntry> filteredLogs;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());

    public LogAdapter(List<AppLogger.LogEntry> logs) {
        this.allLogs = logs;
        this.filteredLogs = new ArrayList<>(logs);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppLogger.LogEntry entry = filteredLogs.get(position);
        String time = dateFormat.format(new Date(entry.timestamp));
        holder.logText.setText(String.format("[%s] [%s/%s]: %s", time, entry.level.name(), entry.tag, entry.message));
        
        // CODIFICACIÓN DE COLORES POR SEVERIDAD:
        int color;
        switch (entry.level) {
            case ERROR: color = Color.parseColor("#EF5350"); break; // Rojo
            case WARN:  color = Color.parseColor("#FFCA28"); break; // Ámbar
            case INFO:  color = Color.parseColor("#66BB6A"); break; // Verde
            default:    color = Color.GRAY; break;
        }
        holder.logText.setTextColor(color);
    }

    @Override
    public int getItemCount() { return filteredLogs.size(); }

    /**
     * Filtra los logs mostrados basándose en una cadena de texto (búsqueda).
     */
    public void filter(String query) {
        filteredLogs.clear();
        if (query.isEmpty()) filteredLogs.addAll(allLogs);
        else {
            String q = query.toLowerCase();
            for (AppLogger.LogEntry e : allLogs) {
                if (e.message.toLowerCase().contains(q) || e.tag.toLowerCase().contains(q)) filteredLogs.add(e);
            }
        }
        notifyDataSetChanged();
    }

    public void update(List<AppLogger.LogEntry> newLogs) {
        this.allLogs.clear(); this.allLogs.addAll(newLogs);
        this.filteredLogs.clear(); this.filteredLogs.addAll(allLogs);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView logText;
        ViewHolder(View view) { super(view); logText = view.findViewById(R.id.logText); }
    }
}


