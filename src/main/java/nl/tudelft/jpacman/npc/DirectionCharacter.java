package nl.tudelft.jpacman.npc;

import java.util.Map;

import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * DirectionCharacter is an interface that group behavior commen to a player and a Ghost.
 */
public interface DirectionCharacter {
	
	/**
	 * Return the sprites for all Directions
	 * @return the sprites for all Directions.
	 */
	Map<Direction, Sprite> getSprites();
	
	/**
	 * Change the mobility state of an DirectionCharacter
	 * @param newValue the new state
	 */
	void setMobility(boolean newValue);
	
	/**
	 * Change the sprites for all Directions
	 * @param sprites for all Directions.
	 */
	void setSprites(Map<Direction, Sprite> sprites);
	
	/**
	 * Returns whether this DirectionCharacter can be moved or not.
	 * @return true if this DirectionCharacter can be moved
	 */
	boolean getMobility();
}
