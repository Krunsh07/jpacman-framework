package nl.tudelft.jpacman.level;

import nl.tudelft.jpacman.npc.Bullet;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

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
     * The level mode change for a while.
     * Pacman become a Hunter and the Ghost are feared.
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