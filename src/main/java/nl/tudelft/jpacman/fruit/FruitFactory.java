package nl.tudelft.jpacman.fruit;

import nl.tudelft.jpacman.sprite.PacManSprites;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.level.Player;
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
	private final int LIFE_TIME = 10;
	
	/**
	 * The duration of the power of a pomgranate returned by this factory.
	 */
	private final int POMEGRANATE_DURATION = 4;
	
	/**
	 * The duration of the power of a bell pepper returned by this factory.
	 */
	private final int BELL_PEPPER_DURATION = 4;
	
	/**
	 * The duration of the power of a tomato returned by this factory.
	 */
	private final int TOMATO_DURATION = 4;
	
	/**
	 * The duration of the power of a kidney bean returned by this factory.
	 */
	private final int KIDNEY_BEAN_DURATION = 4;
	
	/**
	 * The duration of the power of a potato returned by this factory.
	 */
	private final int POTATO_DURATION = 2;
	
	/**
	 * The duration of the power of a fish returned by this factory.
	 */
	private final int FISH_DURATION = 2;

	/**
	 * The sprite store used for the sprites of the fruits returned by this factory.
	 */
	private final PacManSprites sprites;
	
	/**
	 * A list of Fruits used for picking a fruit randomly.
	 */
	private List<Fruit> fruits = new ArrayList<>();
	
	/**
	 * Squares on the boards where fruits should appear, this list is used to pick one square randomly.
	 */
	private List<Square> fruitPositions;
	
	private Random rand = new Random();
	
	private Fruit fish;
	private Fruit potato;
	private Fruit tomato;
	private Fruit pomgranate;
	private Fruit bellPepper;
	private Fruit kidneyBean;
	
	/**
	 * Create a FruitFactory object
	 * 
	 * @param PacManSprites spriteStore The sprite store used for the sprites of the fruits returned by this factory.
	 * @param List<Square> fruitPos the list of the squares on the boards where fruits should appear.
	 * @param List<NPC> npcs the list of the actives NPCs in the game.
	 */
	public FruitFactory(PacManSprites spriteStore, List<Square> fruitPos, List<NPC> npcs) {
		this.sprites = spriteStore;
		fruitPositions = fruitPos;
		fish = new Fish(sprites.getFishSprite(), LIFE_TIME, FISH_DURATION);
		fruits.add(fish);
		potato = new Potato(sprites.getPotatoSprite(), LIFE_TIME, POTATO_DURATION, npcs);
		fruits.add(potato);
		tomato = new Tomato(sprites.getTomatoSprite(), LIFE_TIME, TOMATO_DURATION);
		fruits.add(tomato);
		pomgranate = new Pomgranate(sprites.getPomgranateSprite(), LIFE_TIME, POMEGRANATE_DURATION, npcs);
		fruits.add(pomgranate);
		bellPepper = new BellPepper(sprites.getBellPepperSprite(), LIFE_TIME, BELL_PEPPER_DURATION);
		fruits.add(bellPepper);
		kidneyBean = new KidneyBean(sprites.getKidneyBeanSprite(), LIFE_TIME, KIDNEY_BEAN_DURATION);
		fruits.add(kidneyBean);
	}

	/**
	 * Return a Fruit picked randomly.
	 * @return a Fruit object picked randomly
	 */
	public Fruit getRandomFruit() {
		return fruits.get(rand.nextInt(fruits.size()));
	}
	
	/**
	 * Returns a BellPepper object
	 * @return a BellPepper object
	 */
	public Fruit getBellPepper() {
		return bellPepper;
	}
	
	/**
	 * Returns a BellPepper object
	 * @return a BellPepper object
	 */
	public Fruit getPomgranate() {
		return pomgranate;
	}
	
	/**
	 * Returns a BellPepper object
	 * @return a BellPepper object
	 */
    public Square getRandomFruitPosition() {
    	return fruitPositions.get(rand.nextInt(fruitPositions.size()));
	}

    /**
	 * Returns a Fish object
	 * @return a Fish object
	 */
	public Fruit getFish() {
		return fish;
	}

	/**
	 * Returns a KidneyBean object
	 * @return a KidneyBean object
	 */
	public Fruit getKidneyBean() {
		return kidneyBean;
	}

	/**
	 * Returns a Potato object
	 * @return a Potato object
	 */
	public Fruit getPotato() {
		return potato;
	}

	/**
	 * Returns a Tomato object
	 * @return a Tomato object
	 */
	public Fruit getTomato() {
		return tomato;
	}
}
