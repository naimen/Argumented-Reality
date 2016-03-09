package com.ar.exercise3;

import com.ar.util.PerspectiveOffCenterCamera;
import com.ar.util.UtilAR;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;
import java.util.List;

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
	private AssetManager assets;
	private ModelBuilder builder;
	private ModelLoader loader;
	private Model model;
	private ModelBatch batch;
	private Environment environment;
	private ModelInstance maid1;
	private ModelInstance maid2;
	private ModelInstance spyro;
	private boolean loading;
	private AnimationController controller1;
	private AnimationController controller2;

	//Translation-rotation stuff
	private MatOfPoint3f wc;


    //Homography stuff
    private Mat homographyPlane;
    private MatOfPoint2f drawboard;
    private MatOfPoint2f drawboard2;
    private Mat outputMat;
    private ArrayList<MatOfPoint> markerBorderList;
    private ArrayList<MatOfPoint> sixBorderList;
    private ArrayList<MatOfPoint> tenBorderList;
    
    String model1path = "maid_model/maid1.g3db";
	String model2path = "maid_model/maid2.g3db";
	String model3path = "Spyro the Dragon by Morganicism.obj";
	private Mat intrinsics;
	private MatOfDouble distCoeffs;
	private boolean m1ready;
	private boolean m2ready;


	@Override
	public void create() {
		cam = new VideoCapture(0);
		frame = new Mat();
		grayFrame = new Mat();
		binaryFrame = new Mat();
		contours = new ArrayList<MatOfPoint>();
		markerBorderList = new ArrayList<MatOfPoint>();
		sixBorderList = new ArrayList<MatOfPoint>();
		tenBorderList = new ArrayList<MatOfPoint>();
		intrinsics = UtilAR.getDefaultIntrinsics(camWidth, camHeight);
		distCoeffs = UtilAR.getDefaultDistortionCoefficients();

		//Rotation-translation stuff
		wc = new MatOfPoint3f();
		wc.push_back(new MatOfPoint3f(new Point3(-5f, 5f, 0.0f)));
		wc.push_back(new MatOfPoint3f(new Point3(5f, 5f, 0.0f)));
		wc.push_back(new MatOfPoint3f(new Point3(5f, -5f, 0.0f)));
		wc.push_back(new MatOfPoint3f(new Point3(-5f, -5f, 0.0f)));
		
		/*rvec = new Mat();
		tvec = new Mat();
		*/
		outputMat = new Mat(100, 100, CvType.CV_8UC4);
		
		//Libgdx coordinate system stuff
		builder = new ModelBuilder();

		//loader = new G3dModelLoader();
    	instances = new Array<ModelInstance>();
		environment = new Environment();

    	assets = new AssetManager();
    	assets.load(model1path, Model.class);
		assets.load(model2path, Model.class);
		assets.load(model3path, Model.class);
    	loading = true;
    	
    	cam.read(frame);
		camWidth=(float) cam.get(3);
		camHeight=(float) cam.get(4);
    	
    	pcam = new PerspectiveOffCenterCamera();
		pcam.near=1f;
		pcam.far=300f;
		pcam.setByIntrinsics(UtilAR.getDefaultIntrinsics(camWidth, camHeight), camWidth, camHeight);
		pcam.update();
		
		batch = new ModelBatch();
		//drawCoordinateSystem();

        homographyPlane = new Mat();
        drawboard = new MatOfPoint2f();
        drawboard.push_back(new MatOfPoint2f(new Point(0f,0f)));
        drawboard.push_back(new MatOfPoint2f(new Point(0f,100f)));
        drawboard.push_back(new MatOfPoint2f(new Point(100f,100f)));
        drawboard.push_back(new MatOfPoint2f(new Point(100f,0f)));
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight,0.5f,0.5f,0.5f,0.5f));
		environment.add(new DirectionalLight().set(0.5f,0.5f,0.5f,-1f,-0.8f,-2f));

		m2ready=false;

		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    }

    private void doneLoading() {
        //model 1
		final Model obj1 = assets.get(model1path, Model.class);
		for (Material m: obj1.materials) {
			m.set(new BlendingAttribute(false,1));
		}
		maid1 = new ModelInstance(obj1);
        //maid1.transform.setToScaling(5f, 5f, 5f);
		controller1 = new AnimationController(maid1);
		//animation idx: 0 lookaround; 1 nothing; 2 waving
		controller1.setAnimation(obj1.animations.get(0).id, -1, new AnimationController.AnimationListener() {
			@Override
			public void onEnd(AnimationController.AnimationDesc animation) {

			}
			@Override
			public void onLoop(AnimationController.AnimationDesc animation) {
				if(m2ready) controller1.animate(obj1.animations.get(2).id,-1,this,1f);
				else controller1.animate(obj1.animations.get(0).id,-1,this,1f);
			}
		});
		instances.add(maid1);

		//model 2
		final Model obj2 = assets.get(model2path,Model.class);
		for (Material m: obj2.materials) {
			m.set(new BlendingAttribute(false,1));
		}
		maid2 = new ModelInstance(obj2);
		controller2 = new AnimationController(maid2);
		//animation idx: 0 bow
		controller2.setAnimation(obj2.animations.get(0).id, 1, new AnimationController.AnimationListener() {
			@Override
			public void onEnd(AnimationController.AnimationDesc animation) {

			}

			@Override
			public void onLoop(AnimationController.AnimationDesc animation) {
				if(m1ready) controller2.animate(obj2.animations.get(0).id,1,this,1f); //doesn't seems to have an effect
			}
		});
		instances.add(maid2);
		
		
		//bear
		final Model obj3 = assets.get(model3path, Model.class);
		spyro = new ModelInstance(obj3);
		instances.add(spyro);
		
		loading = false;
    }
    
	@Override
	public void resize(int width, int height) {
		pcam.viewportWidth = width;
		pcam.viewportHeight = height;
		pcam.update();
	}

	@Override
	public void render() {
		//System.out.println(loading);

		if (loading && assets.update())
            doneLoading();

		pcam.update();
		
		if(!loading) {
			maid1.transform.scale(0f, 0f, 0f);
			maid2.transform.scale(0f, 0f, 0f);
			spyro.transform.scale(0f, 0f, 0f);
		}
		
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_FRAMEBUFFER);

		//turn the read frame to binary
		boolean frameRead = cam.read(frame);

		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
		Imgproc.threshold(grayFrame,binaryFrame,123,255,Imgproc.THRESH_BINARY);

		//find contours
		contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(binaryFrame.clone(), contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

		MatOfPoint rawMarker1 = null;
		MatOfPoint marker1 = null;
		MatOfPoint marker1inner = null;
		MatOfPoint marker2 = null;
		MatOfPoint marker2inner = null;
		MatOfPoint marker3 = null;

		MatOfPoint2f approxPoly = new MatOfPoint2f();
		for(int i=0; i<contours.size(); i++)
		{
			MatOfPoint2f coutourMat = new MatOfPoint2f(contours.get(i).toArray());
			Imgproc.approxPolyDP(coutourMat, approxPoly, Imgproc.arcLength(coutourMat,true)*0.02, true);
			//filter polygons

			MatOfPoint approxPoly2 = new MatOfPoint(approxPoly.toArray());

			//Fill list of polygons with 4 corners (our marker borders)
			if(approxPoly2.total()==4
					&& Math.abs(Imgproc.contourArea(coutourMat))>=8000
					&& Math.abs(Imgproc.contourArea(coutourMat))<100000
					&& Imgproc.isContourConvex((approxPoly2))) {
				markerBorderList.add(approxPoly2);
			}
			//Fill list of polygons with 6 borders (Most likely our marker)
			else if(approxPoly2.total()==6 &&
					 Math.abs(Imgproc.contourArea(coutourMat))>1000) {
				sixBorderList.add(approxPoly2);
			}
			
			else if(approxPoly2.total()==10 &&
					Math.abs(Imgproc.contourArea(coutourMat))>1000) {
				tenBorderList.add(approxPoly2);
			}
		}

		
		//Check if a point in our 6-point polygon is inside one of our markers, if so, we found our marker
		Double inside;
		for(MatOfPoint m1 : markerBorderList) {
			MatOfPoint2f m = new MatOfPoint2f(m1.toArray());
			for(MatOfPoint m2 : sixBorderList) {
				inside = Imgproc.pointPolygonTest(m, new Point(m2.get(0, 0)), false);
				if(inside > 0) {
					marker1 = sortCornerPoints(m1,m2);
					rawMarker1 = m1;
				}
			}
		}

		for(MatOfPoint m1 : markerBorderList) {
			MatOfPoint2f m = new MatOfPoint2f(m1.toArray());
			for(MatOfPoint m2 : markerBorderList) {
				inside = Imgproc.pointPolygonTest(m, new Point(m2.get(0, 0)), false);
				if(inside > 0) {
					marker2 = m1;
					marker2inner = m2;
				}
			}
		}
		
		for(MatOfPoint m1 : markerBorderList) {
			MatOfPoint2f m = new MatOfPoint2f(m1.toArray());
			for(MatOfPoint m2 : tenBorderList) {
				inside = Imgproc.pointPolygonTest(m, new Point(m2.get(0, 0)), false);
				if(inside > 0 && !m1.equals(marker2inner) && !m1.equals(rawMarker1)) {
					marker3 = sortCornerPoints(m1,m2);
				}
			}
		}

		

		markerBorderList.clear();
		sixBorderList.clear();

		if(marker1 != null && loading == false) {
			m1ready = true;
			Mat rvec = new Mat();
			Mat tvec = new Mat();

			drawMarkerBorder(marker1);

			MatOfPoint2f markerCorners = new MatOfPoint2f(marker1.toArray());


			Calib3d.solvePnP(wc, markerCorners, intrinsics, distCoeffs, rvec, tvec);

			
			
			
			//Transform our object to the marker
			Matrix4 transformMatrix = maid1.transform.cpy();
			UtilAR.setTransformByRT(rvec, tvec, transformMatrix);
			maid1.transform.set(transformMatrix);
			maid1.transform.scale(0.5f, 0.5f, 0.5f);
			maid1.transform.rotate(1,0,0,90);

			if (m2ready) {
				Vector3 maid1pos = new Vector3();
				Vector3 maid2pos = new Vector3();
				System.out.println(maid1.transform.getTranslation(maid1pos));
				maid1.transform.getTranslation(maid1pos);
				maid2.transform.getTranslation(maid2pos);
				
				Vector3 direction = new Vector3();
				direction.set(maid2pos).sub(maid1pos).nor();
				Quaternion q = new Quaternion();
				q.setFromCross(Vector3.Z,direction);
				
				//maid1.transform.set(q).setTranslation(maid1pos);
				maid1.transform.rotate(q);
			}


			//maid1.transform.setToLookAt();
			controller1.update(Gdx.graphics.getDeltaTime());

			//homographyPlane = Calib3d.findHomography(markerCorners, drawboard);
			//Imgproc.warpPerspective(frame, outputMat, homographyPlane, new Size(100, 100));

			//outputMat.copyTo(frame.rowRange(0, 100).colRange(0, 100));
		}
		else m1ready = false;

		if(marker2 != null && loading == false) {
			Mat rvec = new Mat();
			Mat tvec = new Mat();
			m2ready = true;

			drawMarkerBorder(marker2);

			MatOfPoint2f markerCorners = new MatOfPoint2f(marker2.toArray());
			Calib3d.solvePnP(wc, markerCorners, intrinsics, distCoeffs, rvec, tvec);

			//Transform our object to the marker
			Matrix4 transformMatrix = maid2.transform.cpy();
			UtilAR.setTransformByRT(rvec, tvec, transformMatrix);
			maid2.transform.set(transformMatrix);
			maid2.transform.scale(0.5f, 0.5f, 0.5f);
			maid2.transform.rotate(1,0,0,90);
			controller2.update(Gdx.graphics.getDeltaTime());
			
			//homographyPlane = Calib3d.findHomography(markerCorners, drawboard);
			//Imgproc.warpPerspective(frame, outputMat, homographyPlane, new Size(100, 100));

			//outputMat.copyTo(frame.rowRange(0, 100).colRange(100, 200));
		}
		else m2ready = false;
		
		if (marker3 != null && loading == false) {
			Mat rvec = new Mat();
			Mat tvec = new Mat();
			
			drawMarkerBorder(marker3);
			
			MatOfPoint2f markerCorners = new MatOfPoint2f(marker3.toArray());
			Calib3d.solvePnP(wc, markerCorners, intrinsics, distCoeffs, rvec, tvec);
			
			Matrix4 transformMatrix = spyro.transform.cpy();
			UtilAR.setTransformByRT(rvec, tvec, transformMatrix);
			spyro.transform.set(transformMatrix);
			spyro.transform.scale(3f, 3f, 3);
			spyro.transform.rotate(1,0,0,90);
		}
		
		if(!cam.isOpened()){
			System.out.println("Error");
		}else if (frameRead){
			//Imgproc.drawContours(frame, contours, -1, new Scalar(0,0,255), 2);
			UtilAR.imDrawBackground(frame);
		}


		batch.begin(pcam);
		batch.render(instances,environment);
		batch.end();
	}

	private void drawMarkerBorder(MatOfPoint marker1) {
		Imgproc.line(frame, new Point(marker1.get(0, 0)), new Point(marker1.get(1, 0)), new Scalar(0, 255, 0), 2);
		Imgproc.line(frame, new Point(marker1.get(1, 0)), new Point(marker1.get(2, 0)), new Scalar(0, 255, 0), 2);
		Imgproc.line(frame, new Point(marker1.get(0, 0)), new Point(marker1.get(3, 0)), new Scalar(0, 255, 0), 2);
		Imgproc.line(frame, new Point(marker1.get(2, 0)), new Point(marker1.get(3, 0)), new Scalar(0, 255, 0), 2);
	}
	
	private boolean isInside(MatOfPoint inside, MatOfPoint border) {
		MatOfPoint2f inside2f = new MatOfPoint2f(inside.toArray());
		double isInside = Imgproc.pointPolygonTest(inside2f, new Point(border.get(0, 0)), false);
		return (isInside > 0);
	}
	
	private boolean noBoxInside(MatOfPoint m) {
		for(MatOfPoint m1 : markerBorderList) {
			MatOfPoint2f inside = new MatOfPoint2f(m1.toArray());
			double isInside = Imgproc.pointPolygonTest(inside, new Point(m.get(0, 0)), false);
			if (isInside > 0) {return false;}
		}
		return true;
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
        
        
        //Test arrows for double markers
//        model = builder.createArrow(zeroVect, zAxis,
//        		new Material(ColorAttribute.createDiffuse(Color.RED)), 
//        		Usage.Position | Usage.Normal);
        
        
//        model = loader.loadModel(Gdx.files.internal("spyro.obj"));
//        System.out.println(model);
//        maid1 = new ModelInstance(model);
//        instances.add(maid1);
        
        
        model = builder.createArrow(zeroVect, zAxis,
        		new Material(ColorAttribute.createDiffuse(Color.RED)), 
        		Usage.Position | Usage.Normal);
        maid2 = new ModelInstance(model);
        instances.add(maid2);
        
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
		instances.clear();
		assets.dispose();
	}

	public MatOfPoint sortCornerPoints(MatOfPoint m1, MatOfPoint m2)
	{
		//find the calibration point in side our marker as a square in the top left corner
		double calibDist = 0;
		Point bestP = new Point();
		for (Point p:m2.toList()) {
			double dist = Imgproc.pointPolygonTest(new MatOfPoint2f(m1.toArray()),p,true);
			if(Math.abs(dist)>calibDist)
			{	bestP=p;
				calibDist=dist;
			}
		}
		//sort the corner according to the distance of calibP
		calibDist = 100;
		Point oriP=new Point();
		for (Point p: m1.toList()) {
			double dist = Math.sqrt(Math.pow((p.x-bestP.x),2)+Math.pow((p.y-bestP.y),2));
			if (dist < calibDist){
				calibDist=dist;
				oriP=p;
			}
		}
		//place corners correctly
		int originalIdx=0;
		List<Point> sorted = new ArrayList();
		for (int i=0;i<4;i++){
			if (oriP.equals(m1.toArray()[i]))
			{
				originalIdx=i;
			}
		}
		sorted.add(m1.toArray()[originalIdx]);
		sorted.add(m1.toArray()[(1+originalIdx)%4]);
		sorted.add(m1.toArray()[(2+originalIdx)%4]);
		sorted.add(m1.toArray()[(3+originalIdx)%4]);
		MatOfPoint res=new MatOfPoint();
		res.fromList(sorted);
		return res;
	}

	private float angleBetween(Point a, Point b)
	{
		double len1 = Math.sqrt(Math.pow(a.x,2)+Math.pow(a.y,2));
		double len2 = Math.sqrt(Math.pow(b.x,2)+Math.pow(b.y,2));

		double dot = a.dot(b);

		double val = dot/(len1*len2);

		if(val>=1.0)
		{
			return 0.0f;
		}
		else if (val<=-1.0)
		{
			return (float) Math.PI;
		}
		else return (float) Math.acos(val);
	}


}