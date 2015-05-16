package lemon.engine.shader;

public class TextureShaderProgram extends CoreShaderProgram {
	public TextureShaderProgram(int[] attributes){
		super("shaders/textureVertexShader", "shaders/textureFragmentShader",
				attributes, new String[]{"position", "textureCoords", "normal"});
	}
}
