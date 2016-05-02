package nl.tudelft.jpacman.level;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.npc.ghost.Ghost;
import nl.tudelft.jpacman.sprite.PacManSprites;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * A hole is a special object that prevent pacman or any ghost to move during a
 * specified amount of time
 * 
 * @author Florent Barraco
 */
public class Hole extends Unit {

	/**
	 * The sprite of this unit.
	 */
	private final Sprite image;
	
	/**
	 * The time in seconds for which a character is trapped into this hole.
	 */
	private int trapTime;
	
	/**
	 * Creates a new hole.
	 * @param time The time in seconds for which a character is trapped into 
	 * this hole.
	 * @param sprite The sprite of this hole.
	 */
	public Hole(int time, Sprite sprite) {
		this.image = sprite;
		this.trapTime = time;
	}
	
	@Override
	public Sprite getSprite() {
		return image;
	}
	
	/**
	 * Get the time in seconds for which a character is trapped into this hole.
	 */
	public int getTrapTime(){
		return trapTime;
	}
	
	/**
	 * Trap a character into this hole during the time specified in the method
	 * getTrapTime.
	 * @param dc the character which will be trapped in this hole
	 */
	public void effect(MovableCharacter mc) {
		Map<Direction, Sprite> oldSprites = mc.getSprites();
	    mc.setMovable(false);
		if(mc instanceof Player)
			((Player) mc).setSprites(new PacManSprites().getPacmanParalizedSprites());
		else if(mc instanceof Ghost)
			((Ghost) mc).setSprites(new PacManSprites().getParalizedGhostSprite());
		TimerTask timerTask = new TimerTask() {
			public void run() {
			    mc.setMovable(true);
			    mc.setSprites(oldSprites);
			}
	    };
		Timer timer = new Timer();
		timer.schedule(timerTask, trapTime * 1000);
	}
	
}