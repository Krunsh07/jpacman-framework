package nl.tudelft.jpacman.fruit;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.level.Player;
import nl.tudelft.jpacman.sprite.PacManSprites;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * The BellPepper is a vegetable that temporarily speed-up Pac-Man when he eat it.
 */
public class BellPepper extends Fruit{
	
	/**
	 * Create a BellPepper object
	 * @param sprite the sprite of this bell-pepper
	 * @param lifetime the time for which this bell-pepper will remain on the board
	 * @param effectDuration the time for which the power
	 *                       of this bell-pepper is active.
	 */
	protected BellPepper(Sprite sprite, int lifetime, int effectDuration) {
		super(sprite, lifetime, effectDuration);
	}

	/**
	 * Activate the effect of the fruit
	 * @param p the player that ate this fruit.
     */
	@Override
	public void fruitEffect(Player p) {
		final PacManSprites pms = new PacManSprites();
		final Map<Direction, Sprite> oldSprites = pms.getPacmanSprites();
		p.setAcceleration(true);
		p.setSprites(pms.getPacmanAngrySprite());
		TimerTask timerTask = new TimerTask() {
		    public void run() {
		    	p.setAcceleration(false);
		        p.setSprites(oldSprites);
		    }
		};
		Timer timer = new Timer();
		timer.schedule(timerTask, getEffectDuration() * 1000);
	}
}
