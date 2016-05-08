package nl.tudelft.jpacman.level;

import java.util.Map;

import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.sprite.AnimatedSprite;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * A player operated unit in our game.
 * 
 * @author Jeroen Roosen 
 */

public class Player extends MovableCharacter {

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
	 * <code>true</code> iff this player is under the Hunter Mode.
	 */
	private boolean hunterMode;

	/**
	 * Creates a new player with a score of 0 points.
	 * 
	 * @param spriteMap
	 *            A map containing a sprite for this player for every direction.
	 * @param deathAnimation
	 *            The sprite to be shown when this player dies.
	 */
	Player(Map<Direction, Sprite> spriteMap, AnimatedSprite deathAnimation) {
		setMovable(true);
		this.score = 0;
		this.alive = true;
		this.shooting = false;
		setSprites(spriteMap);
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
			return getSprites().get(getDirection());
		}
		return deathSprite;
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

	/**
	 * Permet de savoir si le joueur est invincible
	 * @return True si le joueur est invincible, false sinon
     */
	public boolean isInvincible() {
		return invincible;
	}

	/**
	 * Permet rendre un joueur invincible ou non
	 * @param value L'état du joueur
     */
	public void setInvincible(boolean value) {
		this.invincible = value;
	}

	/**
	 * Permet d'obtenir le temps entre 2 interval
	 * @return L'interval entre 2 déplacement
     */
	public long getInterval() {
		if(!getAcceleration()){
			return MOVE_INTERVAL;
		}
		else{
			return ACCELERATED_MOVE_INTERVAL;
		}
	}

	/**
	 * Pour savoir si le joueur est en train de tirer
	 * @return True si le joueur tire, false sinon
     */
	public boolean isShooting() {
		return shooting;
	}

	/**
	 * Permet de définir si le joueur tire
	 * @param shooting Si le joueur tire ou pas
     */
	public void setShooting(boolean shooting) {
		this.shooting = shooting;
	}

	/**
	 * Permet de définir la direction du joueurs
	 * @param direction La direction
     */
	public void setDirection(Direction direction) {
		Square square = getSquare();
		System.out.println(square);
		if(isMovable() && square.getSquareAt(direction).isAccessibleTo(this)) {
			super.setDirection(direction);
		}
	}

	@Override
	public Direction nextMove() {
		return getDirection();
	}
}