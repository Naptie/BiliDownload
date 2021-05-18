package me.naptie.bilidownload.objects;

import me.naptie.bilidownload.utils.LoginManager;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Frame extends java.awt.Frame {

	public Frame(String title, String image, Dimension size, boolean tv) {
		this.setTitle(title);
		Panel panel = new Panel(image, size);
		this.add(panel);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (LoginManager.sessData != null && LoginManager.sessData.equalsIgnoreCase("*SessData_Not_Yet_Prepared*")) {
					LoginManager.loginWeb();
				}
				if (tv && LoginManager.accessToken != null && LoginManager.accessToken.equalsIgnoreCase("*Token_Not_Yet_Prepared*")) {
					LoginManager.loginTV();
				}
				dispose();
			}
		});
		this.setAlwaysOnTop(true);
		this.pack();
		this.setVisible(true);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setAlwaysOnTop(false);
	}

}

