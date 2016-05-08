package nl.tudelft.jpacman.fruit;

import java.util.Timer;
import java.util.TimerTask;

import nl.tudelft.jpacman.level.Player;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * The kidney bean is a vegetable that temporarily gives Pac-Man the power to
 * fire bullets that can kill ghosts
 */
public class KidneyBean extends Fruit {

	/**
	 * Create a KidneyBean object
	 * @param sprite the sprite of this kidney bean
	 * @param lifetime the time for which this kidney bean will remain on the board
	 * @param effectDuration the time for which the power of this kidney bean is active.
	 */
	protected KidneyBean(Sprite sprite, int lifetime, int effectDuration) {
		super(sprite, lifetime, effectDuration);
	}

	@Override
	public void fruitEffect(Player p) {
		p.setShooting(true);
		TimerTask timerTask = new TimerTask() {
		    public void run() {
		    	p.setShooting(false);
		    }
		};
		Timer timer = new Timer();
		timer.schedule(timerTask, getEffectDuration() * 1000);
	}
}
