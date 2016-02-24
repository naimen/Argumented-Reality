package com.ar.exercise3;

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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;

public class Exercise3 implements ApplicationListener {
    //shape detection parms
    private VideoCapture cam;
    private Mat frame;
    private Mat grayFrame;
    private Mat binaryFrame;
    private long time;
    private ArrayList<MatOfPoint> contours;
    private MatOfPoint bestMarker;

    //solvePnP parms
    private float camWidth;
    private float camHeight;
    private PerspectiveOffCenterCamera pcam;
    private MatOfPoint3f wc;
    private Mat rvec;
    private Mat tvec;

    //3D modeling
    private Model model;
    private Array<ModelInstance> instances;
    private ModelBatch batch;
    private Environment env;
    @Override
    public void create() {
        cam = new VideoCapture(0);
        frame = new Mat();
        grayFrame = new Mat();
        binaryFrame = new Mat();
        time = System.currentTimeMillis();
        contours = new ArrayList<MatOfPoint>();

        camWidth=(float) cam.get(3);
        camHeight=(float) cam.get(4);
        wc = new MatOfPoint3f(new Point3(0,0,0),new Point3(5,0,0),new Point3(5,5,0),new Point3(0,5,0));
        rvec = new Mat();
        tvec = new Mat();
        pcam = new PerspectiveOffCenterCamera();
        pcam.update();

        batch = new ModelBatch();
        env = new Environment();
        env.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        env.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
        instances = new Array<ModelInstance>();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
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
        Imgproc.threshold(grayFrame, binaryFrame, 123, 255, Imgproc.THRESH_BINARY);

        //find contours
        contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(binaryFrame.clone(), contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        bestMarker=null;
        Rect makerOutbound = new Rect(0, 0, 0, 0);
        MatOfPoint2f approxPoly = new MatOfPoint2f();
        boolean found = false;
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint2f coutourMat = new MatOfPoint2f(contours.get(i).toArray());
            Imgproc.approxPolyDP(coutourMat, approxPoly, Imgproc.arcLength(coutourMat, true) * 0.02, true);
            //filter polygons

            MatOfPoint approxPoly2 = new MatOfPoint(approxPoly.toArray());
            if (approxPoly2.total() == 4 &&
                    Math.abs(Imgproc.contourArea(coutourMat)) > 1000 &&
                    Imgproc.isContourConvex((approxPoly2))) {

                if (makerOutbound.area() < Imgproc.boundingRect(approxPoly2).area()) {
                    makerOutbound = Imgproc.boundingRect(approxPoly2);
                    bestMarker = approxPoly2;
                    found = true;
                }
                Imgproc.line(frame, new Point(bestMarker.get(0, 0)), new Point(bestMarker.get(1, 0)), new Scalar(0, 255, 0), 2);
                Imgproc.line(frame, new Point(bestMarker.get(1, 0)), new Point(bestMarker.get(2, 0)), new Scalar(0, 255, 0), 2);
                Imgproc.line(frame, new Point(bestMarker.get(0, 0)), new Point(bestMarker.get(3, 0)), new Scalar(0, 255, 0), 2);
                Imgproc.line(frame, new Point(bestMarker.get(2, 0)), new Point(bestMarker.get(3, 0)), new Scalar(0, 255, 0), 2);
            }
        }
        if (found)
        {
            pcam.setByIntrinsics(UtilAR.getDefaultIntrinsics(camWidth,camHeight), camWidth, camHeight);
            Calib3d.solvePnP(wc, new MatOfPoint2f(bestMarker.toArray()), UtilAR.getDefaultIntrinsics(camWidth,camHeight), UtilAR.getDefaultDistortionCoefficients(),rvec,tvec);
            UtilAR.setCameraByRT(rvec,tvec,pcam);
            drawArrow(0,0,0);
        }

        if (!cam.isOpened()) {
            System.out.println("Error");
        } else if (frameRead) {
            //Imgproc.drawContours(frame, contours, -1, new Scalar(0,0,255), 2);
            UtilAR.imDrawBackground(frame);

        }
        batch.begin(pcam);
        batch.render(instances,env);
        batch.end();
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
    
    public void drawArrow(float x,float y, float z){
        ModelBuilder builder = new ModelBuilder();
        model = builder.createArrow(new Vector3(0,0,0),new Vector3(5f,0,0),
                new Material(ColorAttribute.createDiffuse(Color.BLUE)),
                VertexAttributes.Usage.Position);
        ModelInstance inst =new ModelInstance(model,x,y,z);
        instances.add(inst);
        model = builder.createArrow(new Vector3(0,0,0),new Vector3(0,5f,0),
                new Material(ColorAttribute.createDiffuse(Color.RED)),
                VertexAttributes.Usage.Position);
        inst =new ModelInstance(model,x,y,z);
        instances.add(inst);
        model = builder.createArrow(new Vector3(0,0,0),new Vector3(0,0,-5f),
                new Material(ColorAttribute.createDiffuse(Color.GREEN)),
                VertexAttributes.Usage.Position);
        inst =new ModelInstance(model,x,y,z);
        instances.add(inst);

    }

}