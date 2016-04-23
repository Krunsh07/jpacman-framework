package nl.tudelft.jpacman.level;

import java.util.Map;

import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.npc.DirectionCharacter;
import nl.tudelft.jpacman.npc.NPC;
import nl.tudelft.jpacman.sprite.AnimatedSprite;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * A player operated unit in our game.
 * 
 * @author Jeroen Roosen 
 */
public class Player extends NPC implements DirectionCharacter {
	
	/**
	 * The base movement interval.
	 */
	private static final int MOVE_INTERVAL = 250;
	
	/**
	 * The base movement when the ghost is accelerated.
	 */
	private static final int ACCELERATED_MOVE_INTERVAL = 125;

	/**
	 * The amount of points accumulated by this player.
	 */
	private int score;

	/**
	 * The animations for every direction.
	 */
	private Map<Direction, Sprite> sprites;

	/**
	 * The animation that is to be played when Pac-Man dies.
	 */
	private final AnimatedSprite deathSprite;

	/**
	 * <code>true</code> iff this player is alive.
	 */
	private boolean alive;
	
	/**
	 * <code>true</code> iff this player is invisible.
	 */
	private boolean invincible;
	
	/**
	 * <code>true</code> iff this player is firing bullets.
	 */
	private boolean shooting;
	
	/**
	 * Whether this unit can be moved or not.
	 */
	private boolean mobile;

	/**
	 * <code>true</code> iff this player is under the Hunter Mode.
	 */
	private boolean hunterMode = false;

	/**
	 * Creates a new player with a score of 0 points.
	 * 
	 * @param spriteMap
	 *            A map containing a sprite for this player for every direction.
	 * @param deathAnimation
	 *            The sprite to be shown when this player dies.
	 */
	Player(Map<Direction, Sprite> spriteMap, AnimatedSprite deathAnimation) {
		this.mobile = true;
		this.score = 0;
		this.alive = true;
		this.shooting = false;
		this.sprites = spriteMap;
		this.deathSprite = deathAnimation;
		deathSprite.setAnimating(false);
	}

	/**
	 * Returns whether this player is alive or not.
	 * 
	 * @return <code>true</code> iff the player is alive.
	 */
	public boolean isAlive() {
		return alive;
	}

	/**
	 * Return the actual game mode
	 *
	 * @return true iff the game is under the Hunter Mode
     */
	public boolean getHunterMode() {
		return hunterMode;
	}

	/**
	 * Change the Game Mode.
	 */
	public void setHunterMode(boolean mode) {
		hunterMode = mode;
	}

	/**
	 * Sets whether this player is alive or not.
	 * 
	 * @param isAlive
	 *            <code>true</code> iff this player is alive.
	 */
	public void setAlive(boolean isAlive) {
		if (isAlive) {
			deathSprite.setAnimating(false);
		}
		if (!isAlive) {
			deathSprite.restart();
		}
		this.alive = isAlive;
	}

	/**
	 * Returns the amount of points accumulated by this player.
	 * 
	 * @return The amount of points accumulated by this player.
	 */
	public int getScore() {
		return score;
	}

	@Override
	public Sprite getSprite() {
		if (isAlive()) {
			return sprites.get(getDirection());
		}
		return deathSprite;
	}
	
	public Map<Direction, Sprite> getSprites() {
		return sprites;
	}

	/**
	 * Adds points to the score of this player.
	 * 
	 * @param points
	 *            The amount of points to add to the points this player already
	 *            has.
	 */
	public void addPoints(int points) {
		score += points;
	}
	
	public void setSprites(Map<Direction, Sprite> sprites) {
		this.sprites = sprites;
	}

	public boolean isInvincible() {
		return invincible;
	}

	public void setInvincible(boolean value) {
		this.invincible = value;
	}

	@Override
	public long getInterval() {
		if(!getAcceleration()){
			return MOVE_INTERVAL;
		}
		else{
			return ACCELERATED_MOVE_INTERVAL;
		}
	}

	@Override
	public Direction nextMove() {
		return getDirection();
	}

	public boolean isShooting() {
		return shooting;
	}

	public void setShooting(boolean shooting) {
		this.shooting = shooting;
	}

	@Override
	public void setMobility(boolean newValue) {
		mobile = newValue;
	}

	@Override
	public boolean getMobility() {
		return mobile;
	}
	
	public void setDirection(Direction direction) {
		if(getMobility()) {
			super.setDirection(direction);
		}
	}
}