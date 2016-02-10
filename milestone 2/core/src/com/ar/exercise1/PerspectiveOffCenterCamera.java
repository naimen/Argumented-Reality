package com.ar.exercise1;

import org.opencv.core.Mat;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

/**
 * Camera using perspective projection including off center projection.
 */
public class PerspectiveOffCenterCamera extends Camera {

	/** The horizontal shift of the view volume. The center is at zero. If the shift is set to the image ratio, the volume is shifted by half of the image width. With the negative view port ratio, it is shifted by minus half of the view port. */
	public float shiftX = 0;
	/** The vertical shift of the view volume. The center is at zero. -1 shifts it by half of the view port height. */
	public float shiftY = 0;
	/** The horizontal field of view in degrees */
	public float fovX = 39.8f;
	/** The vertical field of view in degrees */
	public float fovY = 39.8f;

	//Temps
	final Vector3 tmp = new Vector3();
	private double[] matData = new double[1];

	public PerspectiveOffCenterCamera() {
		near = 0.01f;
	}

	/**
	 * Sets the Camera properties according to the given camera intrinsics based on the pinhole camera model.
	 * @param intrinsics The camera intrinsics matrix in the pinhole model format
	 * @param The width in pixels of the captured camera image
	 * @param The height in pixels of the captured camera image
	 */
	public void setByIntrinsics(Mat intrinsics,float imgWidth,float imgHeight) {

		viewportWidth = imgWidth;
		viewportHeight = imgHeight;

		intrinsics.get(0,0, matData);
		float focX = (float)matData[0];
		intrinsics.get(1,1, matData);
		float focY = (float)matData[0];
		intrinsics.get(0,2, matData);
		shiftX = 0.5f-(float)matData[0]/imgWidth;
		intrinsics.get(1,2, matData);
		shiftY = -(0.5f-(float)matData[0]/imgHeight);

		fovX = (float)((2*Math.atan(imgWidth*0.5f/focX))/Math.PI*180);
		fovY = (float)((2*Math.atan(imgHeight*0.5f/focY))/Math.PI*180);

		update(true);
	}

	/**
	 * Updates the transformations of the camera
	 */
	@Override
	public void update() {
		update(true);
	}

	/**
	 * Updates the transformations of the camera
	 * @param updateFrustum Tells, whether or not, the projection should be updated as well
	 */
	@Override
	public void update(boolean updateFrustum) {

		float nearRight = (float)Math.tan(fovX*0.5f*(float)Math.PI/180)*near;
		float nearTop = (float)Math.tan(fovY*0.5f*(float)Math.PI/180)*near;
		float nearHeight = (nearTop*2);
		float nShiftX = shiftX*nearHeight;
		float nShiftY = shiftY*nearHeight;
		float nearLeft = -nearRight+nShiftX;
		nearRight += nShiftX;
		float nearBottom = -nearTop+nShiftY;
		nearTop += nShiftY;

		projection.setToProjection(nearLeft,nearRight,nearBottom,nearTop, near,far);

		view.setToLookAt(position, tmp.set(position).add(direction), up);
		combined.set(projection);
		Matrix4.mul(combined.val, view.val);

		if (updateFrustum) {
			invProjectionView.set(combined);
			Matrix4.inv(invProjectionView.val);
			frustum.update(invProjectionView);
		}
	}

}