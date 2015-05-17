package lemon.engine.game;

import lemon.engine.math.Location;
import lemon.engine.math.ProjectionMatrix;
import lemon.engine.math.TransformationMatrix;
import lemon.engine.shader.UniformTransformationMatrix;

public class Camera {
	private Location location;
	private UniformTransformationMatrix uniformMatrix;
	public Camera(Location location){
		this.location = location;
	}
	public void loadProjectionMatrix(float fov, float aspect, float zNear, float zFar){
		uniformMatrix.getUniformProjectionMatrix().loadMatrix(ProjectionMatrix.getPerspective(fov, aspect, zNear, zFar));
	}
	public void loadViewMatrix(){
		uniformMatrix.getUniformViewMatrix().loadMatrix(TransformationMatrix.getScalar(location.getScale())
			.multiply(TransformationMatrix.getRotation(location.getRotation().invert()))
			.multiply(TransformationMatrix.getTranslation(location.getPosition().invert())));
	}
	public void loadModelMatrix(Location location){
		uniformMatrix.getUniformModelMatrix().loadMatrix(TransformationMatrix.get(location));
	}
	public void setMatrix(UniformTransformationMatrix matrix){
		uniformMatrix = matrix;
	}
	public UniformTransformationMatrix getMatrix(){
		return uniformMatrix;
	}
	public Location getLocation(){
		return location;
	}
}
