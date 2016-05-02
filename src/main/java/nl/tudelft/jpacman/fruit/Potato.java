package nl.tudelft.jpacman.fruit;

import java.util.List;
import java.util.Set;

import nl.tudelft.jpacman.level.Level;
import nl.tudelft.jpacman.level.Player;
import nl.tudelft.jpacman.npc.NPC;
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
	 * @param Sprite sprite the sprite of this fruit
	 * @param int lifetime the time for which this fruit will remain on the board
	 * @param int effectDuration the time for which the power of this fruit is active.
	 * @param npcs The list of NPCs active in this game.
	 */
	protected Potato(Sprite sprite, Level l, int lifetime, int effectDuration) {
		super(sprite, lifetime, effectDuration);
		level = l;
	}

	@Override
	public void fruitEffect(Player p) {
		Set<Ghost> ghosts = level.getGhosts().keySet();
		for(Ghost ghost: ghosts){
			if(!ghost.getFearedMode())
				ghost.temporaryAcceleration(getEffectDuration());
		}
	}
}
