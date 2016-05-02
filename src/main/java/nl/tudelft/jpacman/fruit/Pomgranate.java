package nl.tudelft.jpacman.fruit;

import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import nl.tudelft.jpacman.board.PassThroughWall;
import nl.tudelft.jpacman.level.Level;
import nl.tudelft.jpacman.level.Player;
import nl.tudelft.jpacman.level.PlayerCollisions;
import nl.tudelft.jpacman.npc.NPC;
import nl.tudelft.jpacman.npc.ghost.Ghost;
import nl.tudelft.jpacman.npc.ghost.Navigation;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * The Pomgranate is a fruit that when eaten by Pac-Man instantly kill ghosts that are at a distance of four square away from it.
 */
public class Pomgranate extends Fruit {
	
	/**
	 *  The level where this fruit was created.
	 */
	private Level level;
	
	/**
	 * Create a Pomgranate object
	 * @param Sprite sprite the sprite of this fruit
	 * @param int lifetime the time for which this fruit will remain on the board
	 * @param int effectDuration the time for which the power of this fruit is active.
	 * @param npcs The list of NPCs active in this game.
	 */
	protected Pomgranate(Sprite sprite, int lifetime, int effectDuration, Level l) {
		super(sprite, lifetime, effectDuration);
		level = l;
	}

	@Override
	public void fruitEffect(Player p) {
		Set<Ghost> ghosts = level.getGhosts().keySet();
		for(Ghost ghost: ghosts){
			if(ghost.getSquare() != null && Navigation.shortestPath(p.getSquare(), ghost.getSquare(), new PassThroughWall()).size() <= 4){
				if(!(ghost.hasExploded())) {
					ghost.setExplode(true);
					TimerTask timerTask = new TimerTask() {
					    public void run() {
					    	ghost.leaveSquare();
					    	Level.getLevel().respawnParticularGhost(ghost);
					    }
					};
					int deadGhostAnimationTime = 5 * 200;
					Timer timer = new Timer();
					timer.schedule(timerTask, deadGhostAnimationTime);
				}
			}
		}
	}
}
