package nl.tudelft.jpacman.level;

import java.util.List;
import java.util.Map;

import nl.tudelft.jpacman.board.Board;
import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.npc.NPC;
import nl.tudelft.jpacman.npc.ghost.Ghost;
import nl.tudelft.jpacman.npc.ghost.GhostColor;
import nl.tudelft.jpacman.npc.ghost.GhostFactory;
import nl.tudelft.jpacman.sprite.AnimatedSprite;
import nl.tudelft.jpacman.sprite.PacManSprites;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * Factory that creates levels and units.
 * 
 * @author Jeroen Roosen 
 */
public class LevelFactory {

	private static final int GHOSTS = 4;
	private static final int BLINKY = 0;
	private static final int INKY = 1;
	private static final int PINKY = 2;
	private static final int CLYDE = 3;

	/**
	 * The default value of a pellet.
	 */
	private static final int PELLET_VALUE = 10;
	
	/**
	 * The default time in seconds during which a player or ghost is trapped into a hole.
	 */
	private static final int HOLE_TIME = 1;

	/**
	 * The default value of a super pellet.
	 */
	private static final int SUPERPELLET_VALUE = 50;

	/**
	 * The sprite store that provides sprites for units.
	 */
	private final PacManSprites sprites;

	/**
	 * Used to cycle through the various ghost types.
	 */
	private int ghostIndex;

	/**
	 * The factory providing ghosts.
	 */
	private final GhostFactory ghostFact;

	/**
	 * Creates a new level factory.
	 * 
	 * @param spriteStore
	 *            The sprite store providing the sprites for units.
	 * @param ghostFactory
	 *            The factory providing ghosts.
	 */
	public LevelFactory(PacManSprites spriteStore, GhostFactory ghostFactory) {
		this.sprites = spriteStore;
		this.ghostIndex = -1;
		this.ghostFact = ghostFactory;
	}

	/**
	 * Creates a new level from the provided data.
	 * 
	 * @param board
	 *            The board with all ghosts and pellets occupying their squares.
	 * @param ghosts
	 *            A list of all ghosts on the board.
	 * @param startPositions
	 *            A list of squares from which players may start the game.
	 * @return A new level for the board.
	 */
	public Level createLevel(Board board, List<NPC> ghosts,
			List<Square> startPositions, List<Square> fruitPositions) {

		// We'll adopt the simple collision map for now.
		CollisionMap collisionMap = new PlayerCollisions();
		
		return new Level(board, ghosts, startPositions, collisionMap);
	}

	/**
	 * Creates a new ghost.
	 * 
	 * @return The new ghost.
	 */
	NPC createGhost() {
		ghostIndex++;
		ghostIndex %= GHOSTS;
		switch (ghostIndex) {
		case BLINKY:
			return ghostFact.createBlinky();
		case INKY:
			return ghostFact.createInky();
		case PINKY:
			return ghostFact.createPinky();
		case CLYDE:
			return ghostFact.createClyde();
		default:
			return new RandomGhost(sprites.getGhostSprite(GhostColor.RED), sprites.getGhostExplodeAnimation());
		}
	}

	/**
	 * Creates a new pellet.
	 * 
	 * @return The new pellet.
	 */
	public Pellet createPellet() {
		return new Pellet(PELLET_VALUE, sprites.getPelletSprite());
	}
	
	/**
	 * Creates a new hole.
	 * 
	 * @return The new hole.
	 */
	public Hole createHole() {
		return new Hole(HOLE_TIME, sprites.getHoleSprite());
	}
	
	/**
	 * Creates a new teleport.
	 * 
	 * @return The new teleport.
	 */
	public Teleport createTeleport()
	{
		return new Teleport(sprites.getTeleportSprite());
	}
	
	/**
	 * Creates a new bridge.
	 * 
	 * @return The new bridge.
	 */
	public Bridge createBridge()
	{
		return new Bridge(sprites.getBridgeSprites());
	}

	/**
	 * Creates a new super pellet.
	 *
	 * @return The new super pellet.
	 */
	public Pellet createSuperPellet() { return new Pellet(SUPERPELLET_VALUE, sprites.getSuperPelletSprite()); }

	/**
	 * Implementation of an NPC that wanders around randomly.
	 * 
	 * @author Jeroen Roosen 
	 */
	private static final class RandomGhost extends Ghost {

		/**
		 * The suggested delay between moves.
		 */
		private static final long DELAY = 175L;

		/**
		 * Creates a new random ghost.
		 * 
		 * @param ghostSprite
		 *            The sprite for the ghost.
		 */
		private RandomGhost(Map<Direction, Sprite> ghostSprite, AnimatedSprite explodeAnimation) {
			super(ghostSprite, explodeAnimation);
		}

		@Override
		public long getInterval() {
			return DELAY;
		}

		@Override
		public Direction nextMove() {
			return randomMove();
		}
	}
}
