package nl.tudelft.jpacman.level;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import nl.tudelft.jpacman.board.Board;
import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.npc.NPC;
import nl.tudelft.jpacman.npc.ghost.Ghost;
import nl.tudelft.jpacman.sprite.PacManSprites;

/**
 * A level of Pac-Man. A level consists of the board with the players and the
 * AIs on it.
 * 
 * @author Jeroen Roosen 
 */
public class Level {

	/**
	 * The board of this level.
	 */
	private final Board board;

	private boolean hunterMode;

	/**
	 * The lock that ensures moves are executed sequential.
	 */
	private final Object moveLock = new Object();

	/**
	 * The lock that ensures starting and stopping can't interfere with each
	 * other.
	 */
	private final Object startStopLock = new Object();

	/**
	 * The NPCs of this level and, if they are running, their schedules.
	 */
	private final Map<NPC, ScheduledExecutorService> npcs;

	/**
	 * <code>true</code> iff this level is currently in progress, i.e. players
	 * and NPCs can move.
	 */
	private boolean inProgress;

	/**
	 * The squares from which players can start this game.
	 */
	private final List<Square> startSquares;

	/**
	 * The start current selected starting square.
	 */
	private int startSquareIndex;

	/**
	 * The players on this level.
	 */
	private final List<Player> players;

	/**
	 * The table of possible collisions between units.
	 */
	private final CollisionMap collisions;

	/**
	 * The objects observing this level.
	 */
	private final List<LevelObserver> observers;

	private static final PacManSprites SPRITE_STORE = new PacManSprites();

	private Timer timerHunterMode = new Timer();

	private Timer timerRespawn = new Timer();

	private Timer timerWarning = new Timer();

	public static int ghostLeft;

	public static int ghostAte = 0;


	private static Level level = null;

	/**
	 * Creates a new level for the board.
	 * 
	 * @param b
	 *            The board for the level.
	 * @param ghosts
	 *            The ghosts on the board.
	 * @param startPositions
	 *            The squares on which players start on this board.
	 * @param collisionMap
	 *            The collection of collisions that should be handled.
	 */
	public Level(Board b, List<NPC> ghosts, List<Square> startPositions,
			CollisionMap collisionMap) {
		assert b != null;
		assert ghosts != null;
		assert startPositions != null;

		this.board = b;
		this.inProgress = false;
		this.npcs = new HashMap<>();
		for (NPC g : ghosts) {
			npcs.put(g, null);
			ghostLeft++;
		}
		this.startSquares = startPositions;
		this.startSquareIndex = 0;
		this.players = new ArrayList<>();
		this.collisions = collisionMap;
		this.observers = new ArrayList<>();
		if(level == null) {
			level = this;
		}
	}

	/**
	 * Adds an observer that will be notified when the level is won or lost
	 * or change his state (Hunter mode).
	 *
	 * @param observer The observer that will be notified.
	 */
	public void addObserver(LevelObserver observer) {
		if (observers.contains(observer)) {
			return;
		}
		observers.add(observer);
	}

	/**
	 * Removes an observer if it was listed.
	 *
	 * @param observer The observer to be removed.
	 */
	public void removeObserver(LevelObserver observer) {
		observers.remove(observer);
	}

	/**
	 * Registers a player on this level, assigning him to a starting position. A
	 * player can only be registered once, registering a player again will have
	 * no effect.
	 * 
	 * @param p
	 *            The player to register.
	 */
	public void registerPlayer(Player p) {
		assert p != null;
		assert !startSquares.isEmpty();

		if (players.contains(p)) {
			return;
		}
		players.add(p);
		Square square = startSquares.get(startSquareIndex);
		p.occupy(square);
		startSquareIndex++;
		startSquareIndex %= startSquares.size();
	}

	/**
	 * Returns the board of this level.
	 * 
	 * @return The board of this level.
	 */
	public Board getBoard() {
		return board;
	}

	/**
	 * Moves the unit into the given direction if possible and handles all
	 * collisions.
	 * 
	 * @param unit
	 *            The unit to move.
	 * @param direction
	 *            The direction to move the unit in.
	 */
	public void move(Unit unit, Direction direction) {
		assert unit != null;
		assert direction != null;

		if (!isInProgress()) {
			return;
		}

		synchronized (moveLock) {
			unit.setDirection(direction);
			Square location = unit.getSquare();
			Square destination = location.getSquareAt(direction);

			if (destination.isAccessibleTo(unit)) {
				List<Unit> occupants = destination.getOccupants();
				unit.occupy(destination);
				for (Unit occupant : occupants) {
					collisions.collide(unit, occupant);
				}
			}
			updateObservers();
		}
	}

