package nl.tudelft.jpacman.fruit;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import nl.tudelft.jpacman.board.PassThroughWall;
import nl.tudelft.jpacman.level.Level;
import nl.tudelft.jpacman.level.Player;
import nl.tudelft.jpacman.npc.ghost.Ghost;
import nl.tudelft.jpacman.npc.ghost.Navigation;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * The Pomgranate is a fruit that when eaten by Pac-Man instantly kill ghosts
 * that are at a distance of four square away from it.
 */
public class Pomgranate extends Fruit {
	
	/**
	 *  The level where this fruit was created.
	 */
	private final Level level;
	
	/**
	 * Create a Pomgranate object
	 * @param sprite the sprite of this fruit
	 * @param lifetime the time for which this fruit will remain on the board
	 * @param effectDuration the time for which the power of this fruit is active.
	 * @param l the level where this fruit was created.
	 */
	protected Pomgranate(Sprite sprite, int lifetime, int effectDuration, Level l) {
		super(sprite, lifetime, effectDuration);
		level = l;
	}

	/**
	 * Activate the effect
	 * @param p the player that ate this fruit.
     */
	@Override
	public void fruitEffect(Player p) {
		Set<Ghost> ghosts = level.getGhosts().keySet();
		Timer timer;
		TimerTask timerTask;
		PassThroughWall ptw = new PassThroughWall();
		for(Ghost ghost: ghosts){
			if(ghost.getSquare() != null &&
					Navigation.shortestPath(p.getSquare(),
							ghost.getSquare(),
							ptw).size() <= 4 &&
					!(ghost.hasExploded())){
				ghost.setExplode(true);
				timerTask = new TimerTask() {
					public void run() {
						ghost.leaveSquare();
						Level.getLevel().respawnParticularGhost(ghost);
					}
				};
				final int deadGhostAnimationTime = 5 * 200;
				timer = new Timer();
				timer.schedule(timerTask, deadGhostAnimationTime);
			}
		}
	}
}

