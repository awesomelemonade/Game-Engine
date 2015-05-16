package lemon.engine.glfw;

import org.lwjgl.glfw.GLFW;

import lemon.engine.time.Timable;

public class GLFWTime implements Timable {
	private static final GLFWTime instance;
	static{
		instance = new GLFWTime();
	}
	private GLFWTime(){}
	@Override
	public double getTime() {
		return GLFW.glfwGetTime();
	}
	@Override
	public double getTimeResolution() {
		return 1;
	}
	public static GLFWTime getInstance(){
		return instance;
	}
}
