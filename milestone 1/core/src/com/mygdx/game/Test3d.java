package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class Test3d extends ApplicationAdapter {
	public PerspectiveCamera cam;
	public Environment environment;
	public ModelBatch modelBatch;
	public Model model;
	public ModelInstance xaxis;
	private ModelInstance cube1;
	private ModelInstance yaxis;
	private ModelInstance zaxis;
	private CameraInputController camController;
	private ModelInstance cube2;

	@Override
	public void create() {
		modelBatch = new ModelBatch();
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight,0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(3f, 3f, 3f);
		cam.lookAt(0,0,0);
		cam.near = 1f;
		cam.far = 300f;
		cam.update();

		ModelBuilder modelBuilder = new ModelBuilder();

		model = modelBuilder.createBox(1f, 1f, 1f,
				new Material(ColorAttribute.createDiffuse(Color.GRAY)),
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

		cube1 = new ModelInstance(model);

		model = modelBuilder.createBox(0.5f, 0.5f, 0.5f,
				new Material(ColorAttribute.createDiffuse(Color.GOLD)),
				VertexAttributes.Usage.Position| VertexAttributes.Usage.Normal);
		cube2 = new ModelInstance(model,2.5f,0,0);

		model = modelBuilder.createArrow(new Vector3(0,0,0),new Vector3(2f,0,0),
				new Material(ColorAttribute.createDiffuse(Color.BLUE)),
				VertexAttributes.Usage.Position);
		xaxis = new ModelInstance(model);
		model = modelBuilder.createArrow(new Vector3(0,0,0),new Vector3(0,2f,0),
				new Material(ColorAttribute.createDiffuse(Color.RED)),
				VertexAttributes.Usage.Position);
		yaxis = new ModelInstance(model);
		model = modelBuilder.createArrow(new Vector3(0,0,0),new Vector3(0,0,2f),
				new Material(ColorAttribute.createDiffuse(Color.GREEN)),
				VertexAttributes.Usage.Position);
		zaxis = new ModelInstance(model);

		camController=new CameraInputController(cam);
		Gdx.input.setInputProcessor(camController);
	}

	@Override
	public void render() {


		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		camController.update();
		modelBatch.begin(cam);
		cube1.transform.rotate(0,1,0,1);
		modelBatch.render(cube1,environment);
		cube2.transform.translate(-2.5f,0,0).rotate(0,1,0,2f).translate(2.5f,0,0);
		modelBatch.render(cube2,environment);
		modelBatch.render(xaxis);
		modelBatch.render(yaxis);
		modelBatch.render(zaxis);
		modelBatch.end();
	}

	@Override
	public void dispose() {
		modelBatch.dispose();
		model.dispose();
	}

	@Override
	public void resize(int width, int height) {
		cam.viewportWidth = Gdx.graphics.getWidth();
		cam.viewportHeight = Gdx.graphics.getHeight();
		cam.update();
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}
