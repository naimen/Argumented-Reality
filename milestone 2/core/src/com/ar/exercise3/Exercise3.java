package com.ar.exercise3;

import com.ar.util.UtilAR;
import com.badlogic.gdx.ApplicationListener;
import org.opencv.core.*;
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
		//turn the read frame to binary
		boolean frameRead = cam.retrieve(frame);
		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
		Imgproc.threshold(grayFrame,binaryFrame,123,255,Imgproc.THRESH_BINARY);

		//find contours
		contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(binaryFrame.clone(), contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

		//UtilAR.imShow(binaryFrame);
		MatOfPoint2f polyFrame = new MatOfPoint2f();

		for(int i =0;i<contours.size();i++)
		{
			MatOfPoint2f coutourMat = new MatOfPoint2f(contours.get(i).toArray());
			Imgproc.approxPolyDP(coutourMat,polyFrame,Imgproc.arcLength(coutourMat,true)*0.02,true);
			//filter polygons

			MatOfPoint polyFrame2 = new MatOfPoint(polyFrame.toArray());
			if(polyFrame2.total()==4 &&
					Math.abs(Imgproc.contourArea(coutourMat))>1000 &&
					Imgproc.isContourConvex((polyFrame2)))
			{
				System.out.println(polyFrame2.rows());
				Imgproc.rectangle(frame,new Point(polyFrame2.get(0,0)),new Point(polyFrame2.get(2,0)),new Scalar(0,255,0),5);

			}
		}

		if(!cam.isOpened()){
			System.out.println("Error");
		}else if (frameRead){
			Imgproc.drawContours(frame, contours, -1, new Scalar(0,0,255), 2);
			UtilAR.imDrawBackground(frame);

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