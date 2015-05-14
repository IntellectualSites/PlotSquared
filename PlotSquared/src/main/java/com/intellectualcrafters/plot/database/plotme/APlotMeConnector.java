package com.intellectualcrafters.plot.database.plotme;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import org.bukkit.configuration.file.FileConfiguration;

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;

public abstract class APlotMeConnector {
    public abstract Connection getPlotMeConnection(String plugin, FileConfiguration plotConfig, String dataFolder);
    
    public abstract HashMap<String, HashMap<PlotId, Plot>> getPlotMePlots(Connection connection) throws SQLException;
}
