package lemon.engine;

import java.nio.ByteBuffer;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.system.MemoryUtil;

import lemon.engine.event.EventManager;
import lemon.engine.event.LemonProgramStopEvent;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.glfw.GLFWInput;
import lemon.engine.glfw.GLFWTime;
import lemon.engine.input.FileDropEvent;
import lemon.engine.input.WindowCloseEvent;
import lemon.engine.logger.Logger;
import lemon.engine.logger.LoggerEvent;
import lemon.engine.screen.ScreenManager;
import lemon.engine.time.AssumptionControl;
import lemon.engine.time.MultiThreads;
import lemon.engine.time.Timer;

public class GameLauncher {
	private static GLFWInput glfwInput;
	private static GLFWErrorCallback errorCallback;
	private static Timer renderTimer;
	private static Timer updateTimer;
	private static boolean closeRequested;
	private static long window;
	
	public static void main(String[] args){
		try{
			init();
			run();
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			close();
		}
	}
	public static void init(){
		EventManager.registerListener(new Listener(){
			@Subscribe
			public void onLog(LoggerEvent event){
				System.out.print(event.getLoggerItem().toString());
			}
		});
		closeRequested = false;
		renderTimer = new Timer(GLFWTime.getInstance(), AssumptionControl.getInstance());
		updateTimer = new Timer(GLFWTime.getInstance(), AssumptionControl.getInstance());
		GLFW.glfwSetErrorCallback(errorCallback = Callbacks.errorCallbackPrint());
		if(GLFW.glfwInit()!=GL11.GL_TRUE){
			Logger.getWriter().println("Unable to initialize GLFW");
		}
		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GL11.GL_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_FALSE);
		window = GLFW.glfwCreateWindow(800, 600, "Game", MemoryUtil.NULL, MemoryUtil.NULL);
		if(window==MemoryUtil.NULL){
			Logger.getWriter().println("Unable to create GLFW window");
		}
		ByteBuffer vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
		GLFW.glfwSetWindowPos(window, (GLFWvidmode.width(vidmode)-800)/2, (GLFWvidmode.height(vidmode)-600)/2);
		//GLFW.glfwSetWindowPos(window, 1500, 200);
		glfwInput = new GLFWInput(window);
		GLFW.glfwMakeContextCurrent(window);
		//GLFW.glfwSwapInterval(1); //Enable vSync
		GLFW.glfwShowWindow(window);
		EventManager.registerListener(new Listener(){
			@Subscribe
			public void onWindowClose(WindowCloseEvent event){
				closeRequested = true;
			}
			@Subscribe
			public void onDrop(FileDropEvent event){
				for(int i=0;i<event.getFiles().length;++i){
					System.out.format("%d: %s%n", i + 1, event.getFiles()[i]);
				}
			}
		});
		GLContext.createFromCurrent();
		Splash.init();
	}
	public static void run(){
		MultiThreads.setInstance(new MultiThreads(GLFWTime.getInstance(), AssumptionControl.getInstance()));
		ScreenManager.pushScreen(new Splash(window));
		MultiThreads.getInstance().add(new Runnable(){
			@Override
			public void run() {
				while(!closeRequested){
					ScreenManager.getCurrentScreen().update(updateTimer);
					updateTimer.sync(60);
				}
			}
		});
		MultiThreads.getInstance().setMainRunnable(new Runnable(){
			@Override
			public void run() {
				while(!closeRequested){
					int error = GL11.glGetError();
					while(error!=GL11.GL_NO_ERROR){
						System.out.println(error);
						error = GL11.glGetError();
					}
					GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
					ScreenManager.getCurrentScreen().render();
					GLFW.glfwSwapBuffers(window);
					GLFW.glfwPollEvents();
					renderTimer.sync(0);
					GLFW.glfwSetWindowTitle(window, updateTimer.getCurrentFps()+" - "+renderTimer.getCurrentFps());
				}
			}
		});
		MultiThreads.getInstance().start();
	}
	public static void close(){
		EventManager.callListeners(new LemonProgramStopEvent());
		GLFW.glfwDestroyWindow(window);
		glfwInput.releaseAll();
		GLFW.glfwTerminate();
		errorCallback.release();
	}
}
