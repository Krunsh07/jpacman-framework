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
import nl.tudelft.jpacman.fruit.Fruit;
import nl.tudelft.jpacman.fruit.FruitFactory;
import nl.tudelft.jpacman.npc.Bullet;
import nl.tudelft.jpacman.npc.NPC;
import nl.tudelft.jpacman.npc.ghost.Ghost;
import nl.tudelft.jpacman.npc.ghost.GhostFactory;
import nl.tudelft.jpacman.npc.ghost.Navigation;
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
	private final Map<Ghost, ScheduledExecutorService> ghosts;

	/**
	 * The NPCs of this level and, if they are running, their schedules.
	 */
	private final Map<Bullet, ScheduledExecutorService> bullets;

	/**
	 * The players on this level.
	 */
	private final Map<Player, ScheduledExecutorService> players;


	/**
	 * <code>true</code> iff this level is currently in progress, i.e. players
	 * and NPCs can move.
	 */
	private boolean inProgress;

	/**
	 * To know if the game is in infinite map
	 */
	public boolean infiniteMode;

	/**
	 * The squares from which players can start this game.
	 */
	private final List<Square> startSquares;

	/**
	 * The start current selected starting square.
	 */
	private int startSquareIndex;

	/**
	 * The Fruit factory for this level.
	 */
	private final FruitFactory fruitFactory;

	/**
	 * The table of possible collisions between units.
	 */
	private final CollisionMap collisions;

	/**
	 * The objects observing this level.
	 */
	private final List<LevelObserver> observers;

	/**
	 * The sprite store
	 */
	private static final PacManSprites SPRITE_STORE = new PacManSprites();

	/**
	 * To create all the timer
	 */
	private TimerTasks tks = new TimerTasks();

	/**
	 * Timer used to handle the game;
	 */
	private Timer timerHunterMode;
	private Timer timerRespawn;
	private Timer timerWarning;
	private Timer addGhostTask;
	private Timer addFruitTask;
	private Timer speedUpTask;

	/**
	 * The level of the game
	 */
	private static Level level;

	/**
	 * To generate random number
	 */
	private final Random random;

	/**
	 * Creates a new level for the board.
	 * @param b The board for the level.
	 * @param ghosts The ghosts on the board.
	 * @param startPositions The squares on which players start on this board.
	 * @param collisionMap The collection of collisions that should be handled.
	 */
	public Level(Board b, List<NPC> ghosts, List<Square> startPositions,
				 CollisionMap collisionMap) {
		assert b != null;
		assert ghosts != null;
		assert startPositions != null;

		this.fruitFactory = new FruitFactory(SPRITE_STORE, this);
		this.board = b;
		this.inProgress = false;
		this.ghosts = new HashMap<>();
		for (NPC g : ghosts) {
			Ghost ghost = (Ghost) g;
			this.ghosts.put(ghost, null);
			Ghost.ghostLeft++;
		}
		this.bullets = new HashMap<>();
		this.startSquares = startPositions;
		this.startSquareIndex = 0;
		this.players = new HashMap<>();
		this.collisions = collisionMap;
		this.observers = new ArrayList<>();
		random = new Random();
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

		if (players.containsKey(p)) {
			return;
		}
		players.put(p, null);
		final Square square = startSquares.get(startSquareIndex);
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
	 * @param unit The unit to move.
	 * @param direction The direction to move the unit in.
	 */
	public void move(Unit unit, Direction direction) {
		assert unit != null;
		assert direction != null;

		if (!isInProgress() || (unit instanceof MovableCharacter && !((MovableCharacter) unit).isMovable())) {
			return;
		}

		synchronized (moveLock) {
			unit.setDirection(direction);
			final Square location = unit.getSquare();
			final Square destination = location.getSquareAt(direction);

			if (destination.isAccessibleTo(unit) && !(Bridge.blockedBybridge(unit, direction))) {
				unit.setOnBridge(false);
				final List<Unit> occupants = destination.getOccupants();
				unit.occupy(destination);
				for (final Unit occupant : occupants) {
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
			startCharacters();
			inProgress = true;
			updateObservers();
		}
		final int nbr = random.nextInt(11);
		timerRespawn = new Timer();
		timerWarning = new Timer();
		timerHunterMode = new Timer();
		addFruitTask = new Timer();
		addGhostTask = new Timer();
		speedUpTask = new Timer();
		if(infiniteMode) {
			addGhostTask.schedule(tks.createAddGhostTask(), (nbr+10)*1000);
			speedUpTask.schedule(tks.createSpeedUpTask(), 10000, 10000);
		}
		addFruitTask.schedule(tks.createAddFruitTask(), (nbr+10)*1000);
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
			stopCharacters();
			addGhostTask.cancel();
			addFruitTask.cancel();
			speedUpTask.cancel();
			inProgress = false;
		}
	}

	/**
	 * Starts all Character movement scheduling.
	 */
	public void startCharacters() {
		MovableCharacter mc;
		ScheduledExecutorService service;
		for (final Ghost ghost : ghosts.keySet()) {
			mc = ghost;
			service = Executors
					.newSingleThreadScheduledExecutor();
			service.schedule(tks.createCharacterMoveTask(service, mc),
					mc.getInterval() / 2, TimeUnit.MILLISECONDS);
			ghosts.put(ghost, service);
		}
		for (final Player player : players.keySet()) {
			mc = player;
			service = Executors
					.newSingleThreadScheduledExecutor();
			service.schedule(tks.createCharacterMoveTask(service, mc),
					mc.getInterval() / 2, TimeUnit.MILLISECONDS);
			players.put(player, service);
		}
		for (final Bullet bullet : bullets.keySet()) {
			mc = bullet;
			service = Executors
					.newSingleThreadScheduledExecutor();
			service.schedule(tks.createCharacterMoveTask(service, mc),
					mc.getInterval() / 2, TimeUnit.MILLISECONDS);
			bullets.put(bullet, service);
		}
	}

	/**
	 * Stops all NPC movement scheduling and interrupts any movements being
	 * executed.
	 */
	public void stopCharacters() {
		for (final Entry<Ghost, ScheduledExecutorService> e : ghosts.entrySet()) {
			e.getValue().shutdownNow();
		}
		for (final Entry<Player, ScheduledExecutorService> e : players.entrySet()) {
			e.getValue().shutdownNow();
		}
		for (final Entry<Bullet, ScheduledExecutorService> e : bullets.entrySet()) {
			e.getValue().shutdownNow();
		}
	}

	/**
	 * Permet d'ajouter des ghosts dans le jeu
	 */
	public void addGhostTask()
	{
		if(this.ghosts.size() < 10) {
			final ScheduledExecutorService service = Executors
					.newSingleThreadScheduledExecutor();
			final GhostFactory ghostFact = new GhostFactory(SPRITE_STORE);
			final int nbr = random.nextInt(6);
			final int ghostIndex = random.nextInt(4);
			addGhostTask.cancel();
			addGhostTask = new Timer();
			addGhostTask.schedule(tks.createAddGhostTask(), ((nbr + 4) + this.ghosts.size()) * 1000);
			final Ghost g = Ghost.addGhost(ghostFact, ghostIndex);
			ghosts.put(g, service);
			Square squareGhost = null;
			while(squareGhost  == null) {
				squareGhost = addUnitOnSquare(board.getWidthOfOneMap(), 4);
				if (squareGhost.isAccessibleTo(g)) {
					g.occupy(squareGhost);
				}
				else{
					squareGhost = null;
				}
			}
			stopCharacters();
			startCharacters();
		}
	}

	/**
	 * Permet d'ajouter des fruits dans le jeu
	 */
	public void addFruitTask()
	{
		Timer timer = new Timer();
		TimerTask timerTask;
		final int nbr = random.nextInt(6);
		addFruitTask.cancel();
		addFruitTask = new Timer();
		addFruitTask.schedule(tks.createAddFruitTask(), (nbr+10)*1000);
		final Fruit fruit = fruitFactory.getRandomFruit();
		Square squareFruit = null;
		final Player p = players.keySet().iterator().next();
		final Square posPlayer = p.getSquare();
		while(squareFruit == null) {
			squareFruit = addUnitOnSquare(board.getWidthOfOneMap()-2, board.getHeightOfOneMap()-2);
			if (Navigation.shortestPath(posPlayer, squareFruit, p) != null) {
				fruit.occupy(squareFruit);
				timerTask = new TimerTask() {
					public void run() {
						fruit.leaveSquare();
					}
				};
				timer.schedule(timerTask, fruit.getLifetime() * 1000);
			}
			else {
				squareFruit = null;
			}
		}
	}

	/**
	 * Permet de mettre une unit sur le plateau
	 * @param random1 L'abscisse
	 * @param random2 L'ordonnée
     * @return Le square sur lequel mettre l'unit
     */
	public Square addUnitOnSquare(int random1, int random2) {
		final Random random = new Random();
		int X;
		int Y;
		int i;
		int j;
		final Square posPlayer = players.keySet().iterator().next().getSquare();
		X = posPlayer.getCoordX();
		Y = posPlayer.getCoordY();

		i = Math.max(1+random.nextInt(random1), (X-(board.getWidthOfOneMap()-1)/2)+random.nextInt(random1));
		j = Math.max(1+random.nextInt(random2), (Y-(board.getHeightOfOneMap()-1)/2)+random.nextInt(random2));

		/*if (X - (board.getWidthOfOneMap()-1)/2 < 0) {
			i = random.nextInt(random1);
		} else {
			i = ((board.getWidthOfOneMap()-1)/2) + random.nextInt(random1);
		}
		if (Y - (board.getHeightOfOneMap()-1)/2 < 0) {
			j = random.nextInt(random2);
		} else {
			j = ((board.getHeightOfOneMap()-1)/2) + random.nextInt(random2);
		}*/
		return board.squareAt(i, j);
	}



	/**
	 * Permet d'augmenter la vitesse des fantomes
	 */
	public void speedUpTask(){
		Ghost g;
		for (final MovableCharacter npc : ghosts.keySet()) {
			g = (Ghost) (npc);
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
		if(!infiniteMode) {
			if (Ghost.ghostLeft != 4) {
				for (LevelObserver o : observers) {
					o.respawnGhost();
				}
			}
			if (remainingPellets() == 0) {
				for (LevelObserver o : observers) {
					o.levelWon();
				}
			}
		}
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
		if (isAnyPlayerShooting()) {
			for (LevelObserver o : observers) {
				o.ShootingEvent();
			}
		}
		List<Bullet> deadBullets = BulletToClean() ;
		if(deadBullets.size() > 0) {
			for (LevelObserver o : observers) {
				o.bulletCleanEvent(deadBullets, bullets);
			}
		}
	}

	/**
	 * Returns <code>true</code> iff at least one of the players in this level
	 * is alive.
	 *
	 * @return <code>true</code> if at least one of the registered players is
	 *         alive.
	 */
	public boolean isAnyPlayerAlive() {
		for (Player p : players.keySet()) {
			if (p.isAlive()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if at lest one of the player can shoot bullets.
	 *
	 * @return <code>true</code> if at lest one of the player can shoot bullets.
	 */
	public boolean isAnyPlayerShooting() {
		for (Player p : players.keySet()) {
			if (p.isShooting()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if at least one NPC is dead
	 * and need to be cleaned from the board.
	 * @return <code>true</code> if at least one NPC is dead
	 * and need to be cleaned from the board.
	 */
	private List<Bullet> BulletToClean() {
		List<Bullet> deadBullets = new ArrayList<>();
		for (Bullet bullet : bullets.keySet()) {
			if (!bullet.isAlive()) {
				deadBullets.add(bullet);
			}
		}
		return deadBullets;
	}

	/**
	 * Permet de savoir si un joueur est en mode Hunter
	 * @return true si un joueur est en mode Hunter
     */
	public boolean isAnyPlayerInHunterMode() {
		for (Player p : players.keySet()) {
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
		Pellet.superPelletLeft--;
		for (int x = 0; x < b.getWidth(); x++) {
			for (int y = 0; y < b.getHeight(); y++) {
				for (Unit u : b.squareAt(x, y).getOccupants()) {
					if (u instanceof Ghost) {
						timerHunterMode.cancel();
						timerWarning.cancel();
						timerHunterMode = new Timer();
						timerWarning = new Timer();
						if (Pellet.superPelletLeft >= 2) {
							timerHunterMode.schedule(tks.createStopHunterModeTask(), 7000);
							timerWarning.schedule(tks.createWarningTask(), 5000, 250);
						} else {
							timerHunterMode.schedule(tks.createStopHunterModeTask(), 5000);
							timerWarning.schedule(tks.createWarningTask(), 3000, 250);
						}
						((Ghost) u).startFearedMode();
					}
				}
			}
		}
		for (Player p : players.keySet()) {
			p.setHunterMode(false);
			Ghost.ghostAte = 0;
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
	public void warningMode() {
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
	public void respawnGhost() {
		Ghost.ghostLeft++;
		Ghost ateGhost = PlayerCollisions.ateGhost.get(PlayerCollisions.ateGhost.size()-1);
		PlayerCollisions.ateGhost.remove(PlayerCollisions.ateGhost.size()-1);
		timerRespawn = new Timer();
		timerRespawn.schedule(tks.createRespawnTask(ateGhost), 5000);
	}

	/**
	 * Permet de remettre un ghost sur le jeu
	 * @param ghost
     */
	public void respawnParticularGhost(Ghost ghost) {
		timerRespawn = new Timer();
		timerRespawn.schedule(tks.createRespawnTask(ghost), 5000);
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
	 * Permet de récupérer la liste des ghosts
	 * @return Les ghosts
     */
	public Map<Ghost, ScheduledExecutorService> getGhosts() {
		return ghosts;
	}

	/**
	 * Return the level
	 * @return The level
     */
	public static Level getLevel() {
		return level;
	}

	/**
	 * enable the movment of a bullet
	 * @param b the bullet that have to be moved.
	 */
	public void animateBullet(Bullet b) {
		MovableCharacter mc = b;
		ScheduledExecutorService service = Executors
				.newSingleThreadScheduledExecutor();
		service.schedule(tks.createCharacterMoveTask(service, mc),
				mc.getInterval() / 2, TimeUnit.MILLISECONDS);
		bullets.put(b, service);
	}
}