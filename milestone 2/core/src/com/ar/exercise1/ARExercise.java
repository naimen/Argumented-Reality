package com.ar.exercise1;

import com.ar.util.UtilAR;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import org.opencv.core.*;

public class ARExercise extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
	}

	@Override
	public void render () {
		// Create an OpenCV identity matrix
		Mat eye = Mat.eye (128, 128, CvType.CV_8UC1);
		// M u l t i p l y wit h 255 ( w h i t e )
		Core.multiply(eye, new Scalar(255), eye);
		// P a s s m a t r i x t o GDX t h r o u g h u t i l and v i s u a l i z e i t
		UtilAR.imDrawBackground(eye);
	}
}
