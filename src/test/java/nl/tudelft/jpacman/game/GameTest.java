package nl.tudelft.jpacman.game;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import nl.tudelft.jpacman.Launcher;
import nl.tudelft.jpacman.board.*;
import nl.tudelft.jpacman.level.*;
import nl.tudelft.jpacman.npc.NPC;

import nl.tudelft.jpacman.npc.ghost.GhostFactory;
import nl.tudelft.jpacman.sprite.PacManSprites;
import nl.tudelft.jpacman.game.Game;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by Yarol Timur on 11/03/2016.
 */
public class GameTest {

    /**
     * The game under test.
     */
    private Game game;

    /**
     * The level under test.
     */
    private Level level;

    /**
     * The super Pellet who change the game's rules.
     */
    private Pellet pellet;

    /**
     * Map parser used to construct boards.
     */
    private MapParser parser;

    /**
     * The level factory to create the super Pellet.
     */
    private LevelFactory levelFactory;

    /**
     * Set up the map parser.
     */
    @Before
    public void setUp() {
        Launcher launcher = new Launcher();
        launcher.setBoardToUse("/board.txt");
        PacManSprites sprites = new PacManSprites();
        parser = new MapParser(new LevelFactory(sprites, new GhostFactory(
                sprites)), new BoardFactory(sprites));
    }

    /**
     * Verifies the game's rules change when a player eat a
     * super pellet.
     */
    @Test
    public void testHunterMode() {
        Board b = parser
                .parseMap(Lists.newArrayList("######", "# ..o#", "######"))
                .getBoard();
        Square s1 = b.squareAt(1, 1);
        Square s2 = b.squareAt(4, 1);
    }


}
