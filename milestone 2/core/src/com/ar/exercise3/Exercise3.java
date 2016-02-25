package com.ar.exercise3;

import com.ar.util.PerspectiveOffCenterCamera;
import com.ar.util.UtilAR;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;

public class Exercise3 implements ApplicationListener {
	private VideoCapture cam;
	private float camWidth;
	private float camHeight;
	private Mat frame;
	private Mat grayFrame;
	private Mat binaryFrame;
	private ArrayList<MatOfPoint> contours;
	
	//Libgdx coordinate system vars
	private PerspectiveOffCenterCamera pcam;
	private Array<ModelInstance> instances;
	private ModelBuilder builder;
	private Model model;
	private ModelBatch batch;
	
	//Translation-rotation stuff
	private MatOfPoint3f wc;
	private Mat rvec;
	private Mat tvec;

    //Homography stuff
    private Mat homographyPlane;
    private MatOfPoint2f drawboard;
    private Mat outputMat;
    private Mat newframe;
    private ArrayList<MatOfPoint> markerBorderList;

    @Override
	public void create() {
		cam = new VideoCapture(1);
		frame = new Mat();
		grayFrame = new Mat();
		binaryFrame = new Mat();
		contours = new ArrayList<MatOfPoint>();
		markerBorderList = new ArrayList<MatOfPoint>();
		
		//Rotation-translation stuff
		wc = new MatOfPoint3f();
		wc.push_back(new MatOfPoint3f(new Point3(-5f, 5f, 0.0f)));
		wc.push_back(new MatOfPoint3f(new Point3(5f, 5f, 0.0f)));
		wc.push_back(new MatOfPoint3f(new Point3(5f, -5f, 0.0f)));
		wc.push_back(new MatOfPoint3f(new Point3(-5f, -5f, 0.0f)));
		
		rvec = new Mat();
		tvec = new Mat();
		
		outputMat = new Mat(100, 100, CvType.CV_8UC4);
		newframe = new Mat();
		
		//Libgdx coordinate system stuff
		builder = new ModelBuilder();
    	instances = new Array<ModelInstance>();
    	
    	cam.read(frame);
		camWidth=(float) cam.get(3);
		camHeight=(float) cam.get(4);
    	
    	pcam = new PerspectiveOffCenterCamera();
		pcam.update();
		
		batch = new ModelBatch();
		drawCoordinateSystem();
		
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        homographyPlane = new Mat();
        drawboard = new MatOfPoint2f();
        drawboard.push_back(new MatOfPoint2f(new Point(0f,0f)));
        drawboard.push_back(new MatOfPoint2f(new Point(0f,100f)));
        drawboard.push_back(new MatOfPoint2f(new Point(100f,100f)));
        drawboard.push_back(new MatOfPoint2f(new Point(100f,0f)));
    }

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
	}

	@Override
	public void render() {
		//turn the read frame to binary
		boolean frameRead = cam.read(frame);
		
		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
		Imgproc.threshold(grayFrame,binaryFrame,123,255,Imgproc.THRESH_BINARY);

		//find contours
		contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(binaryFrame.clone(), contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

		MatOfPoint bestMarker = null;
		Rect makerOutbound = new Rect(0,0,0,0);
		MatOfPoint2f approxPoly = new MatOfPoint2f();
		for(int i=0; i<contours.size(); i++)
		{
			MatOfPoint2f coutourMat = new MatOfPoint2f(contours.get(i).toArray());
			Imgproc.approxPolyDP(coutourMat, approxPoly, Imgproc.arcLength(coutourMat,true)*0.02, true);
			//filter polygons

			MatOfPoint approxPoly2 = new MatOfPoint(approxPoly.toArray());
			if(approxPoly2.total()==4 &&
					Math.abs(Imgproc.contourArea(coutourMat))>1000 &&
					Imgproc.isContourConvex((approxPoly2))) {
				
				markerBorderList.add(approxPoly2);

			}
		}

		Double inside;
		for(MatOfPoint m1 : markerBorderList) {
			MatOfPoint2f m = new MatOfPoint2f(m1.toArray());
			for(MatOfPoint m2 : markerBorderList) {
				inside = Imgproc.pointPolygonTest(m, new Point(m2.get(0, 0)), false);
				if(inside > 0) {
					bestMarker = m1;
				}
			}
		}

		markerBorderList.clear();

		if(bestMarker != null) {

			Imgproc.line(frame, new Point(bestMarker.get(0, 0)), new Point(bestMarker.get(1, 0)), new Scalar(0, 255, 0), 2);
			Imgproc.line(frame, new Point(bestMarker.get(1, 0)), new Point(bestMarker.get(2, 0)), new Scalar(0, 255, 0), 2);
			Imgproc.line(frame, new Point(bestMarker.get(0, 0)), new Point(bestMarker.get(3, 0)), new Scalar(0, 255, 0), 2);
			Imgproc.line(frame, new Point(bestMarker.get(2, 0)), new Point(bestMarker.get(3, 0)), new Scalar(0, 255, 0), 2);

			MatOfPoint2f markerCorners = new MatOfPoint2f(bestMarker.toArray());
			pcam.setByIntrinsics(UtilAR.getDefaultIntrinsics(camWidth, camHeight), camWidth, camHeight);
			Calib3d.solvePnP(wc, markerCorners, UtilAR.getDefaultIntrinsics(camWidth, camHeight), UtilAR.getDefaultDistortionCoefficients(), rvec, tvec);
			UtilAR.setCameraByRT(rvec, tvec, pcam);

			homographyPlane = Calib3d.findHomography(markerCorners, drawboard);
			Imgproc.warpPerspective(frame, outputMat, homographyPlane, new Size(100, 100));

			outputMat.copyTo(frame.rowRange(0, 100).colRange(0, 100));
		}

		System.out.println(homographyPlane.dump());
		if(!cam.isOpened()){
			System.out.println("Error");
		}else if (frameRead){
			//Imgproc.drawContours(frame, contours, -1, new Scalar(0,0,255), 2);
			UtilAR.imDrawBackground(frame);
		}
		
		batch.begin(pcam);
		batch.render(instances);
		batch.end();
	}
	
	private void drawCoordinateSystem() {
		//Coordinate System
        Vector3 zeroVect = new Vector3(0f, 0f, 0f);
        Vector3 xAxis = new Vector3(5f, 0f, 0f);
        Vector3 yAxis = new Vector3(0f, 5f, 0f);
        Vector3 zAxis = new Vector3(0f, 0f, 5f);
        
        model = builder.createArrow(zeroVect, xAxis,
        		new Material(ColorAttribute.createDiffuse(Color.GREEN)), 
        		Usage.Position | Usage.Normal);
        instances.add(new ModelInstance(model));
        
        model = builder.createArrow(zeroVect, yAxis,
        		new Material(ColorAttribute.createDiffuse(Color.BLUE)), 
        		Usage.Position | Usage.Normal);
        instances.add(new ModelInstance(model));
        
        model = builder.createArrow(zeroVect, zAxis,
        		new Material(ColorAttribute.createDiffuse(Color.RED)), 
        		Usage.Position | Usage.Normal);
        instances.add(new ModelInstance(model));
        
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
		batch.dispose();
		model.dispose();
	}

}