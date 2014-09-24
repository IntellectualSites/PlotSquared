package com.intellectualsites.web;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.bukkit.plugin.java.JavaPlugin;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import com.intellectualcrafters.plot.PlotMain;

/**
 * Created by Citymonstret on 2014-09-20.
 */
public class PlotWeb {

    public static PlotWeb PLOTWEB;

    private String title;
    private int port;
    private Server server;
    private Connection connection;
    private Container container;
    private SocketAddress address;

    public PlotWeb(String title, int port) {
        this.title = title;
        this.port = port;
    }

    public void start() throws Exception {
        this.container = new IndexHandler(JavaPlugin.getPlugin(PlotMain.class), this.title);
        this.server = new ContainerServer(this.container);
        this.connection = new SocketConnection(this.server);
        this.address = new InetSocketAddress(this.port);

        this.connection.connect(this.address);
        PLOTWEB = this;
    }

    public void stop() throws Exception {
        this.connection.close();
        PLOTWEB = null;
    }
}
