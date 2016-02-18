package com.ar.exercise1;

import com.ar.util.PerspectiveOffCenterCamera;
import com.ar.util.UtilAR;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.utils.Array;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Exercise2 implements ApplicationListener {

	private VideoCapture cam;
	private Mat frame;
	private Mat grayFrame;
	private MatOfPoint2f corners;
	private Size boardSize;
	private long time;
	private PerspectiveOffCenterCamera pcam;
	private float camWidth;
	private float camHeight;


	//Calibration variables
	private boolean calibrated;
	private MatOfPoint3f wc;
	private List<Mat> worldPoints;
	private List<Mat> imagePoints;
	private int pointsLogged = 0;
	private Mat calibIntrinsic;
	private MatOfDouble distCoeffs;
	private Mat rvec;
	private Mat tvec;

	//Box stuffs
	private Array<ModelInstance> instances;
	private ModelBuilder builder;
	private Model model;
	private ModelBatch batch;
	private Environment env;
	private float boxSize;

	@Override
	public void create() {
		cam = new VideoCapture(0);
		frame = new Mat();
		grayFrame = new Mat();
		corners = new MatOfPoint2f();
        boardSize = new Size(7, 5);
        time = System.currentTimeMillis();
		rvec = new Mat();
		tvec = new Mat();
        
        wc = new MatOfPoint3f();
    	imagePoints = new ArrayList<Mat>();
    	worldPoints = new ArrayList<Mat>();
    	distCoeffs = new MatOfDouble();
    	boxSize = 3.5f;
    	
    	for (int i = 0; i < boardSize.width*boardSize.height; i++) {
    		float x = i%(int)boardSize.width;
			float y = i/(int)boardSize.width;
			wc.push_back(new MatOfPoint3f(new Point3(boxSize*x, boxSize*y, 0.0f)));

    	}
    	
    	//Box stuff
    	builder = new ModelBuilder();
    	instances = new Array<ModelInstance>();
        
        cam.read(frame);
		camWidth=(float) cam.get(3);
		camHeight=(float) cam.get(4);

		pcam = new PerspectiveOffCenterCamera();
		pcam.update();
		
		batch = new ModelBatch();
		drawBoxes();
		env = new Environment();
		env.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		env.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
		
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
	}

	@Override
	public void render() {
		boolean frameRead = cam.read(frame);
//		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
		
		//Stores the corner locations in the 'corners' parameter, based on the frame image
        boolean found = Calib3d.findChessboardCorners(frame, boardSize, corners,
				Calib3d.CALIB_CB_ADAPTIVE_THRESH + Calib3d.CALIB_CB_NORMALIZE_IMAGE + Calib3d.CALIB_CB_FAST_CHECK);
        
        //Refines corners, based on greyscale image
        if(found) {
//        	TermCriteria term = new TermCriteria(TermCriteria.EPS | TermCriteria.MAX_ITER, 30, 0.1);
//          Imgproc.cornerSubPix(grayFrame, corners, new Size(15, 11), new Size(-1, -1), term);
            //logPoints();
        	
            pcam.setByIntrinsics(UtilAR.getDefaultIntrinsics(camWidth,camHeight), camWidth, camHeight);
            
            Calib3d.solvePnP(wc, corners, UtilAR.getDefaultIntrinsics(camWidth,camHeight), UtilAR.getDefaultDistortionCoefficients(),rvec,tvec);
			UtilAR.setCameraByRT(rvec,tvec,pcam);
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
        
		batch.begin(pcam);
		batch.render(instances,env);
		batch.end();
	}

	private void drawBoxes(){

		model = builder.createBox(boxSize,boxSize,boxSize,
				new Material(ColorAttribute.createDiffuse(Color.GREEN)),
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
		
		
		for(int y=0; y <4; y++) {
			for(int x=0; x<6; x++) {
				ModelInstance inst = new ModelInstance(model);
				
				if((x%2 == 0 && y%2 == 1) || (x%2 == 1 && y%2 == 0)){
					inst.transform.translate(1.75f + x*boxSize, 1.75f + y*boxSize, -1.75f);	
					instances.add(inst);
				}
			}
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
		batch.dispose();
		model.dispose();
	}
	
	
	//CALIBRATION MAGIC, IT'S SHIT, DON'T USE UNLESS FIXED
	private void logPoints() {
		if (time + 3*1000 < System.currentTimeMillis()) {
			if (pointsLogged < 3)
			{
				// save all the needed values
				imagePoints.add(corners);
				worldPoints.add(wc);
				pointsLogged++;
				time = System.currentTimeMillis();
				//System.out.println("Logged points!");
				//System.out.println(corners.dump());
				//System.out.println(wc.dump());
			}
			
			// reach the correct number of images needed for the calibration
			if (pointsLogged == 3 && !calibrated)
			{
				this.calibrateCamera();
				System.out.println("Calibrated camera!");
			}
		}
	}
	
	private void calibrateCamera() {
		//Do magic
		System.out.println("Do magic camera stuff!");
		List<Mat> rvecs = new ArrayList<>();
		List<Mat> tvecs = new ArrayList<>();
		calibIntrinsic.put(0, 0, 1);
		calibIntrinsic.put(1, 1, 1); //Tutorial did this...
		Calib3d.calibrateCamera(worldPoints, imagePoints, grayFrame.size(), calibIntrinsic, distCoeffs, rvecs, tvecs);
		//System.out.println("Did magic camera stuff!");
		//System.out.println("Intrinsic: " + intrinsic.dump());
		//System.out.println("distCoeffs: " + distCoeffs.dump());
		calibrated = true;
	}

}