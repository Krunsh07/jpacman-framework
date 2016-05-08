package nl.tudelft.jpacman.npc.ghost;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.npc.NPC;
import nl.tudelft.jpacman.sprite.AnimatedSprite;
import nl.tudelft.jpacman.sprite.PacManSprites;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * An antagonist in the game of Pac-Man, a ghost.
 * 
 * @author Jeroen Roosen 
 */
public abstract class Ghost extends NPC {
	
	/**
	 * The animation to play when this ghost explodes.
	 */
	private final AnimatedSprite explodeSprite;
	
	/**
	 * Whether this ghost has exploded or not.
	 */
	private boolean exploded;

	/**
	 * The sprite store
	 */
	private static final PacManSprites SPRITE_STORE = new PacManSprites();

	public static int count = 0;

	/**
	 * A boolean to know if ghosts are feared by Pacman.
	 */
	private boolean fearedMode;

	/**
	 * The last position (Square) of the Ghost.
	 */
	private Square lastSquare;

	/**
	 * Modificateur de vitesse du ghost
	 */
	protected double speed = 1.0;

	public static int ghostLeft;

	public static int ghostAte = 0;

	/**
	 * Creates a new ghost.
	 * 
	 * @param spriteMap The sprites for every direction.
	 * @param explodeAnimation the animation to play when this ghost explodes.
	 */
	protected Ghost(Map<Direction, Sprite> spriteMap, AnimatedSprite explodeAnimation) {
		setMovable(true);
		this.explodeSprite = explodeAnimation;
		this.exploded = false;
		setSprites(spriteMap);
	}

	@Override
	public Sprite getSprite() {
		if (!exploded) {
			return getSprites().get(getDirection());
		}
		return explodeSprite;
	}

	/**
	 *
	 * @return true iff the game is under the Hunter Mode
	 */
	public boolean getFearedMode(){ return fearedMode; }

	/**
	 *
	 * set the game mode under Hunter Mode
	 */
	public void setFearedMode(boolean fearedMode) { this.fearedMode = fearedMode; }

	/**
	 *
	 * Change the aim of Ghosts. For some seconds, they are feared.
	 */
	public void startFearedMode()
	{
		setFearedMode(true);
		setSprites(SPRITE_STORE.getGhostSprite(GhostColor.VUL_BLUE));
	}

	/**
	 *
	 * Signal the end of the Hunter Mode
	 */
	public void warningMode()
	{
		if(getFearedMode())
		{
			if(count%2==0) {
				setSprites(SPRITE_STORE.getGhostSprite(GhostColor.VUL_BLUE));
			}
			else{
				setSprites(SPRITE_STORE.getGhostSprite(GhostColor.VUL_WHITE));
			}
		}
	}

	/**
	 *
	 * Stop the Hunter Mode
	 */
	public void stopFearedMode()
	{
		setFearedMode(false);
		count = 0;
		if(this instanceof Blinky)
		{
			setSprites(SPRITE_STORE.getGhostSprite(GhostColor.RED));
		}
		if(this instanceof Inky)
		{
			setSprites(SPRITE_STORE.getGhostSprite(GhostColor.CYAN));
		}
		if(this instanceof Pinky)
		{
			setSprites(SPRITE_STORE.getGhostSprite(GhostColor.PINK));
		}
		if(this instanceof Clyde)
		{
			setSprites(SPRITE_STORE.getGhostSprite(GhostColor.ORANGE));
		}
	}

	public static Ghost addGhost(GhostFactory ghostFact, int index){
		Ghost g;
		switch (index) {
			case 0:
				g = ghostFact.createBlinky();
				break;
			case 1:
				g = ghostFact.createInky();
				break;
			case 2:
				g = ghostFact.createPinky();
				break;
			case 3:
				g = ghostFact.createClyde();
				break;
			default:
				g = ghostFact.createBlinky();
				break;
		}
		return g;
	}

	/**
	 * Determines a possible move in a random direction.
	 * 
	 * @return A direction in which the ghost can move, or <code>null</code> if
	 *         the ghost is shut in by inaccessible squares.
	 */
	protected Direction randomMove() {
		Square square = getSquare();
		List<Direction> directions = new ArrayList<>();
		for (Direction d : Direction.values()) {
			if (square.getSquareAt(d).isAccessibleTo(this)) {
				directions.add(d);
			}
		}
		if (directions.isEmpty()) {
			for (Direction d : Direction.values()) {
				if (square.getSquareAt(d) == getLastSquare()) {
					directions.add(d);
				}
			}
		}
		int i = new Random().nextInt(directions.size());
		this.lastSquare = getSquare();
		return directions.get(i);
	}

	/**
	 *
	 * @return the last position of the ghost
	 */
	protected Square getLastSquare() {
		return this.lastSquare;
	}

	/**
	 * Determines a possible move at crossroads in a random direction.
	 *
	 * @return A direction in which the ghost can move.
	 */
	protected Direction randomMoveAtCrossroads()
	{
		Square square = getSquare();
		List<Direction> directions = new ArrayList<>();
		for (Direction d : Direction.values()) {
			if (square.getSquareAt(d).isAccessibleTo(this) && square.getSquareAt(d) != getLastSquare()) {
				directions.add(d);
			}
		}
		if (directions.isEmpty()) {
			for (Direction d : Direction.values()) {
				if (square.getSquareAt(d) == getLastSquare()) {
					directions.add(d);
				}
			}
		}
		int i = new Random().nextInt(directions.size());
		this.lastSquare = getSquare();
		return directions.get(i);
	}

	/**
	 * Permet d'obtenir la vitesse du ghost
	 * @return La vitesse du ghost
     */
	public double getSpeed() {
		return speed;
	}

	/**
	 * Permet de mettre a jour la vitesse du ghost
	 * @param speed La nouvelle vitesse
     */
	public void setSpeed(double speed) {
		this.speed = speed;
	}

	/**
	 * Accelération temporaire des ghosts
	 * @param time Le temps de l'accélération
     */
	public void temporaryAcceleration(int time)
	{
		Map<Direction, Sprite> oldSprites = getSprites();
		setAcceleration(true);
		setSprites(new PacManSprites().getAngryGhostSprite());
		TimerTask timerTask = new TimerTask() {
		    public void run() {
		    	setAcceleration(false);
		        setSprites(oldSprites);
		    }
		};
		Timer timer = new Timer();
		timer.schedule(timerTask, time * 1000);
	}

	/**
	 * Returns true if this ghost exploded.
	 * @return true if this ghost exploded
	 */
	public boolean hasExploded() {
		return exploded;
	}
	
	/**
	 * Changer the explosion state of a ghost.
	 * @param value the new state
	 */
	public void setExplode(boolean value) {
		if (!value) {
			explodeSprite.setAnimating(false);
			setMovable(true);
		}
		if (value) {
			setMovable(false);
			explodeSprite.restart();
		}
		this.exploded = value;
	}
}
