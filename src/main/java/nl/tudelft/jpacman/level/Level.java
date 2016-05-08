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
<<<<<<< HEAD
	 * The players on this level.
	 */
	private final Map<Player, ScheduledExecutorService> players;


	/**
=======
>>>>>>> 0bc64a59ca22559bb0064c5c82e966cb2a0bc720
	 * The table of possible collisions between units.
	 */
	private final CollisionMap collisions;

	/**
	 * The objects observing this level.
	 */
	private final List<LevelObserver> observers;

	private static final PacManSprites SPRITE_STORE = new PacManSprites();

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

<<<<<<< HEAD
	private boolean norm;

	private static int c = 1;
=======
	private static Level level;
>>>>>>> 0bc64a59ca22559bb0064c5c82e966cb2a0bc720

	private Random random;

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
<<<<<<< HEAD
		Launcher la = Launcher.getLauncher();
		if(la.getBoardToUse() == "/Board.txt" || la.getBoardToUse() == "BoardFruit.txt"){
			this.norm = true;
		}
		else
		{
			this.norm = false;
		}
=======
		random = new Random();
>>>>>>> 0bc64a59ca22559bb0064c5c82e966cb2a0bc720
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
		int nbr = random.nextInt(11);
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
			ScheduledExecutorService service = Executors
					.newSingleThreadScheduledExecutor();
			GhostFactory ghostFact = new GhostFactory(SPRITE_STORE);
			int nbr = random.nextInt(6);
			int ghostIndex = random.nextInt(4);
			addGhostTask.cancel();
			addGhostTask = new Timer();
			addGhostTask.schedule(tks.createAddGhostTask(), ((nbr + 4) + this.ghosts.size()) * 1000);
			Ghost g = Ghost.addGhost(ghostFact, ghostIndex);
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
		int nbr = random.nextInt(6);
		addFruitTask.cancel();
		addFruitTask = new Timer();
		addFruitTask.schedule(tks.createAddFruitTask(), (nbr+10)*1000);
		Fruit fruit = fruitFactory.getRandomFruit();
		Square squareFruit = null;
		Player p = players.keySet().iterator().next();
		Square posPlayer = p.getSquare();
		while(squareFruit == null) {
<<<<<<< HEAD
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
=======
			squareFruit = addUnitOnSquare(board.getWidthOfOneMap()-2, board.getHeightOfOneMap()-2);
			if (Navigation.shortestPath(posPlayer, squareFruit, p) != null) {
				fruit.occupy(squareFruit);
				timerTask = new TimerTask() {
>>>>>>> 0bc64a59ca22559bb0064c5c82e966cb2a0bc720
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

	public Square addUnitOnSquare(int random1, int random2) {
		Random random = new Random();
		int X, Y;
		int i, j;
		Square posPlayer = players.keySet().iterator().next().getSquare();
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
		for (MovableCharacter npc : ghosts.keySet()) {
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
<<<<<<< HEAD

	public void respawnParticularGhost(Ghost ghost)
	{
=======
	
	public void respawnParticularGhost(Ghost ghost) {
>>>>>>> 0bc64a59ca22559bb0064c5c82e966cb2a0bc720
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
<<<<<<< HEAD

	/**
	 * returns the fruit factory for this level.
	 * @return the fruit factory for this level.
	 */
	public FruitFactory getFruitFactory(){
		return fruitFactory;
	}
=======
>>>>>>> 0bc64a59ca22559bb0064c5c82e966cb2a0bc720

	public Map<Ghost, ScheduledExecutorService> getGhosts() {
		return ghosts;
	}

	public static Level getLevel() {
		return level;
	}

<<<<<<< HEAD
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
=======
>>>>>>> 0bc64a59ca22559bb0064c5c82e966cb2a0bc720

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