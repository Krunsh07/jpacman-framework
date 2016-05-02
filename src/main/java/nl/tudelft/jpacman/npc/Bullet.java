package nl.tudelft.jpacman.npc;

import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.level.Bridge;
import nl.tudelft.jpacman.level.Player;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * A Bullet is an object that Pac-Man fires temporarily when he eat a kidney bean.
 */
public class Bullet extends NPC{
	
	/**
	 * The sprite map, one sprite for each direction.
	 */
	private Sprite sprite;
	
	/**
	 * The base movement interval.
	 */
	private static final int MOVE_INTERVAL = 100;
	
	/**
	 * The amount of time in seconds before Pac-Man can fire another bullet
	 */
	private static final int BULLET_DELAY = 1;
	
	/**
	 * Whether this bullet has hit something or not, when a bullet has hit a wall, a ghost or was blocked by a bridge the bullet is dead.
	 */
	private boolean alive;
	
	/**
	 * The direction when the player has fired this bullet.
	 */
	private final Direction shootingDirection;
	
	/**
	 * Create a bullet object
	 * @param sprite the sprite of this bullet
	 * @param p the player that fired this bullet.
	 */
	public Bullet(Sprite sprite, Player p) {
		setMovable(true);
		this.setAlive(true);
		this.sprite = sprite;
		shootingDirection = p.getDirection();
	}

	@Override
	public long getInterval() {
		return MOVE_INTERVAL;
	}
	
	@Override
	public void setDirection(Direction direction) {}

	@Override
	public Direction nextMove() {
		if(alive && getSquare().getSquareAt(shootingDirection).isAccessibleTo(this) && !(Bridge.blockedBybridge(this, shootingDirection))) {
			return shootingDirection;
		}
		else {
			setAlive(false);
			return null;
		}
	}

	@Override
	public Sprite getSprite() {
		return sprite;
	}

	/**
	 * Returns if Whether this bullet is alive or not. 
	 * @return true Whether this bullet is alive and false otherwise.
	 */
	public boolean isAlive() {
		return alive;
	}

	/**
	 * Change the life state of this bullet.
	 * @param alive the new state
	 */
	public void setAlive(boolean alive) {
		this.alive = alive;
	}
	
	/**
	 * Returns the amount of time in seconds before Pac-Man can fire another bullet
	 * @return The amount of time in seconds before Pac-Man can fire another bullet
	 */
	public int getBulletDelay() {
		return BULLET_DELAY;
	}
}
