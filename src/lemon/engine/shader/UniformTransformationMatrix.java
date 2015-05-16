package lemon.engine.shader;

import lemon.render.UniformVariable;

public interface UniformTransformationMatrix {
	public UniformVariable getUniformModelMatrix();
	public UniformVariable getUniformViewMatrix();
	public UniformVariable getUniformProjectionMatrix();
}