	/**
	 * Starts or resumes this level, allowing movement and (re)starting the
	 * NPCs.
	 */
	public void start() {
		synchronized (startStopLock) {
			if (isInProgress()) {
				return;
			}
			startNPCs();
			inProgress = true;
			updateObservers();
		}
	}

	/**
	 * Stops or pauses this level, no longer allowing any movement on the board
	 * and stopping all NPCs.
	 */
	public void stop() {
		synchronized (startStopLock) {
			if (!isInProgress()) {
				return;
			}
			stopNPCs();
			inProgress = false;
		}
	}

	/**
	 * Starts all NPC movement scheduling.
	 */
	private void startNPCs() {
		for (final NPC npc : npcs.keySet()) {
			ScheduledExecutorService service = Executors
					.newSingleThreadScheduledExecutor();
			service.schedule(new NpcMoveTask(service, npc),
					npc.getInterval() / 2, TimeUnit.MILLISECONDS);
			npcs.put(npc, service);
		}
	}

	/**
	 * Stops all NPC movement scheduling and interrupts any movements being
	 * executed.
	 */
	private void stopNPCs() {
		for (Entry<NPC, ScheduledExecutorService> e : npcs.entrySet()) {
			e.getValue().shutdownNow();
		}
	}

	/**
	 * Permet d'ajouter des ghosts dans le jeu
	 * @param l Le Level qui contient les ghost a ajouter
     */
	public void addGhost(Level l)
	{
		if(this.isInProgress())
		{
			l.start();
		}
		this.npcs.putAll(l.getNpcs());
		this.stop();
		//l.getNpcs().putAll(this.getNpcs());
		this.start();
		for (NPC npc : npcs.keySet())
		{
			Ghost g = (Ghost) (npc);
			g.setSpeed(g.getSpeed() + 0.05);
		}

	}

	/**
	 * Returns whether this level is in progress, i.e. whether moves can be made
	 * on the board.
	 * 
	 * @return <code>true</code> iff this level is in progress.
	 */
	public boolean isInProgress() {
		return inProgress;
	}

	/**
	 * Updates the observers about the state of this level.
	 */
	private void updateObservers() {
		if (!isAnyPlayerAlive()) {
			for (LevelObserver o : observers) {
				o.levelLost();
			}
		}
		if (isAnyPlayerInHunterMode()) {
			for (LevelObserver o : observers) {
				o.startHunterMode();
			}
		}
		/*if (ghostLeft != 4) {
			for (LevelObserver o : observers) {
				o.respawnGhost();
			}
		}*/
		/*if (remainingPellets() == 0) {
			for (LevelObserver o : observers) {
				o.levelWon();
			}
		}*/
	}

	/**
	 * Returns <code>true</code> iff at least one of the players in this level
	 * is alive.
	 * 
	 * @return <code>true</code> if at least one of the registered players is
	 *         alive.
	 */
	public boolean isAnyPlayerAlive() {
		for (Player p : players) {
			if (p.isAlive()) {
				return true;
			}
		}
		return false;
	}

