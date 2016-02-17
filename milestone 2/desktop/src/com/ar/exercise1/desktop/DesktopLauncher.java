package com.ar.exercise1.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.ar.exercise1.*;

public class DesktopLauncher {
	public static void main (String[] arg) {
		System.loadLibrary("opencv_java310");
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.height = 480;
		config.width = 640;
		new LwjglApplication(new Exercise2(), config);
	}
}
