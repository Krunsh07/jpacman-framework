package nl.tudelft.jpacman.level;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.fruit.Fruit;
import nl.tudelft.jpacman.npc.Bullet;
import nl.tudelft.jpacman.npc.ghost.Ghost;
import nl.tudelft.jpacman.level.Level;

/**
 * A simple implementation of a collision map for the JPacman player.
 * <p>
 * It uses a number of instanceof checks to implement the multiple dispatch for the 
 * collisionmap. For more realistic collision maps, this approach will not scale,
 * and the recommended approach is to use a {@link CollisionInteractionMap}.
 * 
 * @author Arie van Deursen, 2014
 *
 */

public class PlayerCollisions implements CollisionMap {

	/**
	 * The number of Ghost eat by Pacman during a Hunter Mode.
	 */
	public static ArrayList<Ghost> ateGhost = new ArrayList<>();

	@Override
	public void collide(Unit mover, Unit collidedOn) {
		System.out.println(mover.isOnBridge()+ " vs. "+collidedOn.isOnBridge());
		if(mover.isOnBridge() == collidedOn.isOnBridge()){
			if (mover instanceof Player) {
				playerColliding((Player) mover, collidedOn);
			}
			else if (mover instanceof Ghost) {
				ghostColliding((Ghost) mover, collidedOn);
			}
			else if (mover instanceof Bullet) {
				BulletColliding((Bullet) mover, collidedOn);
			}
		}
	}

	private void BulletColliding(Bullet mover, Unit collidedOn) {
		if (collidedOn instanceof Ghost) {
			ghostVersusBullet((Ghost) collidedOn, mover);
		}
		if (collidedOn instanceof Bridge) {
			characterVersusBridge(mover, (Bridge) collidedOn);
		}
	}

	private void playerColliding(Player player, Unit collidedOn) {
		if (collidedOn instanceof Ghost && !(player.isInvincible())) {
			if (((Ghost) collidedOn).getFearedMode()){
				playerVersusEatableGhost(player, (Ghost) collidedOn);
			}
			else {
				playerVersusGhost(player, (Ghost) collidedOn);
			}
		}
		if (collidedOn instanceof Pellet) {
			playerVersusPellet(player, (Pellet) collidedOn);
		}
		if (collidedOn instanceof Hole) {
			characterVersusHole(player, (Hole) collidedOn);
		}
		if (collidedOn instanceof Teleport) {
			playerVersusTeleport(player, (Teleport) collidedOn);
		}
		if (collidedOn instanceof Bridge) {
			characterVersusBridge(player, (Bridge) collidedOn);
		}
		if (collidedOn instanceof Fruit) {
			playerVersusFruit(player, (Fruit) collidedOn);
		}
	}

	private void ghostColliding(Ghost ghost, Unit collidedOn) {
		if (ghost.getFearedMode() && collidedOn instanceof Player) {
			playerVersusEatableGhost((Player) collidedOn, ghost);
		}
		else if (collidedOn instanceof Player) {
			playerVersusGhost((Player) collidedOn, ghost);
		}
		if (collidedOn instanceof Hole) {
			characterVersusHole(ghost, (Hole) collidedOn);
		}
		if (collidedOn instanceof Bridge) {
			characterVersusBridge(ghost, (Bridge) collidedOn);
		}
		if (collidedOn instanceof Bullet && ((Bullet)collidedOn).isAlive()) {
			ghostVersusBullet(ghost, (Bullet) collidedOn);
		}
	}

	private void ghostVersusBullet(Ghost ghost, Bullet collidedOn) {
		collidedOn.setAlive(false);
		if(!(ghost.hasExploded())) {
			ghost.setExplode(true);
			TimerTask timerTask = new TimerTask() {
			    public void run() {
			    	ateGhost.add(ghost);
			    	ghost.leaveSquare();
					Ghost.ghostLeft--;
                }
			};
			int deadGhostAnimationTime = 5 * 200;
			Timer timer = new Timer();
			timer.schedule(timerTask, deadGhostAnimationTime);
		}
	}

	/**
	 * Normal case of player bumping into ghost or vice versa.
     *
     * @param player The player involved in the collision.
	 */
	public void playerVersusGhost(Player player, Ghost ghost) {
		if(!ghost.hasExploded()){
			player.setAlive(false);
		}
	}


	/**
	 * Special case of player bumping into ghost or vice versa.
	 * In this case, the player is under the Hunter Mode.
	 * He is able to eat Ghost.
	 *
	 * @param player The player involved in the collision.
	 * @param ghost The ghost involved in the collision.
	 */
	public void playerVersusEatableGhost(Player player, Ghost ghost)
	{
		ateGhost.add(ghost);
		ghost.leaveSquare();
		Ghost.ghostLeft--;
		Ghost.ghostAte++;
		if(Ghost.ghostAte == 1){
			player.addPoints(200);
		}
		if(Ghost.ghostAte == 2) {
			player.addPoints(400);
		}
		if(Ghost.ghostAte == 3) {
			player.addPoints(800);
		}
		if(Ghost.ghostAte == 4) {
			player.addPoints(1600);
		}
	}
	
	/**
	 * Actual case of player consuming a pellet.
     *
     * @param player The player involved in the collision.
     * @param pellet The pellet involved in the collision.
	 */
	public void playerVersusPellet(Player player, Pellet pellet) {
		pellet.leaveSquare();
		player.addPoints(pellet.getValue());
		if(pellet.getValue() == 50) {
			player.setHunterMode(true);
		}
	}

	/**
	 * Actual case of A player or a ghost falling into a hole.
     *
     * @param unit The unit involved in the collision.
     * @param hole The hole involved in the collision.
	 */
	public void characterVersusHole(Unit unit, Hole hole) {
		if(unit instanceof MovableCharacter) {
			hole.leaveSquare();
			hole.effect((MovableCharacter) unit);
		}
	}
	
	/**
	 * Actual case of A player entering into a teleport
     *
     * @param unit The player involved in the collision.
     * @param teleport The pellet involved in the collision.
	 */
	public void playerVersusTeleport(Player unit, Teleport teleport) {
		teleport.effect(unit, this);
	}
	
	/**
	 * Actual case of A player or a ghost falling into a hole.
     *
     * @param unit The unit involved in the collision.
     * @param bridge The bridge involved in the collision.
	 */
	public void characterVersusBridge(Unit unit, Bridge bridge) {
		bridge.effect(unit);
	}
	
	/**
	 * Actual case of player consuming a fruit.
     *
     * @param player The player involved in the collision.
     * @param fruit The fruit involved in the collision.
	 */
	public void playerVersusFruit(Player player, Fruit fruit) {
		fruit.leaveSquare();
		fruit.fruitEffect(player);
	}
}
