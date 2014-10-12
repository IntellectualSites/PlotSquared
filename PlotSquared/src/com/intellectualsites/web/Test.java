package com.intellectualsites.web;

/**
 * Created by Citymonstret on 2014-09-20.
 */
public class Test {

	public static void main(String[] args) {
		try {
			new PlotWeb("Test", 9000).start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
