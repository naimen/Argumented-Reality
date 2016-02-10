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
import java.util.ArrayList;
import java.util.List;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.*;

import com.ar.exercise1.UtilAR;

public class Exercise2 implements ApplicationListener {

	private VideoCapture cam;
	private Mat frame;
	private Mat grayFrame;
	private MatOfPoint2f corners;
	private Size boardSize;
	private long time;

	//Calibration variables
	private MatOfPoint3f obj;
	private List<Mat> imagePoints;
	private List<Mat> objectPoints;
	private int pointsLogged = 0;
	private Mat intrinsic;
	private Mat distCoeffs;
	
	@Override
	public void create() {
		cam = new VideoCapture(0);
		frame = new Mat();
		grayFrame = new Mat();
		corners = new MatOfPoint2f();
		corners.alloc(7);
        boardSize = new Size(7, 5);
        time = System.currentTimeMillis();
        
        MatOfPoint3f obj = new MatOfPoint3f();
    	List<Mat> imagePoints = new ArrayList<Mat>();
    	List<Mat> objectPoints = new ArrayList<Mat>();
    	Mat intrinsic = new Mat(3, 3, CvType.CV_32FC1);
    	Mat distCoeffs = new Mat();
        
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
		
		//Stores the corner locations in a the 'corners' parameter, based on the frame image
        boolean found = Calib3d.findChessboardCorners(frame, boardSize, corners, Calib3d.CALIB_CB_ADAPTIVE_THRESH + Calib3d.CALIB_CB_NORMALIZE_IMAGE + Calib3d.CALIB_CB_FAST_CHECK);
        
        //Does magic to make corners more accurate, based on greyscale image
        if(found) {
        	TermCriteria term = new TermCriteria(TermCriteria.EPS | TermCriteria.MAX_ITER, 30, 0.1);
            Imgproc.cornerSubPix(grayFrame, corners, new Size(15, 11), new Size(-1, -1), term);
            logPoints();
            System.out.println(found);
        }
        
        if(!cam.isOpened()){
            System.out.println("Error");
        }
        else { //If cam works and frame read, draw frame, with chessboard corners if they were found
        	if (frameRead){
        		Calib3d.drawChessboardCorners(frame, boardSize, corners, found);
        		UtilAR.imDrawBackground(frame);
        	}
        }
	}
	
	private void logPoints() {
		if (time > (System.currentTimeMillis() + 3*1000)) {
			if (pointsLogged < 10)
			{
				// save all the needed values
				imagePoints.add(corners);
				objectPoints.add(obj);
				pointsLogged++;
				System.out.println("Logged points!");
			}
			
			// reach the correct number of images needed for the calibration
			if (pointsLogged == 10)
			{
				this.calibrateCamera();
				System.out.println("Calibrated camera!");
			}
		}
	}
	
	private void calibrateCamera() {
		//Do magic
		System.out.println("Do magic camera stuff!");
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