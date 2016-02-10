package com.ar.exercise1;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;

import java.awt.image.BufferedImage;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.*;

import com.ar.exercise1.UtilAR;

public class Exercise2 implements ApplicationListener {

	private VideoCapture cam;
	private Mat frame;
	private Mat grayFrame;
	MatOfPoint2f corners;
	Size boardSize;
	long time;
	
	@Override
	public void create() {
		cam = new VideoCapture(0);
		frame = new Mat();
		grayFrame = new Mat();
		corners = new MatOfPoint2f();
		corners.alloc(7);
        boardSize = new Size(7, 5);
        time = System.currentTimeMillis();
        cam.read(frame);
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void render() {
		boolean frameRead = cam.read(frame);
		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        boolean found = Calib3d.findChessboardCorners(frame, boardSize, corners, Calib3d.CALIB_CB_ADAPTIVE_THRESH + Calib3d.CALIB_CB_NORMALIZE_IMAGE + Calib3d.CALIB_CB_FAST_CHECK);
        
        if(found) {
        	TermCriteria term = new TermCriteria(TermCriteria.EPS | TermCriteria.MAX_ITER, 30, 0.1);
            Imgproc.cornerSubPix(grayFrame, corners, new Size(15, 11), new Size(-1, -1), term);
            System.out.println(found);
        }
        
        if(!cam.isOpened()){
            System.out.println("Error");
        }
        else {                  
        	if (frameRead){
        		Calib3d.drawChessboardCorners(frame, boardSize, corners, found);
        		UtilAR.imDrawBackground(frame);
        	}
        }
	}
	
	private void logPoints() {
		if (time > (System.currentTimeMillis() + 3*1000)) {
			
			System.out.println("Logged points!");
		}
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		cam.release();
	}

}