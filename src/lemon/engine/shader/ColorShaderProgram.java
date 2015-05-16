package lemon.engine.shader;

public class ColorShaderProgram extends CoreShaderProgram {
	public ColorShaderProgram(int[] attributes) {
		super("shaders/colorVertexShader", "shaders/colorFragmentShader",
				attributes, new String[]{"position", "color", "normal"});
	}
}
