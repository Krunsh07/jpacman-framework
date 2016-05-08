package nl.tudelft.jpacman.level;


import nl.tudelft.jpacman.board.Board;
import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.npc.ghost.Ghost;

import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimerTasks {

    public CharacterMoveTask createCharacterMoveTask(ScheduledExecutorService service, MovableCharacter mc){
        return new CharacterMoveTask(service, mc);
    }

    public TimerHunterTask createStopHunterModeTask(){
        return new TimerHunterTask();
    }

    public TimerRespawnTask createRespawnTask(Ghost ghost){
        return new TimerRespawnTask(ghost);
    }

    public TimerWarningTask createWarningTask(){
        return new TimerWarningTask();
    }

    public TimerAddGhostTask createAddGhostTask(){
        return new TimerAddGhostTask();
    }

    public TimerAddFruitTask createAddFruitTask(){
        return new TimerAddFruitTask();
    }

    public TimerSpeedUpTask createSpeedUpTask(){
        return new TimerSpeedUpTask();
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
            Level level = Level.getLevel();
            long interval;
            if (nextMove != null) {
                level.move(character, nextMove);
            }
            interval = character.getInterval();
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
            Level level = Level.getLevel();
            level.stopHunterMode();
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
            Level level = Level.getLevel();
            Board b = level.getBoard();
            ghost.setExplode(false);
            ghost.occupy(b.getMiddleOfTheMap());
            ghost.stopFearedMode();
            level.stopCharacters();
            level.startCharacters();
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
        public void run() {
            Level level = Level.getLevel();
            level.warningMode();
        }
    }

    /**
     * A task that handle the end of Hunter Mode.
     *
     * @author Yarol Timur
     */
    private final class TimerAddGhostTask extends TimerTask {

        @Override
        public void run() {
            Level level = Level.getLevel();
            level.addGhostTask();
        }
    }

    private final class TimerAddFruitTask extends TimerTask {

        @Override
        public void run() {
            Level level = Level.getLevel();
            level.addFruitTask();
        }
    }

    private final class TimerSpeedUpTask extends TimerTask {

        @Override
        public void run() {
            Level level = Level.getLevel();
            level.speedUpTask();
        }
    }
}
