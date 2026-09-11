/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: LogAdapter.java
 * Versión: v1.3.0
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
import java.util.ArrayList;
import java.util.List;
import binaryqva.educative.preusync.R;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {

    private final List<AppLogger.LogEntry> allLogs;
    private final List<AppLogger.LogEntry> filteredLogs;

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
        holder.logText.setText(String.format("[%s] [%s/%s]: %s", entry.timestamp, entry.level.name(), entry.tag, entry.message));
        
        int color;
        switch (entry.level) {
            case ERROR: color = Color.parseColor("#EF5350"); break;
            case WARN:  color = Color.parseColor("#FFCA28"); break;
            case INFO:  color = Color.parseColor("#66BB6A"); break;
            default:    color = Color.GRAY; break;
        }
        holder.logText.setTextColor(color);
    }

    @Override
    public int getItemCount() { return filteredLogs.size(); }

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
