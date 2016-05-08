package nl.tudelft.jpacman.level;

import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.sprite.Sprite;

import java.util.List;

import nl.tudelft.jpacman.board.Square;

/**
 * A teleport is a special object that will teleport
 * pacman instantly to another part of the board.
 */
public class Teleport extends Unit {

	/**
	 * The sprite of this unit.
	 */
	private final Sprite image;
	
	/**
	 * The square where this teleport will put the player
	 */
	private Square reference;
	
	/**
	 * Creates a new hole.
	 * @param sprite The sprite of this hole.
	 */
	public Teleport(Sprite sprite) {
		this.image = sprite;
	}

	/**
	 * Return the sprite
	 * @return The sprite
     */
	@Override
	public Sprite getSprite() {
		return image;
	}
	
	/**
	 * returns the square pointed by this teleport.
	 * @return the square pointed by this teleport.
	 */
	public Square getReference(){
		return reference;
	}
	
	/**
	 * change the reference pointed by this teleport.
	 * @param ref the new square pointed by this teleport.
	 */
	public void setReference(Square ref){
		reference = ref;
	}
	
	/**
	 * teleport the player to the square pointed by this telport if this square
	 * is accessible to the player, this will also enter in collisions with the
	 * occupants of this square except telports to avoid a infinite loop.
	 * @param p the player that have to be moved
	 * @param pc the object that manage collision in this game
	 */
	public void effect(Player p, PlayerCollisions pc) {
		if(reference.isAccessibleTo(p))
		{
			p.occupy(reference);
			List<Unit> occupants = reference.getOccupants();
			for (Unit occupant : occupants) {
				if(!(occupant instanceof Teleport))
				{
					if(occupant instanceof Bridge)
					{
						p.setDirection(occupant.getDirection());
					}
					pc.collide(p, occupant);
				}
			}
		}
	}
}
