package nl.tudelft.jpacman.fruit;

import java.util.Timer;
import java.util.TimerTask;

import nl.tudelft.jpacman.level.Player;
import nl.tudelft.jpacman.sprite.PacManSprites;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * A fish is food that temporarily prevent Pac-Man from moving 
 */
public class Fish extends Fruit {
	
	/**
	 * Create a Fish object
	 * @param sprite the sprite of this fish
	 * @param lifetime the time for which this fish will remain on the board
	 * @param effectDuration the time for which the power of this fish is active.
	 */
	protected Fish(Sprite sprite, int lifetime, int effectDuration) {
		super(sprite, lifetime, effectDuration);
	}

	/**
	 * Activate the effect
	 * @param p the player that ate this fruit.
     */
	@Override
	public void fruitEffect(Player p) {
		p.setMovable(false);
		p.setSprites(new PacManSprites().getPacmanParalizedSprites());
		TimerTask timerTask = new TimerTask() {
		    public void run() {
		        p.setMovable(true);
		        p.setSprites(new PacManSprites().getPacmanSprites());
		    }
		};
		Timer timer = new Timer();
		timer.schedule(timerTask, getEffectDuration() * 1000);
	}
}
