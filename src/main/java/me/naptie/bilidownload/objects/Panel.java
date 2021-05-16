package me.naptie.bilidownload.objects;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Panel extends java.awt.Panel {

	private final Image screenImage = new BufferedImage(500, 500, 2);

	private final Graphics2D screenGraphic = (Graphics2D) screenImage.getGraphics();

	private Image backgroundImage;

	public Panel(String image, Dimension size) {
		loadImage(image);
		// 设定焦点在本窗体
		setFocusable(true);
		setPreferredSize(size);
		// 绘制背景
		drawView();
	}

	private void loadImage(String image) {
		ImageIcon icon = new ImageIcon(image);
		backgroundImage = icon.getImage();
	}

	private void drawView() {
		screenGraphic.drawImage(backgroundImage, 0, 0, null);
	}

	public void paint(Graphics g) {
		g.drawImage(screenImage, 0, 0, null);
	}

}
