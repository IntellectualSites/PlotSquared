package com.intellectualsites.web;


import com.intellectualcrafters.plot.PlotMain;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import java.net.InetSocketAddress;
import java.net.SocketAddress;


/**
 * Created by Citymonstret on 2014-09-20.
 */
public class PlotWeb {

    public static PlotWeb PLOTWEB;

    private String              title;
    private int                 port;
    private Server              server;
    private Connection          connection;
    private Container           container;
    private SocketAddress       address;

    public PlotWeb(String title, int port) {
        this.title  = title;
        this.port   = port;
    }

    public void start() throws Exception {
        container       = new IndexHandler(JavaPlugin.getPlugin(PlotMain.class), title);
        server          = new ContainerServer(container);
        connection      = new SocketConnection(server);
        address         = new InetSocketAddress(port);

        connection.connect(address);
        PLOTWEB = this;
    }

    public void stop() throws Exception {
        connection.close();
        PLOTWEB = null;
    }
}