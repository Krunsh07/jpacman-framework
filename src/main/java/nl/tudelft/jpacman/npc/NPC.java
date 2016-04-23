package nl.tudelft.jpacman.npc;

import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Unit;

/**
 * A non-player unit.
 * 
 * @author Jeroen Roosen 
 */
public abstract class NPC extends Unit {
	
	/**
	 * Whether the NPC is speed-up
	 */
	private boolean acceleration = false;

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
	 * Set the acceleration state of this NPC.
	 * @param value true to make the NPC move faster, false otherwise.
	 */
	public void setAcceleration(boolean value) {
		acceleration = value;
	}
	
	/**
	 * Returns whether the NPC is speed-up
	 * @return true if the speed of this NPC is accelerated.
	 */
	public boolean getAcceleration() {
		return acceleration;
	}
}
