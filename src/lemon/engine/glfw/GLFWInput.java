package lemon.engine.glfw;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWCharModsCallback;
import org.lwjgl.glfw.GLFWCursorEnterCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWDropCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWWindowCloseCallback;
import org.lwjgl.glfw.GLFWWindowFocusCallback;
import org.lwjgl.glfw.GLFWWindowIconifyCallback;
import org.lwjgl.glfw.GLFWWindowPosCallback;
import org.lwjgl.glfw.GLFWWindowRefreshCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL11;

import lemon.engine.event.EventManager;

public class GLFWInput {
	private final GLFWCharModsCallback charModsCallback;
	private final GLFWCursorEnterCallback cursorEnterCallback;
	private final GLFWCursorPosCallback cursorPosCallback;
	private final GLFWDropCallback dropCallback;
	private final GLFWFramebufferSizeCallback framebufferSizeCallback;
	private final GLFWKeyCallback keyCallback;
	private final GLFWMouseButtonCallback mouseButtonCallback;
	private final GLFWScrollCallback scrollCallback;
	private final GLFWWindowCloseCallback windowCloseCallback;
	private final GLFWWindowFocusCallback windowFocusCallback;
	private final GLFWWindowIconifyCallback windowIconifyCallback;
	private final GLFWWindowPosCallback windowPosCallback;
	private final GLFWWindowRefreshCallback windowRefreshCallback;
	private final GLFWWindowSizeCallback windowSizeCallback;
	
	public GLFWInput(long window){
		Callbacks.glfwSetCallback(window, charModsCallback = new GLFWCharModsCallback(){
			@Override
			public void invoke(long window, int codepoint, int mods){
				EventManager.callListeners(new GLFWCharacterEvent(window, codepoint, mods));
			}
		});
		Callbacks.glfwSetCallback(window, cursorEnterCallback = new GLFWCursorEnterCallback(){
			@Override
			public void invoke(long window, int entered){
				EventManager.callListeners(new GLFWCursorEnterEvent(window, entered==GL11.GL_TRUE));
			}
		});
		Callbacks.glfwSetCallback(window, cursorPosCallback = new GLFWCursorPosCallback(){
			@Override
			public void invoke(long window, double xPos, double yPos){
				EventManager.callListeners(new GLFWCursorPositionEvent(window, xPos, yPos));
			}
		});
		Callbacks.glfwSetCallback(window, dropCallback = new GLFWDropCallback(){
			@Override
			public void invoke(long window, int count, long names){
				EventManager.callListeners(new GLFWFileDropEvent(window, count, names));
			}
		});
		Callbacks.glfwSetCallback(window, framebufferSizeCallback = new GLFWFramebufferSizeCallback(){
			@Override
			public void invoke(long window, int width, int height){
				EventManager.callListeners(new GLFWFrameBufferSizeEvent(window, width, height));
			}
		});
		Callbacks.glfwSetCallback(window, keyCallback = new GLFWKeyCallback(){
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods){
				EventManager.callListeners(new GLFWKeyEvent(window, key, scancode, action, mods));
			}
		});
		Callbacks.glfwSetCallback(window, mouseButtonCallback = new GLFWMouseButtonCallback(){
			@Override
			public void invoke(long window, int button, int action, int mods){
				EventManager.callListeners(new GLFWMouseButtonEvent(window, button, action, mods));
			}
		});
		Callbacks.glfwSetCallback(window, scrollCallback = new GLFWScrollCallback(){
			@Override
			public void invoke(long window, double xOffSet, double yOffSet){
				EventManager.callListeners(new GLFWMouseScrollEvent(window, xOffSet, yOffSet));
			}
		});
		Callbacks.glfwSetCallback(window, windowCloseCallback = new GLFWWindowCloseCallback(){
			@Override
			public void invoke(long window){
				EventManager.callListeners(new GLFWWindowCloseEvent(window));
			}
		});
		Callbacks.glfwSetCallback(window, windowFocusCallback = new GLFWWindowFocusCallback(){
			@Override
			public void invoke(long window, int focused){
				EventManager.callListeners(new GLFWWindowFocusEvent(window, focused==GL11.GL_TRUE));
			}
		});
		Callbacks.glfwSetCallback(window, windowIconifyCallback = new GLFWWindowIconifyCallback(){
			@Override
			public void invoke(long window, int iconified){
				EventManager.callListeners(new GLFWWindowMinimizeEvent(window, iconified==GL11.GL_TRUE));
			}
		});
		Callbacks.glfwSetCallback(window, windowPosCallback = new GLFWWindowPosCallback(){
			@Override
			public void invoke(long window, int xPos, int yPos){
				EventManager.callListeners(new GLFWWindowMoveEvent(window, xPos, yPos));
			}
		});
		Callbacks.glfwSetCallback(window, windowRefreshCallback = new GLFWWindowRefreshCallback(){
			@Override
			public void invoke(long window){
				EventManager.callListeners(new GLFWWindowRefreshEvent(window));
			}
		});
		Callbacks.glfwSetCallback(window, windowSizeCallback = new GLFWWindowSizeCallback(){
			@Override
			public void invoke(long window, int width, int height){
				EventManager.callListeners(new GLFWWindowSizeEvent(window, width, height));
			}
		});
	}
	public void releaseAll(){
		charModsCallback.release();
		cursorEnterCallback.release();
		cursorPosCallback.release();
		dropCallback.release();
		framebufferSizeCallback.release();
		keyCallback.release();
		mouseButtonCallback.release();
		scrollCallback.release();
		windowCloseCallback.release();
		windowFocusCallback.release();
		windowIconifyCallback.release();
		windowPosCallback.release();
		windowRefreshCallback.release();
		windowSizeCallback.release();
	}
	public GLFWCharModsCallback getCharModsCallback() {
		return charModsCallback;
	}
	public GLFWCursorEnterCallback getCursorEnterCallback() {
		return cursorEnterCallback;
	}
	public GLFWCursorPosCallback getCursorPosCallback() {
		return cursorPosCallback;
	}
	public GLFWDropCallback getDropCallback() {
		return dropCallback;
	}
	public GLFWFramebufferSizeCallback getFramebufferSizeCallback() {
		return framebufferSizeCallback;
	}
	public GLFWKeyCallback getKeyCallback() {
		return keyCallback;
	}
	public GLFWMouseButtonCallback getMouseButtonCallback() {
		return mouseButtonCallback;
	}
	public GLFWScrollCallback getScrollCallback() {
		return scrollCallback;
	}
	public GLFWWindowCloseCallback getWindowCloseCallback() {
		return windowCloseCallback;
	}
	public GLFWWindowFocusCallback getWindowFocusCallback() {
		return windowFocusCallback;
	}
	public GLFWWindowIconifyCallback getWindowIconifyCallback() {
		return windowIconifyCallback;
	}
	public GLFWWindowPosCallback getWindowPosCallback() {
		return windowPosCallback;
	}
	public GLFWWindowRefreshCallback getWindowRefreshCallback() {
		return windowRefreshCallback;
	}
	public GLFWWindowSizeCallback getWindowSizeCallback() {
		return windowSizeCallback;
	}
}