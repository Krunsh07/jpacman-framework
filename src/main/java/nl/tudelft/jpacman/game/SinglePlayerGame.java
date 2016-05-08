package nl.tudelft.jpacman.game;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;

import nl.tudelft.jpacman.level.Level;
import nl.tudelft.jpacman.level.MovableCharacter;
import nl.tudelft.jpacman.level.Player;
import nl.tudelft.jpacman.npc.Bullet;
import nl.tudelft.jpacman.npc.ghost.Ghost;
import nl.tudelft.jpacman.sprite.PacManSprites;

import com.google.common.collect.ImmutableList;

/**
 * A game with one player and a single level.
 * 
 * @author Jeroen Roosen 
 */
public class SinglePlayerGame extends Game {

	/**
	 * The player of this game.
	 */
	private final Player player;

	/**
	 * The level of this game.
	 */
	private final Level level;

	/**
	 * A lock that prevent a bullet from being created on the board, when the lock value is true,
	 * a bullet can appear on the board and false when a bullet can't appear.
	 */
	private boolean shootLock = true;
	/**
	 * Create a new single player game for the provided level and player.
	 * 
	 * @param p
	 *            The player.
	 * @param l
	 *            The level.
	 */
	protected SinglePlayerGame(Player p, Level l) {
		assert p != null;
		assert l != null;

		this.player = p;
		this.level = l;
		level.registerPlayer(p);
	}

	@Override
	public List<Player> getPlayers() {
		return ImmutableList.of(player);
	}

	@Override
	public Level getLevel() {
		return level;
	}


	@Override
	public void ShootingEvent() {
		if(shootLock){
			shootLock = false;
			Bullet b = new Bullet(new PacManSprites().getBulletSprite(), player);
			b.occupy(player.getSquare());
			level.animateBullet(b);
			TimerTask timerTask = new TimerTask() {
		        public void run() {
		        	shootLock = true;
		        }
		    };
		    Timer timer = new Timer();
		    timer.schedule(timerTask, b.getBulletDelay() * 1000);
		}
	}

	public void ghostCleanEvent(List<Ghost> deadNPCs, Map<Ghost, ScheduledExecutorService> npcs) {
		Timer timer;
		for(MovableCharacter npc : deadNPCs) {
			TimerTask timerTask = new TimerTask() {
			    public void run() {
			    	npc.leaveSquare();
			    	npcs.remove(npc);
			    }
			};
			int deadGhostAnimationTime = 5 * 200;
			timer = new Timer();
			timer.schedule(timerTask, deadGhostAnimationTime);
		}
	}
	
	public void bulletCleanEvent(List<Bullet> deadBullets, Map<Bullet, ScheduledExecutorService> bullets) {
		for(MovableCharacter bullet : deadBullets) {
		    bullets.get(bullet).shutdownNow();
		    bullet.leaveSquare();
		    bullets.remove(bullet);
		}
	}
}
