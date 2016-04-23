package nl.tudelft.jpacman.fruit;

import java.util.List;

import nl.tudelft.jpacman.level.Player;
import nl.tudelft.jpacman.npc.NPC;
import nl.tudelft.jpacman.npc.ghost.Ghost;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * The Potato is a vegetable that will speed-up the ghosts when Pac-Man eat it.
 */
public class Potato extends Fruit{
	
	/**
	 * The list of NPCs active in this game.
	 */
	private List<NPC> npcs;
	
	/**
	 * Create a Pomgranate object
	 * @param Sprite sprite the sprite of this fruit
	 * @param int lifetime the time for which this fruit will remain on the board
	 * @param int effectDuration the time for which the power of this fruit is active.
	 * @param npcs The list of NPCs active in this game.
	 */
	protected Potato(Sprite sprite, int lifetime, int effectDuration, List<NPC> npcs) {
		super(sprite, lifetime, effectDuration);
		this.npcs = npcs;
	}

	@Override
	public void fruitEffect(Player p) {
		Ghost g;
		for(NPC npc: npcs){
			if(npc instanceof Ghost){
				g = (Ghost) npc;
				g.temporaryAcceleration(getEffectDuration());
			}
		}
	}
}
