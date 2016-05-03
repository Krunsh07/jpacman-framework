package nl.tudelft.jpacman.fruit;

import nl.tudelft.jpacman.sprite.PacManSprites;
import nl.tudelft.jpacman.level.Level;
import nl.tudelft.jpacman.npc.NPC;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A FruitFactory is an object used to get Fruit objects ready to be used in the game.
 */
public class FruitFactory {
	
	/**
	 * The life time of any fruit returned by this factory
	 */
	private static final int LIFE_TIME = 10;
	
	/**
	 * The duration of the power of a fruit that has a positive effect for the player.
	 */
	private static final int GOOD_EFFECT_DURATION = 4;
	
	/**
	 * The number of different fruits supported by this game.
	 */
	private static final int FRUITS = 6;
	
	/**
	 * The duration of the power of a fruit that has a negative effect for the player.
	 */
	private static final int BAD_EFFECT_DURATION = 2;
	
	/**
	 * List of supported fruits.
	 */
	private static enum Fruits {FISH, POTATO, TOMATO, POMGRANATE, BELLPEPPER, KIDNEYBEAN};
	
	/**
	 * The level where this factory will produce fruits.
	 */
	private Level level;

	/**
	 * The sprite store used for the sprites of the fruits returned by this factory.
	 */
	private final PacManSprites sprites;
	
	private Random rand;
	
	/**
	 * Create a FruitFactory object
	 * 
	 * @param spriteStore spriteStore The sprite store used for the sprites of the fruits returned by this factory.
	 * @param l The game level.
	 */
	public FruitFactory(PacManSprites spriteStore, Level l) {
		sprites = spriteStore;
		level = l;
		rand = new Random();
	}

	/**
	 * Return a Fruit picked randomly.
	 * @return a Fruit object picked randomly
	 */
	public Fruit getRandomFruit() {
		int fruit = rand.nextInt(FRUITS);
		Fruits f = Fruits.values()[fruit];
		//return getPomgranate();

		switch(f) {
		    case FISH:
		    	return getFish();
		    case POTATO:
		    	return getPotato();
		    case POMGRANATE:
		    	return getPomgranate();
		    case BELLPEPPER:
		    	return getBellPepper();
		    case KIDNEYBEAN:
		    	return getKidneyBean();
		    case TOMATO:
		    	return getTomato();
		    default:
		    	System.err.println("Fruit non supporté");
		    	return null;
		}
	}
	
	/**
	 * Returns a BellPepper object
	 * @return a BellPepper object
	 */
	public Fruit getBellPepper() {
		return new BellPepper(sprites.getBellPepperSprite(), LIFE_TIME, GOOD_EFFECT_DURATION);
	}
	
	/**
	 * Returns a Pomgranate object
	 * @return a Pomgranate object
	 */
	public Fruit getPomgranate() {
		return new Pomgranate(sprites.getPomgranateSprite(), LIFE_TIME, GOOD_EFFECT_DURATION, level);
	}

    /**
	 * Returns a Fish object
	 * @return a Fish object
	 */
	public Fruit getFish() {
		return new Fish(sprites.getFishSprite(), LIFE_TIME, BAD_EFFECT_DURATION);
	}

	/**
	 * Returns a KidneyBean object
	 * @return a KidneyBean object
	 */
	public Fruit getKidneyBean() {
		return new KidneyBean(sprites.getKidneyBeanSprite(), LIFE_TIME, GOOD_EFFECT_DURATION);
	}

	/**
	 * Returns a Potato object
	 * @return a Potato object
	 */
	public Fruit getPotato() {
		return new Potato(sprites.getPotatoSprite(), level, LIFE_TIME, BAD_EFFECT_DURATION);
	}

	/**
	 * Returns a Tomato object
	 * @return a Tomato object
	 */
	public Fruit getTomato() {
		return new Tomato(sprites.getTomatoSprite(), LIFE_TIME, GOOD_EFFECT_DURATION);
	}
}
