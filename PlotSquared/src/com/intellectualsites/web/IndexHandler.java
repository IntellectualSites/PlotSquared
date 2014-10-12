package com.intellectualsites.web;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;

/**
 * Created by Citymonstret on 2014-09-20.
 */
public class IndexHandler implements Container {

	private JavaPlugin plugin;
	private String title;

	public IndexHandler(JavaPlugin plugin, String title) {
		this.plugin = plugin;
		this.title = title;
	}

	@Override
	public void handle(Request request, Response response) {
		try {
			PrintStream body;
			long time;
			String coverage;

			body = response.getPrintStream();
			time = System.currentTimeMillis();
			request.getQuery();
			request.getPath();

			if ((request.getInteger("page")) < 0) {
			}
			if (((coverage = request.getTarget()) == null) || coverage.equals("/")) {
				coverage = "index";
			}

			coverage = coverage.toLowerCase();

			List<String> list = new ArrayList<>(Arrays.asList(new String[] { "install", "index", "stylesheet" }));

			if (!list.contains(coverage)) {
				coverage = "index";
			}

			if (coverage.equals("stylesheet")) {
				response.setValue("Content-Type", "text/css");
				response.setValue("Server", "PlotWeb/1.0 (Simple 5.0)");
				response.setDate("Date", time);
				response.setDate("Last-Modified", time);

				ResourceHandler stylesheet =
						new ResourceHandler("stylesheet", ResourceHandler.FileType.CSS, this.plugin.getDataFolder());

				String stylesheetHTML = stylesheet.getHTML();

				stylesheet.done();

				body.print(stylesheetHTML);
			}
			else {
				response.setValue("Content-Type", "html");
				response.setValue("Server", "PlotWeb/1.0 (Simple 5.0)");
				response.setDate("Date", time);
				response.setDate("Last-Modified", time);

				ResourceHandler header =
						new ResourceHandler("header", ResourceHandler.FileType.HTML, this.plugin.getDataFolder());
				ResourceHandler footer =
						new ResourceHandler("footer", ResourceHandler.FileType.HTML, this.plugin.getDataFolder());
				ResourceHandler cPage =
						new ResourceHandler(coverage, ResourceHandler.FileType.HTML, this.plugin.getDataFolder());

				String headerHTML = header.getHTML().replace("@title", this.title);
				String footerHTML = footer.getHTML();
				String cPageHTML = cPage.getHTML();

				header.done();
				footer.done();
				cPage.done();

				body.print(headerHTML);
				body.print(cPageHTML);
				body.print(footerHTML);
			}
			body.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
