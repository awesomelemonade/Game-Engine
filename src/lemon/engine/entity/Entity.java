package lemon.engine.entity;

import lemon.engine.math.Location;

public interface Entity {
	public Location getLocation();
	public EntityModel getModel();
}
