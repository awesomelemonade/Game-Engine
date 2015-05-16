package lemon.engine.entity;

import lemon.engine.math.Location;

public class TestEntity implements Entity {
	private Location location;
	
	public TestEntity(Location location){
		this.location = location;
	}
	@Override
	public Location getLocation(){
		return location;
	}
	@Override
	public EntityModel getModel(){
		return null;
	}
}
