package lemon.engine;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.lwjgl.BufferUtils;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.ProgramStopEvent;
import lemon.engine.event.Subscribe;
import lemon.engine.glfw.GLFWTime;
import lemon.engine.logger.Logger;
import lemon.engine.math.Location;
import lemon.engine.math.TransformationMatrix;
import lemon.engine.math.Vector;
import lemon.engine.screen.Screen;
import lemon.engine.screen.ScreenChangeEvent;
import lemon.engine.screen.ScreenManager;
import lemon.engine.time.MultiThreads;
import lemon.engine.time.Timer;
import lemon.render.RawModel;
import lemon.render.Shader;
import lemon.render.ShaderProgram;
import lemon.render.Texture;
import lemon.render.UniformVariable;

public class Splash implements Screen, Listener {
	private double targetTime;
	
	private Game game;
	private RawModel model;
	private ShaderProgram program;
	private UniformVariable uniform_transformationMatrix;
	private Location loadingBar;
	private Location currentLoadingBar;
	private Texture loadingTexture;
	private Texture currentLoadingTexture;
	
	private long window;
	
	public static void init(){
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd yyyy hh:mm aaa");
		String date = dateFormat.format(new Date(System.currentTimeMillis()));
		Logger.getWriter().println("Today is "+date);
		Logger.getWriter().println("You have support of OpenGL up to: "+GL11.glGetString(GL11.GL_VERSION));
		Logger.getWriter().println("OpenGL Major Version: "+GL11.glGetInteger(GL30.GL_MAJOR_VERSION));
		Logger.getWriter().println("OpenGL Minor Version: "+GL11.glGetInteger(GL30.GL_MINOR_VERSION));
		Logger.getWriter().println("OpenGL Vendor: "+GL11.glGetString(GL11.GL_VENDOR));
		Logger.getWriter().println("OpenGL Renderer: "+GL11.glGetString(GL11.GL_RENDERER));
		Logger.getWriter().println("LWJGL Version: "+Sys.getVersion());
	}
	public Splash(long window){
		this.window = window;
		targetTime = -1;
		EventManager.registerListener(this);
		game = new Game();
		int[] indices = new int[]{
				0, 1, 3,
				3, 1, 2
		};
		float[] vertices = new float[]{
				-1f, 1f, 0f,
				-1f, -1f, 0f,
				1f, -1f, 0f,
				1f, 1f, 0f
		};
		float[] textureCoords = new float[]{
				0f, 0f,
				0f, 1f,
				1f, 1f,
				1f, 0f
		};
		IntBuffer indicesBuffer = BufferUtils.createIntBuffer(indices.length);
		indicesBuffer.put(indices);
		indicesBuffer.flip();
		FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
		verticesBuffer.put(vertices);
		verticesBuffer.flip();
		FloatBuffer textureCoordsBuffer = BufferUtils.createFloatBuffer(textureCoords.length);
		textureCoordsBuffer.put(textureCoords);
		textureCoordsBuffer.flip();
		model = new RawModel(indices.length);
		model.addAttribute(0, 1);
		GL30.glBindVertexArray(model.getVaoId());
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, model.getVbo());
		model.loadVboIndices(indicesBuffer);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, model.getVbo());
		model.loadVboAttributeData(verticesBuffer, GL15.GL_STATIC_DRAW);
		model.loadVertexAttribPointer(0, 3, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, model.getVbo());
		model.loadVboAttributeData(textureCoordsBuffer, GL15.GL_STATIC_DRAW);
		model.loadVertexAttribPointer(1, 2, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
		program = new ShaderProgram();
		program.addShader(new Shader(GL20.GL_VERTEX_SHADER, getFile("shaders/basicVertexShader")));
		program.addShader(new Shader(GL20.GL_FRAGMENT_SHADER, getFile("shaders/basicFragmentShader")));
		program.bindAttribute(0, "position");
		program.bindAttribute(1, "textureCoords");
		program.link();
		program.validate();
		loadingBar = new Location(new Vector(0f, -0.9f, 0f), new Vector(), new Vector(0, 0.1f, 1));
		currentLoadingBar = new Location(new Vector(0f, -0.7f, 0f), new Vector(), new Vector(0, 0.1f, 1));
		uniform_transformationMatrix = program.getUniformVariable("transformationMatrix");
		//currentLoadingTexture = new Texture(Texture.createBufferedImage(0, 1, 1, 1f));
		BufferedImage image = new BufferedImage(800, 60, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = image.createGraphics();
		g2d.setColor(Color.CYAN);
		g2d.fillRect(0, 0, 800, 60);
		g2d.setColor(Color.BLACK);
		g2d.setFont(new Font("Tahoma", Font.PLAIN, 30));
		g2d.drawString("Loading...", 400-(g2d.getFontMetrics().stringWidth("Loading...")/2), 30);
		currentLoadingTexture = new Texture(image);
		loadingTexture = new Texture(Texture.createBufferedImage(0, 0, 1, 1f));
	}
	private static StringBuilder getFile(String path){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
			StringBuilder builder = new StringBuilder();
			String line;
			while((line=reader.readLine())!=null){
				builder.append(line).append("\n");
			}
			reader.close();
			return builder;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	@Subscribe
	public void onScreenChange(ScreenChangeEvent event){
		if(event.getScreenTo()==this){
			MultiThreads.getInstance().add(new Runnable(){
				@Override
				public void run() {
					game.loadObjFiles();
					targetTime = GLFWTime.getInstance().getTime()+(GLFWTime.getInstance().getTimeResolution()*2D);
				}
			});
		}
	}
	@Override
	public void update(Timer timer){
		if(game.getObjLoader()!=null){
			if(game.getObjLoader().getTotal()!=0){
				float percent = ((float)game.getObjLoader().getProgress())/
						((float)game.getObjLoader().getTotal());
				loadingBar.setX(percent-1f);
				loadingBar.getScale().setX(percent);
				float currentPercent = ((float)game.getObjLoader().getCurrentProgress())/
						((float)game.getObjLoader().getCurrentTotal());
				currentLoadingBar.setX(currentPercent-1f);
				currentLoadingBar.getScale().setX(currentPercent);
				//System.out.println(game.getObjLoader().getCurrent());
			}
		}
	}
	@Override
	public void render(){
		if(targetTime<GLFWTime.getInstance().getTime()&&(targetTime!=-1)){
			game.load(window);
			ScreenManager.pushScreen(game);
			targetTime = -1;
		}
		GL30.glBindVertexArray(model.getVaoId());
		GL20.glUseProgram(program.getId());
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, loadingTexture.getId());
		uniform_transformationMatrix.loadMatrix(TransformationMatrix.get(loadingBar));
		model.render();
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentLoadingTexture.getId());
		uniform_transformationMatrix.loadMatrix(TransformationMatrix.get(currentLoadingBar));
		model.render();
		GL20.glUseProgram(0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL30.glBindVertexArray(0);
	}
	@Subscribe
	public void onCleanUp(ProgramStopEvent event){
		model.cleanUp();
		program.cleanUp();
		loadingTexture.cleanUp();
		currentLoadingTexture.cleanUp();
	}
}
