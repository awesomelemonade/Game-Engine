package lemon.engine.game;

import lemon.engine.math.Location;
import lemon.engine.math.Matrix;
import lemon.engine.math.ProjectionMatrix;
import lemon.engine.math.TransformationMatrix;
import lemon.engine.shader.UniformTransformationMatrix;

public class Camera {
	private Location location;
	private UniformTransformationMatrix[] uniformMatrices;
	public Camera(Location location, UniformTransformationMatrix... uniformMatrices){
		this.location = location;
		this.uniformMatrices = uniformMatrices;
	}
	public void loadProjectionMatrix(float fov, float aspect, float zNear, float zFar){
		Matrix matrix = ProjectionMatrix.getPerspective(fov, aspect, zNear, zFar);
		for(UniformTransformationMatrix uniformMatrix: uniformMatrices){
			uniformMatrix.getUniformProjectionMatrix().loadMatrix(matrix);
		}
	}
	public void loadViewMatrix(){
		Matrix matrix = TransformationMatrix.getScalar(location.getScale())
				.multiply(TransformationMatrix.getRotation(location.getRotation().invert()))
				.multiply(TransformationMatrix.getTranslation(location.getPosition().invert()));
		for(UniformTransformationMatrix uniformMatrix: uniformMatrices){
			uniformMatrix.getUniformProjectionMatrix().loadMatrix(matrix);
		}
	}
	public void loadModelMatrix(Location location){
		Matrix matrix = TransformationMatrix.get(location);
		for(UniformTransformationMatrix uniformMatrix: uniformMatrices){
			uniformMatrix.getUniformProjectionMatrix().loadMatrix(matrix);
		}
	}
	public Location getLocation(){
		return location;
	}
}
