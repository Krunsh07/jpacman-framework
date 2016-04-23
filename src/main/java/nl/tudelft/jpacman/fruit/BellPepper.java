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
	 * @param Sprite sprite the sprite of this bell-pepper
	 * @param int lifetime the time for which this bell-pepper will remain on the board
	 * @param int effectDuration the time for which the power of this bell-pepper is active.
	 */
	protected BellPepper(Sprite sprite, int lifetime, int effectDuration) {
		super(sprite, lifetime, effectDuration);
	}

	@Override
	public void fruitEffect(Player p) {
		PacManSprites pms = new PacManSprites();
		Map<Direction, Sprite> oldSprites = pms.getPacmanSprites();
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
