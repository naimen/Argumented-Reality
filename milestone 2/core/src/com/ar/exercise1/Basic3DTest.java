package com.ar.exercise1;

import com.ar.util.UtilAR;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
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

import org.opencv.core.*;

public class Basic3DTest implements ApplicationListener {
	public PerspectiveCamera cam;
	public ModelBatch modelBatch;
	public Model model;
	public Array<ModelInstance> instances = new Array<ModelInstance>();
    public Environment environment;
    public CameraInputController camController;
    public ModelInstance box1;
    public ModelInstance box2;
    private Float alpha = 0f;
    

	@Override
	public void create() {
		modelBatch = new ModelBatch();
		
		environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
		
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0f, 20f, 0f);
        cam.lookAt(0,0,0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();
        
        camController = new CameraInputController(cam);
        Gdx.input.setInputProcessor(camController);
        
        ModelBuilder modelBuilder = new ModelBuilder();
        
        
        //Box1
        model = modelBuilder.createBox(1f, 1f, 1f, 
                new Material(ColorAttribute.createDiffuse(Color.GREEN)),
                Usage.Position | Usage.Normal);
        box1 = new ModelInstance(model);
        box1.transform.setToTranslation(1f, 1f, 1f);
        instances.add(box1);
            
        //Box2
        model = modelBuilder.createBox(0.5f, 0.5f, 0.5f, 
                new Material(ColorAttribute.createDiffuse(Color.TEAL)),
                Usage.Position | Usage.Normal);
        box2 = new ModelInstance(model);
        box2.transform.setToTranslation(2.5f, 2.5f, 0f);
        instances.add(box2);
            
        //Coordinate System
        Vector3 zeroVect = new Vector3(0f, 0f, 0f);
        Vector3 xAxis = new Vector3(40f, 0f, 0f);
        Vector3 yAxis = new Vector3(0f, 40f, 0f);
        Vector3 zAxis = new Vector3(0f, 0f, 40f);
        
        model = modelBuilder.createArrow(zeroVect, xAxis,
        		new Material(ColorAttribute.createDiffuse(Color.GREEN)), 
        		Usage.Position | Usage.Normal);
        instances.add(new ModelInstance(model));
        
        model = modelBuilder.createArrow(zeroVect, yAxis,
        		new Material(ColorAttribute.createDiffuse(Color.BLUE)), 
        		Usage.Position | Usage.Normal);
        instances.add(new ModelInstance(model));
        
        model = modelBuilder.createArrow(zeroVect, zAxis,
        		new Material(ColorAttribute.createDiffuse(Color.RED)), 
        		Usage.Position | Usage.Normal);
        instances.add(new ModelInstance(model));
        
        model = modelBuilder.createBox(3.5f,3.5f,3.5f,
				new Material(ColorAttribute.createDiffuse(Color.GREEN)),
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
		
        for(int y=0; y <4; y++) {
			for(int x=0; x<6; x++) {
				ModelInstance inst = new ModelInstance(model);
				
				if((x%2 == 0 && y%2 == 1) || (x%2 == 1 && y%2 == 0)){
					inst.transform.translate(1.75f + x*3.5f, 1.75f + y*3.5f, 1.75f);	
					instances.add(inst);
				}
				
			}
		}
        
//        Gdx.graphics.setContinuousRendering(false);
//        Gdx.graphics.requestRendering();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void render() {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        
        Vector3 yAxis = new Vector3(0f, 40f, 0f);
        box1.transform.rotate(Vector3.Y, 1f);
        
        Float newX = 0f;
        Float newZ = 0f;
//        Float oldX = box2.transform.getTranslation(new Vector3()).x;
//        Float oldZ = box2.transform.getTranslation(new Vector3()).z;
        
        newX = (float)(Math.cos(alpha) * 2.5f);
        newZ = (float)(Math.sin(alpha) * 2.5f);
        alpha += 0.1f;
        
        box2.transform.setToTranslation(newX, 2.5f, newZ);
        
        // Create an OpenCV identity matrix
     	Mat eye = Mat.eye (128, 128, CvType.CV_8UC1);
     	// Multiply with 255 (white)
   		Core.multiply(eye, new Scalar(255), eye);
     	// Pass matrix to GDX through util and visualize it
     	UtilAR.imDrawBackground(eye);
        
        modelBatch.begin(cam);
        modelBatch.render(instances, environment);
        modelBatch.end();
        
        camController.update();
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
		modelBatch.dispose();
        model.dispose();
	}

}