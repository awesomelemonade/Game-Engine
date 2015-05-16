package lemon.engine;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import lemon.engine.entity.Entity;
import lemon.engine.entity.EntityModel;
import lemon.engine.entity.TestEntity;
import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.ProgramStopEvent;
import lemon.engine.event.Subscribe;
import lemon.engine.game.Camera;
import lemon.engine.input.CursorPositionEvent;
import lemon.engine.input.KeyEvent;
import lemon.engine.input.MouseButtonEvent;
import lemon.engine.loader.Loader;
import lemon.engine.math.Location;
import lemon.engine.math.TransformationMatrix;
import lemon.engine.math.Vector;
import lemon.engine.screen.Screen;
import lemon.engine.screen.ScreenChangeEvent;
import lemon.engine.shader.ColorShaderProgram;
import lemon.engine.shader.TextureShaderProgram;
import lemon.engine.text.Text;
import lemon.engine.time.Timer;
import lemon.render.ModelData;
import lemon.render.ObjLoader;
import lemon.render.RawModel;
import lemon.render.ShaderProgram;
import lemon.render.Texture;
import lemon.render.UniformVariable;

public class Game implements Screen, Listener {
	private Loader<String[], String, ModelData> objLoader;
	private Camera camera;
	private Entity entity;
	private EntityModel model;
	private ShaderProgram colorShaderProgram;
	private ShaderProgram textureShaderProgram;
	private UniformVariable uniform_shineDamper;
	private UniformVariable uniform_reflectivity;
	private UniformVariable uniform_lightColor;
	private UniformVariable uniform_lightPosition;
	private Texture texture;
	
	private RawModel textModel;
	private Location textLocation;
	
	private PlayerControls<Integer, Integer> controls;
	
	private double lastMouseX;
	private double lastMouseY;
	private double mouseX;
	private double mouseY;
	
