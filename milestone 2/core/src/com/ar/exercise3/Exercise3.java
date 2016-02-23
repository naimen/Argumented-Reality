package com.ar.exercise3;

import com.ar.util.UtilAR;
import com.badlogic.gdx.ApplicationListener;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;

public class Exercise3 implements ApplicationListener {
	private VideoCapture cam;
	private Mat frame;
	private Mat grayFrame;
	private Mat binaryFrame;
	private long time;
	private ArrayList<MatOfPoint> contours;

	@Override
	public void create() {
		cam = new VideoCapture(0);
		frame = new Mat();
		grayFrame = new Mat();
		binaryFrame = new Mat();
		time = System.currentTimeMillis();
		contours = new ArrayList<MatOfPoint>();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
	}

	@Override
	public void render() {
		//turn the readed frame to binary
		boolean frameRead = cam.retrieve(frame);
		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
		Imgproc.threshold(grayFrame,binaryFrame,123,255,Imgproc.THRESH_BINARY);

		//find contours
		Imgproc.findContours(binaryFrame.clone(),contours,new Mat(),Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);


		if(!cam.isOpened()){
			System.out.println("Error");
		}else if (frameRead){
			//Imgproc.drawContours(binaryFrame,contours,0,new Scalar(255,0,0));
			UtilAR.imDrawBackground(binaryFrame);
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