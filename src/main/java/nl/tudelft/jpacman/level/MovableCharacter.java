package nl.tudelft.jpacman.level;

import java.util.Map;

import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * This class is the model for a character any character that can move in the
 * game whether controlled by the computer or the player.
 */
public abstract class MovableCharacter extends Unit {
	
	/**
	 * Whether the Character move faster than it's regular speed.
	 */
	private boolean acceleration;
	
	/**
	 * Whether this unit can be move or not.
	 */
	private boolean movable;
	
	/**
	 * The animations for every direction.
	 */
	private Map<Direction, Sprite> sprites;

	/**
	 * The time that should be taken between moves.
	 * 
	 * @return The suggested delay between moves in milliseconds.
	 */
	public abstract long getInterval();
	
	/**
	 * Calculates the next move for this unit and returns the direction to move
	 * in.
	 * 
	 * @return The direction to move in, or <code>null</code> if no move could
	 *         be devised.
	 */
	public abstract Direction nextMove();
	
	/**
	 * Set the acceleration state of this character.
	 * @param value true to make the character move faster than it's regular
	 *        speed, false otherwise.
	 */
	public void setAcceleration(boolean value) {
		acceleration = value;
	}
	
	/**
	 * Returns true if the character move faster than it's regular speed.
	 * @return true if the character move faster than it's regular speed.
	 */
	public boolean getAcceleration() {
		return acceleration;
	}
	
	/**
	 * Return the sprites for all Directions
	 * @return the sprites for all Directions.
	 */
	public Map<Direction, Sprite> getSprites() {
		return sprites;
	}
	
	/**
	 * Change the sprites for all Directions
	 * @param sprites for all Directions.
	 */
	public void setSprites(Map<Direction, Sprite> sprites) {
		this.sprites = sprites;
	}
	
	/**
	 * Allow or prevent a character to make a move.
	 * @param newValue true allow the character to move and false will prevent
	 *        this character from moving.
	 */
	public void setMovable(boolean newValue) {
		movable = newValue;
	}

	/**
	 * Return true if this character is allowed to move, false otherwise.
	 * @return true if this character is allowed to move, false otherwise.
	 */
	public boolean isMovable() {
		return movable;
	}
}