	public boolean isAnyPlayerInHunterMode() {
		for (Player p : players) {
			if (p.getHunterMode()) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Start the Hunter Mode for Pacman.
	 * Start the Feared Mode for Ghosts.
	 */
	public void startHunterMode() {
		Board b = getBoard();
		for (int x = 0; x < b.getWidth(); x++) {
			for (int y = 0; y < b.getHeight(); y++) {
				for (Unit u : b.squareAt(x, y).getOccupants()) {
					if (u instanceof Ghost) {
						timerHunterMode.cancel();
						timerWarning.cancel();
						timerHunterMode = new Timer();
						timerWarning = new Timer();
						if (remainingSuperPellets() >= 2) {
							timerHunterMode.schedule(new TimerHunterTask(), 7000);
							timerWarning.schedule(new TimerWarningTask(), 5000, 250);
						} else {
							timerHunterMode.schedule(new TimerHunterTask(), 5000);
							timerWarning.schedule(new TimerWarningTask(), 3000, 250);
						}
						((Ghost) u).startFearedMode();
					}
				}
			}
		}
		for (Player p : players) {
			p.setHunterMode(false);
			ghostAte = 0;
		}
	}

	/**
	 * Stop the Hunter Mode for Pacman.
	 * Stop the Feared Mode for Ghosts.
	 */
	public void stopHunterMode() {
		Board b = getBoard();
		timerWarning.cancel();
		for (int x = 0; x < b.getWidth(); x++) {
			for (int y = 0; y < b.getHeight(); y++) {
				for (Unit u : b.squareAt(x, y).getOccupants()) {
					if (u instanceof Ghost) {
						((Ghost) u).stopFearedMode();
					}
				}
			}
		}
	}

	/**
	 * Handle the end of the Hunter Mode for Pacman and
	 * warning him about that.
	 */
	public void warningMode()
	{
		Board b = getBoard();
		Ghost.count++;
		for (int x = 0; x < b.getWidth(); x++) {
			for (int y = 0; y < b.getHeight(); y++) {
				for (Unit u : b.squareAt(x, y).getOccupants()) {
					if (u instanceof Ghost) {
						((Ghost) u).warningMode();
					}
				}
			}
		}
	}

	/**
	 * Start the Timer to respawn a ghost after being ate by Pacman.
	 */
	public void respawnGhost()
	{
		ghostLeft++;
		Ghost ateGhost = PlayerCollisions.ateGhost;
		timerRespawn = new Timer();
		timerRespawn.schedule(new TimerRespawnTask(ateGhost), 5000);
	}

	/**
	 * Counts the pellets remaining on the board.
	 * 
	 * @return The amount of pellets remaining on the board.
	 */
	public int remainingPellets() {
		Board b = getBoard();
		int pellets = 0;
		for (int x = 0; x < b.getWidth(); x++) {
			for (int y = 0; y < b.getHeight(); y++) {
				for (Unit u : b.squareAt(x, y).getOccupants()) {
					if (u instanceof Pellet) {
						pellets++;
					}
				}
			}
		}
		return pellets;
	}

	/**
	 * Counts the super pellets remaining on the board.
	 *
	 * @return The amount of super pellets remaining on the board.
	 */
	public int remainingSuperPellets() {
		Board b = getBoard();
		int superPellets = 0;
		for (int x = 0; x < b.getWidth(); x++) {
			for (int y = 0; y < b.getHeight(); y++) {
				for (Unit u : b.squareAt(x, y).getOccupants()) {
					if (u instanceof Pellet && ((Pellet) u).getValue() == 50) {
						superPellets++;
					}
				}
			}
		}
		return superPellets;
	}

	public Map<NPC, ScheduledExecutorService> getNpcs() {
		return npcs;
	}

	public static Level getLevel() {
		return level;
	}

	/**
	 * A task that moves an NPC and reschedules itself after it finished.
	 * 
	 * @author Jeroen Roosen 
	 */
	private final class NpcMoveTask implements Runnable {

		/**
		 * The service executing the task.
		 */
		private final ScheduledExecutorService service;

		/**
		 * The NPC to move.
		 */
		private final NPC npc;

		/**
		 * Creates a new task.
		 * 
		 * @param s
		 *            The service that executes the task.
		 * @param n
		 *            The NPC to move.
		 */
		private NpcMoveTask(ScheduledExecutorService s, NPC n) {
			this.service = s;
			this.npc = n;
		}

		@Override
		public void run() {
			Direction nextMove = npc.nextMove();
			long interval;
			if (nextMove != null) {
				move(npc, nextMove);
			}
			if(((Ghost) npc).getFearedMode()) {
				interval = ((Ghost) npc).getFearedInterval();
			}
			else {
				interval = npc.getInterval();
			}
			service.schedule(this, interval, TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * A task that stop the Hunter Mode after an amount of time.
	 *
	 * @author Yarol Timur
	 */
	private final class TimerHunterTask extends TimerTask {
		@Override
		public void run() {
			stopHunterMode();
		}
	}

	/**
	 * A task that respawn an NPC after being eat by Pacman.
	 *
	 * @author Yarol Timur
	 */
	private final class TimerRespawnTask extends TimerTask {

		private Ghost ghost;

		private TimerRespawnTask(Ghost ghost)
		{
			this.ghost = ghost;
		}

		@Override
		public void run() {
			Board b = getBoard();
			ghost.occupy(b.getMiddleOfTheMap());
			ghost.stopFearedMode();
			stopNPCs();
			startNPCs();
			this.cancel();
		}
	}

	/**
	 * A task that handle the end of Hunter Mode.
	 *
	 * @author Yarol Timur
	 */
	private final class TimerWarningTask extends TimerTask {

		@Override
		public void run() { warningMode(); }
	}

	/**
	 * An observer that will be notified when the level is won or lost.
	 *
	 * @author Jeroen Roosen
	 */
	public interface LevelObserver {

		/**
		 * The level has been won. Typically the level should be stopped when
		 * this event is received.
		 */
		void levelWon();

		/**
		 * The level has been lost. Typically the level should be stopped when
		 * this event is received.
		 */
		void levelLost();

		/**
		 * The level mode change for a while. Pacman become a Hunter and the Ghost are feared.
		 */
		void startHunterMode();

		/**
		 * A ghost need to be respawned.
		 */
		void respawnGhost();
	}
}
