package nl.tudelft.jpacman.fruit;

import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.level.Player;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * A Fruit is an object that has special effect when Pac-Man eat it.
 */
public abstract class Fruit extends Unit {

	/**
	 * The sprite of the fruit
	 */
	private final Sprite image;

	/**
	 * The lifetime of the fruit
	 */
	private final int lifetime;

	/**
	 * The duration of the effect
	 */
	private final int effectDuration;
	
	/**
	 * Create a Fruit object
	 * @param sprite the sprite of this fruit
	 * @param lifetime the time for which this fruit will remain on the board
	 * @param effectDuration the time for which the power of this fruit is active.
	 */
	protected Fruit(Sprite sprite, int lifetime, int effectDuration) {
		this.image = sprite;
		this.lifetime = lifetime;
		this.effectDuration = effectDuration;
	}
	
	/**
	 * Return the sprite of this fruit
	 * @return the sprite of this fruit
	 */
	public Sprite getSprite() {
		return image;
	}
	
	/**
	 * returns the time for which this fruit will remain on the board.
	 * @return The time for which this fruit will remain on the board.
	 */
	public int getLifetime() {
		return lifetime;
	}
	
	/**
	 * Returns the time for which the power of this fruit is active
	 * @return The time for which the power of this fruit is active
	 */
	public int getEffectDuration() {
		return effectDuration;
	}
	
	/**
	 * Enable the power of this fruit.
	 * @param p the player that ate this fruit.
	 */
	public abstract void fruitEffect(Player p);
}
