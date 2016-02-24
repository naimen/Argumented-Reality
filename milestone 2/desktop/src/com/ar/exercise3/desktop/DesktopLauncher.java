package com.ar.exercise3.desktop;

import com.ar.exercise3.*;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	public static void main (String[] arg) {
		System.loadLibrary("opencv_java310");
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.height = 480;
		config.width = 640;
		new LwjglApplication(new Exercise3(), config);
	}
}
