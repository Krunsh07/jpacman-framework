package nl.tudelft.jpacman.fruit;

import java.util.List;

import nl.tudelft.jpacman.level.Player;
import nl.tudelft.jpacman.npc.NPC;
import nl.tudelft.jpacman.npc.ghost.Ghost;
import nl.tudelft.jpacman.npc.ghost.Navigation;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * The Pomgranate is a fruit that when eaten by Pac-Man instantly kill ghosts that are at a distance of four square away from it.
 */
public class Pomgranate extends Fruit {
	
	/**
	 *  The list of NPCs active in this game at the moment when this fruit was created.
	 */
	private List<NPC> npcs;
	
	/**
	 * Create a Pomgranate object
	 * @param Sprite sprite the sprite of this fruit
	 * @param int lifetime the time for which this fruit will remain on the board
	 * @param int effectDuration the time for which the power of this fruit is active.
	 * @param npcs The list of NPCs active in this game.
	 */
	protected Pomgranate(Sprite sprite, int lifetime, int effectDuration, List<NPC> npcs) {
		super(sprite, lifetime, effectDuration);
		this.npcs = npcs;
	}

	@Override
	public void fruitEffect(Player p) {
		Ghost g;
		for(NPC npc: npcs){
			if(npc instanceof Ghost){
				g = (Ghost) npc;
				if(npc.getSquare() != null && Navigation.shortestPath(p.getSquare(), g.getSquare(), this).size() <= 4){
					g.setExplode(true);
				}
			}
		}
	}
}
