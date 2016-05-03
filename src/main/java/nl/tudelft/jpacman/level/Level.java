package nl.tudelft.jpacman.level;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import nl.tudelft.jpacman.Launcher;
import nl.tudelft.jpacman.board.Board;
import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.fruit.Fruit;
import nl.tudelft.jpacman.fruit.FruitFactory;
import nl.tudelft.jpacman.level.MovableCharacter;
import nl.tudelft.jpacman.level.Bridge;
import nl.tudelft.jpacman.npc.Bullet;
import nl.tudelft.jpacman.npc.NPC;
import nl.tudelft.jpacman.npc.ghost.Blinky;
import nl.tudelft.jpacman.npc.ghost.Ghost;
import nl.tudelft.jpacman.npc.ghost.GhostColor;
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
	 * <code>true</code> iff this level is currently in progress, i.e. players
	 * and NPCs can move.
	 */
	private boolean inProgress;

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
	private FruitFactory fruitFactory;

	/**
	 * The players on this level.
	 */
	private final Map<Player, ScheduledExecutorService> players;


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

	private Timer addGhostTask = new Timer();

	private Timer addFruitTask = new Timer();

	private Timer speedUpTask = new Timer();

	private boolean norm;

	private static int c = 1;

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
		Launcher la = Launcher.getLauncher();
		if(la.getBoardToUse() == "/Board.txt" || la.getBoardToUse() == "BoardFruit.txt"){
			this.norm = true;
		}
		else
		{
			this.norm = false;
		}
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

		if (players.containsKey(p)) {
			return;
		}
		players.put(p, null);
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

		if (!isInProgress() || (unit instanceof MovableCharacter && !((MovableCharacter) unit).isMovable())) {
			return;
		}

		synchronized (moveLock) {
			unit.setDirection(direction);
			Square location = unit.getSquare();
			Square destination = location.getSquareAt(direction);

			if (destination.isAccessibleTo(unit) && !(Bridge.blockedBybridge(unit, direction))) {
				unit.setOnBridge(false);
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
			startCharacters();
			inProgress = true;
			updateObservers();
		}
		Random random = new Random();
		int nbr = random.nextInt(11);
		if(infiniteMode) {
			addGhostTask.schedule(new TimerAddGhostTask(), (nbr+10)*1000);
			speedUpTask.schedule(new TimerSpeedUpTask(), 10000, 10000);
		}
		addFruitTask.schedule(new TimerAddFruitTask(), (nbr+10)*1000); // fuittask
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
			addFruitTask.cancel(); // fruittask
			speedUpTask.cancel();
			inProgress = false;
		}
	}

	/**
	 * Starts all Character movement scheduling.
	 */
	private void startCharacters() {
		MovableCharacter mc;
		for (final Ghost ghost : ghosts.keySet()) {
			mc = ghost;
			ScheduledExecutorService service = Executors
					.newSingleThreadScheduledExecutor();
			service.schedule(new CharacterMoveTask(service, mc),
					mc.getInterval() / 2, TimeUnit.MILLISECONDS);
			ghosts.put(ghost, service);
		}
		for (final Player player : players.keySet()) {
			mc = player;
			ScheduledExecutorService service = Executors
					.newSingleThreadScheduledExecutor();
			service.schedule(new CharacterMoveTask(service, mc),
					mc.getInterval() / 2, TimeUnit.MILLISECONDS);
			players.put(player, service);
		}
		for (final Bullet bullet : bullets.keySet()) {
			mc = bullet;
			ScheduledExecutorService service = Executors
					.newSingleThreadScheduledExecutor();
			service.schedule(new CharacterMoveTask(service, mc),
					mc.getInterval() / 2, TimeUnit.MILLISECONDS);
			bullets.put(bullet, service);
		}
	}

	/**
	 * Stops all NPC movement scheduling and interrupts any movements being
	 * executed.
	 */
	private void stopCharacters() {
		for (Entry<Ghost, ScheduledExecutorService> e : ghosts.entrySet()) {
			e.getValue().shutdownNow();
		}
		for (Entry<Player, ScheduledExecutorService> e : players.entrySet()) {
			e.getValue().shutdownNow();
		}
		for (Entry<Bullet, ScheduledExecutorService> e : bullets.entrySet()) {
			e.getValue().shutdownNow();
		}
	}

	/**
	 * Permet d'ajouter des ghosts dans le jeu
	 */
	public void addGhostTask()
	{
		if(this.ghosts.size() < 10) {
			System.out.println("ghost en vie début : " + this.ghosts.size());
			ScheduledExecutorService service = Executors
					.newSingleThreadScheduledExecutor();
			GhostFactory ghostFact = new GhostFactory(SPRITE_STORE);
			Random random = new Random();
			int nombre = random.nextInt(6);
			int ghostIndex = random.nextInt(4);
			addGhostTask.cancel();
			addGhostTask = new Timer();
			addGhostTask.schedule(new TimerAddGhostTask(), ((nombre + 4) + this.ghosts.size()) * 1000);
			Ghost g;
			switch (ghostIndex) {
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
			ghosts.put(g, service);
			Square squareGhost = null;
			while (squareGhost == null) {
				Square posPlayer = players.keySet().iterator().next().getSquare();
				int X = posPlayer.getCoordX();
				int Y = posPlayer.getCoordY();
				int i, j;
				if(X-10 < 0){
					i =  random.nextInt(board.getWidthOfOneMap()-1);
				}
				else{
					i = (X-10) + random.nextInt(board.getWidthOfOneMap()-1);
				}
				if(Y-14 < 0){
					j = random.nextInt(4);
				}
				else{
					j = (Y-14) + random.nextInt(4);
				}
				squareGhost = board.squareAt(i, j);
				if (squareGhost.isAccessibleTo(g)) {
					g.occupy(squareGhost);
				}
				else{
					squareGhost = null;
				}
			}
			stopCharacters();
			startCharacters();
			System.out.println("ghost en vie fin : " + this.ghosts.size());
		}
	}


	private void speedUpTask(){
		Ghost g;
		for (MovableCharacter npc : ghosts.keySet()) {
			g = (Ghost) (npc);
			g.setSpeed(g.getSpeed() + 0.05);
		}
	}

	/**
	 * Permet d'ajouter des fruits dans le jeu
	 */
	public void addFruitTask()
	{
		Random random = new Random();
		int nbr = random.nextInt(6);
		addFruitTask.cancel();
		addFruitTask = new Timer();
		addFruitTask.schedule(new TimerAddFruitTask(), (nbr+10)*1000);
		Fruit fruit = fruitFactory.getRandomFruit();
		Square squareFruit = null;
		while(squareFruit == null) {
			Player p = players.keySet().iterator().next();
			Square posPlayer = p.getSquare();
			int X, Y;
			if(this.norm){
				X = posPlayer.getCoordX();
				Y = posPlayer.getCoordY();
			}
			else{
				X = 0;
				Y = 0;
			}
			int i, j;
			if(X-10 < 0){
				i =  random.nextInt(board.getWidthOfOneMap()-2);
			}
			else{
				i = (X-10) + random.nextInt(board.getWidthOfOneMap()-2);
			}
			if(Y-14 < 0){
				j = random.nextInt(board.getHeightOfOneMap()-2);
			}
			else{
				j = (Y-14) + random.nextInt(board.getHeightOfOneMap()-2);
			}
			squareFruit = board.squareAt(i, j);
			if (Navigation.shortestPath(posPlayer, squareFruit, p) != null) {
				fruit.occupy(squareFruit);
				TimerTask timerTask = new TimerTask() {
					public void run() {
						fruit.leaveSquare();
					}
				};
				Timer timer = new Timer();
				timer.schedule(timerTask, fruit.getLifetime() * 1000);
			}
			else {
				squareFruit = null;
			}
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
	 * Returns <code>true</code> if at least one NPC is dead and need to be cleaned from the board.
	 *
	 * @return <code>true</code> if at least one NPC is dead and need to be cleaned from the board.
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
		Ghost.ghostLeft++;
		Ghost ateGhost = PlayerCollisions.ateGhost.get(PlayerCollisions.ateGhost.size()-1);
		PlayerCollisions.ateGhost.remove(PlayerCollisions.ateGhost.size()-1);
		timerRespawn = new Timer();
		timerRespawn.schedule(new TimerRespawnTask(ateGhost), 5000);
	}

	public void respawnParticularGhost(Ghost ghost)
	{
		timerRespawn = new Timer();
		timerRespawn.schedule(new TimerRespawnTask(ghost), 5000);
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
	 * returns the fruit factory for this level.
	 * @return the fruit factory for this level.
	 */
	public FruitFactory getFruitFactory(){
		return fruitFactory;
	}

	public Map<Ghost, ScheduledExecutorService> getGhosts() {
		return ghosts;
	}

	public static Level getLevel() {
		return level;
	}

	/**
	 * A task that moves an NPC and reschedules itself after it finished.
	 *
	 * @author Jeroen Roosen
	 */
	private final class CharacterMoveTask implements Runnable {

		/**
		 * The service executing the task.
		 */
		private final ScheduledExecutorService service;

		/**
		 * The NPC to move.
		 */
		private final MovableCharacter character;

		/**
		 * Creates a new task.
		 *
		 * @param s
		 *            The service that executes the task.
		 * @param c
		 *            The NPC to move.
		 */
		private CharacterMoveTask(ScheduledExecutorService s, MovableCharacter c) {
			this.service = s;
			this.character = c;
		}

		@Override
		public void run() {
			Direction nextMove = character.nextMove();
			long interval;
			if (nextMove != null) {
				move(character, nextMove);
			}

			//Ce code est dégeux et devrait être déplacé dans la méthode getInterval de chaque fantômes.
			if(character instanceof Ghost && ((Ghost) character).getFearedMode()) {
				interval = ((Ghost) character).getFearedInterval();
			}

			else {
				interval = character.getInterval();
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
			ghost.setExplode(false);
			ghost.occupy(b.getMiddleOfTheMap());
			ghost.stopFearedMode();
			stopCharacters();
			startCharacters();
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
	 * A task that handle the end of Hunter Mode.
	 *
	 * @author Yarol Timur
	 */
	private final class TimerAddGhostTask extends TimerTask {

		@Override
		public void run() { addGhostTask(); }
	}

	private final class TimerAddFruitTask extends TimerTask {

		@Override
		public void run() { addFruitTask(); }
	}

	private final class TimerSpeedUpTask extends TimerTask {

		@Override
		public void run() { speedUpTask(); }
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

		/**
		 * A Player can shoot bullets
		 */
		void ShootingEvent();

		/**
		 * A NPC is dead and need to be cleared from the board
		 * @param deadBullets the list of the NPCs that are dead
		 * @param bullet the npcs that are still in the game.
		 */
		void bulletCleanEvent(List<Bullet> deadBullets, Map<Bullet, ScheduledExecutorService> bullet);
	}

	/**
	 * enable the movment of a bullet
	 * @param b the bullet that have to be moved.
	 */
	public void animateBullet(Bullet b) {
		MovableCharacter mc = (MovableCharacter) b;
		ScheduledExecutorService service = Executors
				.newSingleThreadScheduledExecutor();
		service.schedule(new CharacterMoveTask(service, mc),
				mc.getInterval() / 2, TimeUnit.MILLISECONDS);
		bullets.put(b, service);
	}
}