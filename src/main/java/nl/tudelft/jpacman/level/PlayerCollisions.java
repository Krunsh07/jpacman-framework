package nl.tudelft.jpacman.level;

import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.BridgePosition;
import nl.tudelft.jpacman.npc.ghost.Ghost;
import nl.tudelft.jpacman.board.Square;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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

	@Override
	public void collide(Unit mover, Unit collidedOn) {
		if(mover.getBridgePosition() == collidedOn.getBridgePosition()){
			if (mover instanceof Player) {
				playerColliding((Player) mover, collidedOn);
			}
			else if (mover instanceof Ghost) {
				ghostColliding((Ghost) mover, collidedOn);
			}
		}
	}
	
	private void playerColliding(Player player, Unit collidedOn) {
		if (collidedOn instanceof Ghost) {
			playerVersusGhost(player, (Ghost) collidedOn);
		}
		if (collidedOn instanceof Pellet) {
			playerVersusPellet(player, (Pellet) collidedOn);
		}
		if (collidedOn instanceof Hole) {
			characterVersusHole((Unit) player, (Hole) collidedOn);
		}
		if (collidedOn instanceof Teleport) {
			playerVersusTeleport(player, (Teleport) collidedOn);
		}
		if (collidedOn instanceof Bridge) {
			characterVersusBridge((Unit) player, (Bridge) collidedOn);
		}
	}
	
	private void ghostColliding(Ghost ghost, Unit collidedOn) {
		if (collidedOn instanceof Player) {
			playerVersusGhost((Player) collidedOn, ghost);
		}
		if (collidedOn instanceof Hole) {
			characterVersusHole((Unit) ghost, (Hole) collidedOn);
		}
		if (collidedOn instanceof Bridge) {
			characterVersusBridge((Unit) ghost, (Bridge) collidedOn);
		}
	}
	
	
	/**
	 * Actual case of player bumping into ghost or vice versa.
     *
     * @param player The player involved in the collision.
     * @param ghost The ghost involved in the collision.
	 */
	public void playerVersusGhost(Player player, Ghost ghost) {
		player.setAlive(false);
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
	}

	/**
	 * Actual case of A player or a ghost falling into a hole.
     *
     * @param player The player involved in the collision.
     * @param hole The hole involved in the collision.
	 */
	public void characterVersusHole(Unit unit, Hole hole) {
		int timeMilisec = hole.getTrapTime() * 1000;
		hole.leaveSquare();
		if (unit instanceof Player || unit instanceof Ghost) {
			unit.setMobility(false);
			TimerTask timerTask = new TimerTask() {
		        public void run() {
		        	unit.setMobility(true);
		        }
		    };
		    Timer timer = new Timer();
		    timer.schedule(timerTask, timeMilisec);
		}
	}
	
	/**
	 * Actual case of A player entering into a teleport
     *
     * @param player The player involved in the collision.
     * @param teleport The pellet involved in the collision.
	 */
	public void playerVersusTeleport(Player unit, Teleport teleport) {
		Square s = teleport.getReference();
		if(s.isAccessibleTo(unit))
		{
			unit.occupy(s);
			List<Unit> occupants = s.getOccupants();
			for (Unit occupant : occupants) {
				if(!(occupant instanceof Teleport))
				{
					if(occupant instanceof Bridge)
					{
						unit.setDirection(occupant.getDirection());
					}
					collide(unit, occupant);
				}
			}
		}
	}
	
	/**
	 * Actual case of A player or a ghost falling into a hole.
     *
     * @param player The player involved in the collision.
     * @param hole The hole involved in the collision.
	 */
	public void characterVersusBridge(Unit unit, Bridge bridge) {
		Direction uDir = unit.getDirection();
		if(bridge.parralelTo(uDir)){
			unit.setBridgePosition(BridgePosition.ON_A_BRIDGE);
		}
		else{
			unit.setBridgePosition(BridgePosition.UNDER_A_BRIDGE);
		}
	}
}
