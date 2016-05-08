package nl.tudelft.jpacman.fruit;

import java.util.Set;

import nl.tudelft.jpacman.level.Level;
import nl.tudelft.jpacman.level.Player;
import nl.tudelft.jpacman.npc.ghost.Ghost;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * The Potato is a vegetable that will speed-up the ghosts when Pac-Man eat it.
 */
public class Potato extends Fruit{
	
	/**
	 * The level where this potato appeared.
	 */
	private Level level;
	
	/**
	 * Create a Pomgranate object
	 * @param sprite the sprite of this fruit
	 * @param lifetime the time for which this fruit will remain on the board
	 * @param effectDuration the time for which the power of this fruit is active.
	 * @param l the level where this potato appeared.
	 */
	protected Potato(Sprite sprite, int lifetime, int effectDuration, Level l) {
		super(sprite, lifetime, effectDuration);
		level = l;
	}

	@Override
	public void fruitEffect(Player p) {
		Set<Ghost> ghosts = level.getGhosts().keySet();
		for(Ghost ghost: ghosts){
			if(!ghost.getFearedMode()){
				ghost.temporaryAcceleration(getEffectDuration());
			}
		}
	}
}