	public void loadObjFiles(){
		objLoader = new ObjLoader();
		//String[] file = getFile("res/dragon.obj").toString().split("\n");
		objLoader.add(getFile("res/stall.obj").toString().split("\n"));
		objLoader.add(getFile("res/dragon.obj").toString().split("\n"));
		//objLoader.add(file);
		objLoader.load();
	}
	public Loader<String[], String, ModelData> getObjLoader(){
		return objLoader;
	}
	public void load(long window){
		EventManager.registerListener(this);
		Text text = new Text(new Font("Tahoma", Font.PLAIN, 128), "Skittles");
		textLocation = new Location(new Vector(0f, 12f, -25f), new Vector(0f, 0f, 0f), new Vector(0.02f, 0.02f, 0.2f));
		textModel = new RawModel(text.getModelData().getIndices().size());
		textModel.addAttribute(0, 1, 2, 3);
		GL30.glBindVertexArray(textModel.getVaoId());
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, textModel.getVbo());
		textModel.loadVboIndices(text.getModelData().getIndicesBuffer());
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, textModel.getVbo());
		textModel.loadVboAttributeData(text.getModelData().getBuffer(), GL15.GL_STATIC_DRAW);
		textModel.loadVertexAttribPointer(0, 3, 8*4, 0);
		textModel.loadVertexAttribPointer(1, 2, 8*4, 3*4);
		textModel.loadVertexAttribPointer(2, 3, 8*4, 5*4);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, textModel.getVbo());
		float[] colors = new float[text.getModelData().getSize()*4];
		for(int i=0;i<colors.length;i+=4){
			colors[i] = (float) Math.random();
			colors[i+1] = (float) Math.random();
			colors[i+2] = (float) Math.random();
			colors[i+3] = 1f;
		}
		FloatBuffer colorsData = BufferUtils.createFloatBuffer(colors.length);
		colorsData.put(colors);
		colorsData.flip();
		textModel.loadVboAttributeData(colorsData, GL15.GL_STATIC_DRAW);
		textModel.loadVertexAttribPointer(3, 4, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
		model = new EntityModel(objLoader.get());
		model.addAttribute(3);
		List<Float> vertices = model.getData().getDataByAttribute(0);
		float[] array = new float[model.getData().getSize()*4];
		for(int i=0;i<array.length;i+=4){
			array[i] = vertices.get(i/4*3)+2.5f;
			array[i+1] = vertices.get(i/4*3+1)-0.5f;
			array[i+2] = vertices.get(i/4*3+2)+0.5f;
			array[i+3] = 1f;
			//array[i+3] = (float) Math.random();
		}
		FloatBuffer data = BufferUtils.createFloatBuffer(array.length);
		data.put(array);
		data.flip();
		GL30.glBindVertexArray(model.getVaoId());
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, model.getVbo());
		model.loadVboAttributeData(data, GL15.GL_STATIC_DRAW);
		model.loadVertexAttribPointer(3, 4, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
		textureShaderProgram = new TextureShaderProgram(new int[]{0, 1, 2});
		colorShaderProgram = new ColorShaderProgram(new int[]{0, 3, 2});
		camera = new Camera(new Location());
		entity = new TestEntity(new Location(new Vector(0f, -2f, -25f), new Vector(0f, 0f, 0f), new Vector(1f, 1f, 1f)));
		uniform_shineDamper = program.getUniformVariable("shineDamper");
		uniform_reflectivity = program.getUniformVariable("reflectivity");
		uniform_lightPosition = program.getUniformVariable("lightPosition");
		uniform_lightColor = program.getUniformVariable("lightColor");
		GL20.glUseProgram(program.getId());
		camera.loadProjectionMatrix(60f, getAspectRatio(window), 0.1f, 100f);
		uniform_lightPosition.loadVector(new Vector(0, 0, -5));
		uniform_lightColor.loadVector(new Vector(1, 1, 1));
		GL20.glUseProgram(0);
		/*try {
			texture = new Texture(ImageIO.read(new File("res/Eye.jpg")));
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		try {
			texture = new Texture(ImageIO.read(new File("res/stallTexture.png")), 10, 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		controls = new PlayerControls<Integer, Integer>();
		controls.bindKey(GLFW.GLFW_MOUSE_BUTTON_LEFT, GLFW.GLFW_MOUSE_BUTTON_LEFT);
		controls.bindKey(GLFW.GLFW_KEY_A, GLFW.GLFW_KEY_A);
		controls.bindKey(GLFW.GLFW_KEY_D, GLFW.GLFW_KEY_D);
		controls.bindKey(GLFW.GLFW_KEY_W, GLFW.GLFW_KEY_W);
		controls.bindKey(GLFW.GLFW_KEY_S, GLFW.GLFW_KEY_S);
		controls.bindKey(GLFW.GLFW_KEY_SPACE, GLFW.GLFW_KEY_SPACE);
		controls.bindKey(GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_LEFT_SHIFT);
	}
	private static float getAspectRatio(long window){
		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowSize(window, width, height);
		return ((float)width.get())/((float)height.get());
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
			GL11.glClearColor(0.6f, 0.6f, 1, 1);
		}
		if(event.getScreenFrom()==this){
			GL11.glClearColor(0, 0, 0, 1);
		}
	}
	private static final double PLAYER_SPEED = 4D;
	private static final double MOUSE_SENSITIVITY = 0.2D;
	@Override
	public void update(Timer timer){
		entity.getLocation().setYaw((float) (entity.getLocation().getYaw()+50f*timer.getTimePassed()));
		textLocation.setYaw((float) (entity.getLocation().getYaw()+50f*timer.getTimePassed()));
		if(controls.hasStates()){
			float angle = (camera.getLocation().getYaw()+90)*(((float)Math.PI)/180f);
			float sin = (float)Math.sin(angle);
			float cos = (float)Math.cos(angle);
			if(controls.getState(GLFW.GLFW_KEY_A)){
				camera.getLocation().setX(camera.getLocation().getX()-((float)(PLAYER_SPEED*timer.getTimePassed()))*sin);
				camera.getLocation().setZ(camera.getLocation().getZ()-((float)(PLAYER_SPEED*timer.getTimePassed()))*cos);
			}
			if(controls.getState(GLFW.GLFW_KEY_D)){
				camera.getLocation().setX(camera.getLocation().getX()+((float)(PLAYER_SPEED*timer.getTimePassed()))*sin);
				camera.getLocation().setZ(camera.getLocation().getZ()+((float)(PLAYER_SPEED*timer.getTimePassed()))*cos);
			}
			angle = camera.getLocation().getYaw()*(((float)Math.PI)/180f);
			sin = (float)Math.sin(angle);
			cos = (float)Math.cos(angle);
			if(controls.getState(GLFW.GLFW_KEY_W)){
				camera.getLocation().setX(camera.getLocation().getX()-((float)(PLAYER_SPEED*timer.getTimePassed()))*sin);
				camera.getLocation().setZ(camera.getLocation().getZ()-((float)(PLAYER_SPEED*timer.getTimePassed()))*cos);
			}
			if(controls.getState(GLFW.GLFW_KEY_S)){
				camera.getLocation().setX(camera.getLocation().getX()+((float)(PLAYER_SPEED*timer.getTimePassed()))*sin);
				camera.getLocation().setZ(camera.getLocation().getZ()+((float)(PLAYER_SPEED*timer.getTimePassed()))*cos);
			}
			if(controls.getState(GLFW.GLFW_KEY_SPACE)){
				camera.getLocation().setY(camera.getLocation().getY()+((float)(PLAYER_SPEED*timer.getTimePassed())));
			}
			if(controls.getState(GLFW.GLFW_KEY_LEFT_SHIFT)){
				camera.getLocation().setY(camera.getLocation().getY()-((float)(PLAYER_SPEED*timer.getTimePassed())));
			}
		}
	}
	@Override
	public void render(){
		//GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		//GL11.glEnable(GL11.GL_CULL_FACE);
		//GL11.glCullFace(GL11.GL_BACK);
		
		/*if(model.getIntersection(camera.getLocation().getPosition(),
				camera.getLocation().getRotation()).isEmpty()){
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		}else{
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		}*/
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getId());
		
		//GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureBank[(int)(Math.random()*textureBank.length)].getId());
		
		GL20.glUseProgram(program.getId());
		camera.loadModelMatrix(entity.getLocation());
		camera.loadViewMatrix();
		uniform_shineDamper.loadFloat(texture.getShineDamper());
		uniform_reflectivity.loadFloat(texture.getReflectivity());
		GL30.glBindVertexArray(model.getVaoId());
		model.render();
		GL30.glBindVertexArray(0);
		camera.loadModelMatrix(textLocation);
		GL30.glBindVertexArray(textModel.getVaoId());
		textModel.render();
		GL30.glBindVertexArray(0);
		GL20.glUseProgram(0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		//GL11.glDisable(GL11.GL_CULL_FACE);
	}
	@Subscribe
	public void onKey(KeyEvent event){
		if(event.getAction()==GLFW.GLFW_PRESS){
			controls.setKeyState(event.getKey(), true);
		}
		if(event.getAction()==GLFW.GLFW_RELEASE){
			controls.setKeyState(event.getKey(), false);
		}
	}
	@Subscribe
	public void onMouse(MouseButtonEvent event){
		if(event.getAction()==GLFW.GLFW_PRESS){
			controls.setKeyState(event.getButton(), true);
		}
		if(event.getAction()==GLFW.GLFW_RELEASE){
			controls.setKeyState(event.getButton(), false);
		}
	}
	@Subscribe
	public void onMousePosition(CursorPositionEvent event){
		lastMouseX = mouseX;
		lastMouseY = mouseY;
		mouseX = event.getX();
		mouseY = event.getY();
		if(controls.getState(GLFW.GLFW_MOUSE_BUTTON_1)){
			camera.getLocation().setYaw((float) (camera.getLocation().getYaw()-(mouseX-lastMouseX)*MOUSE_SENSITIVITY));
			camera.getLocation().setPitch((float) (camera.getLocation().getPitch()-(mouseY-lastMouseY)*MOUSE_SENSITIVITY));
			if(camera.getLocation().getPitch()<-90){
				camera.getLocation().setPitch(-90);
			}
			if(camera.getLocation().getPitch()>90){
				camera.getLocation().setPitch(90);
			}
		}
	}
	@Subscribe
	public void onCleanUp(ProgramStopEvent event){
		model.cleanUp();
		textureShaderProgram.cleanUp();
		colorShaderProgram.cleanUp();
		texture.cleanUp();
	}
}
