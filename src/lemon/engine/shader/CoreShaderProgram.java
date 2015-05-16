package lemon.engine.shader;

import lemon.render.Shader;
import lemon.render.ShaderProgram;
import lemon.render.UniformVariable;

import org.lwjgl.opengl.GL20;

public class CoreShaderProgram extends ShaderProgram implements UniformTransformationMatrix {
	private UniformVariable uniform_modelMatrix;
	private UniformVariable uniform_viewMatrix;
	private UniformVariable uniform_projectionMatrix;
	public CoreShaderProgram(String vertexShader, String fragmentShader, int[] attributes, String[] variableNames){
		this.addShader(new Shader(GL20.GL_VERTEX_SHADER, vertexShader));
		this.addShader(new Shader(GL20.GL_FRAGMENT_SHADER, fragmentShader));
		for(int i=0;i<attributes.length&&i<variableNames.length;++i){
			this.bindAttribute(attributes[i], variableNames[i]);
		}
		this.link();
		this.validate();
		uniform_modelMatrix = this.getUniformVariable("modelMatrix");
		uniform_viewMatrix = this.getUniformVariable("viewMatrix");
		uniform_projectionMatrix = this.getUniformVariable("projectionMatrix");
	}
	@Override
	public UniformVariable getUniformModelMatrix() {
		return uniform_modelMatrix;
	}
	@Override
	public UniformVariable getUniformViewMatrix() {
		return uniform_viewMatrix;
	}
	@Override
	public UniformVariable getUniformProjectionMatrix() {
		return uniform_projectionMatrix;
	}
}
