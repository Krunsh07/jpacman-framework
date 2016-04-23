package nl.tudelft.jpacman.level;

import java.util.Map;

import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * A bridge is a special object that let a Unit to be safe from colliding with
 * the other Units that are on the other side of it. e.g. when one unit is
 * under this bridge and the other one is on the bridge.
 */
public class Bridge extends Unit{
	
	/**
	 * The sprites of this bridge
	 */
	private Map<Direction, Sprite> sprites;

	/**
	 * create a bridge Object
	 * @param Map<Direction, Sprite> spriteMap the sprites of this bridge depending of the direction.
	 */
	public Bridge(Map<Direction, Sprite> spriteMap){
		sprites = spriteMap;
	}
	
	/**
	 * return the sprite of this bridge
	 * @return the Sprite of this bridge
	 */
	public Sprite getSprite(){
		return sprites.get(getDirection());
	}
	
	/**
	 * Return Whether the a direction is parralel to the direction of this bridge.
	 * @return true when both direction are parralel and false if they are perpendicular. 
	 */
	public boolean parralelTo(Direction dir){
		Direction bDir = getDirection();
		if(((dir == Direction.NORTH || dir == Direction.SOUTH) && bDir == Direction.NORTH)
		   || ((dir == Direction.EAST || dir == Direction.WEST) && bDir == Direction.EAST)){
				return true;
			}
			else{
				return false;
			}
	}
	
	/**
	 * enable the effect of this bridge on the unit, when a unit is on a bridge
	 * its position with respect to a bridge (see method setOnBridge in the Unit class)
	 * have to be changed to put the unit on this bridge.
	 * @param Unit unit that have to be set as on a bridge
	 */
	public void effect(Unit unit) {
		Direction uDir = unit.getDirection();
		if(parralelTo(uDir)){
			unit.setOnBridge(true);
		}
		else{
			unit.setOnBridge(false);
		}
	}
	
	/**
	 * It's assumed that the bridge has border, so if you are on a bridge, you can't go out of these border
	 * (when you go in a direction other than the direction of this bridge). When you are under a under a bridge
	 * it does not make sense to be able to be able to go in the elevated places that are bridged by this bridge because
	 * you're under the bridge, to sum up, on a bridge you can only move on the direction of this bridge and when you're
	 * under that bridge, you can only move in the direction perpendicular to the direction of that bridge and this method
	 * returns whether the unit can't go in a direction because a bridge prevent it from going to that direction.
	 * @param Unit unit that have to be set as on a bridge
	 * @param Direction direction the direction where this Unit want to go.
	 * @returns true is the unit can't go in that direction because there a bridge that block it. 
	 */
	public static boolean blockedBybridge(Unit unit, Direction direction){
		Unit u = unit.getSquare().getOccupants().get(0);
		if(u instanceof Bridge){
			Bridge b = (Bridge) u;
			if((!(b.parralelTo(direction)) && unit.isOnBridge())
			  || (b.parralelTo(direction) && !(unit.isOnBridge()))){
				return true;
			}
		}
		return false;
	}
}
