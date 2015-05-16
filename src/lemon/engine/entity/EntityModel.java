package lemon.engine.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import lemon.engine.math.MathUtil;
import lemon.engine.math.Vector;
import lemon.render.ModelData;
import lemon.render.RawModel;

public class EntityModel extends RawModel {
	private ModelData data;
	public EntityModel(ModelData data) {
		super(data.getIndices().size());
		GL30.glBindVertexArray(getVaoId());
		this.addAttribute(0, 1, 2);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, getVbo());
		this.loadVboIndices(data.getIndicesBuffer());
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, getVbo());
		this.loadVboAttributeData(data.getBuffer(), GL15.GL_STATIC_DRAW);
		this.loadVertexAttribPointer(0, 3, 8*4, 0);
		this.loadVertexAttribPointer(1, 2, 8*4, 3*4);
		this.loadVertexAttribPointer(2, 3, 8*4, 5*4);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
		this.data = data;
	}
	//TODO If no intersection, return null
	public List<Float> getIntersection(Vector origin, Vector direction){
		List<Float> intersections = new ArrayList<Float>();
		List<Float> vertices = data.getDataByAttribute(0);
		for(int i=0;i<vertices.size();i+=9){
			float intersection = MathUtil.getIntersection(origin, direction, 
					new Vector(vertices.get(i), vertices.get(i+1), vertices.get(i+2)),
					new Vector(vertices.get(i+3), vertices.get(i+4), vertices.get(i+5)),
					new Vector(vertices.get(i+6), vertices.get(i+7), vertices.get(i+8)));
			if(intersection!=-1){
				intersections.add(intersection);
			}
		}
		Collections.sort(intersections);
		return intersections;
	}
	public ModelData getData(){
		return data;
	}
}
