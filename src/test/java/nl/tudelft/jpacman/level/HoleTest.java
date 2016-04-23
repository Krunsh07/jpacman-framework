package nl.tudelft.jpacman.level;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.instanceOf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.tudelft.jpacman.board.Board;
import nl.tudelft.jpacman.board.BoardFactory;
import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.level.LevelFactory;
import nl.tudelft.jpacman.level.MapParser;
import nl.tudelft.jpacman.level.Hole;
import nl.tudelft.jpacman.npc.NPC;
import nl.tudelft.jpacman.npc.ghost.Ghost;
import nl.tudelft.jpacman.npc.ghost.GhostFactory;
import nl.tudelft.jpacman.sprite.PacManSprites;
import nl.tudelft.jpacman.sprite.Sprite;
import nl.tudelft.jpacman.sprite.SpriteStore;

import org.junit.Test;

import com.google.common.collect.Lists;

public class HoleTest {
	
	public static final int HOLE_TIME_TEST = 2;
	
	/**
	 * Verifies that a Hole object is initialized correctly. 
	 * 
	 * @throws IOException
	 */
	@Test
	public void initializationTest() throws IOException {
		SpriteStore store = new SpriteStore();
        Sprite sprite = store.loadSprite("/sprite/64x64white.png");
		Hole testHole = new Hole(HOLE_TIME_TEST, sprite);
		assertEquals(testHole.getTrapTime(), HOLE_TIME_TEST);
		assertEquals(testHole.getSprite(), sprite);
	}
	
	/**
	 * Verifies that an 'H' character in a string representing a board produce
	 * a Square object only occupied by a Hole object.
	 * 
	 * @throws IOException
	 */
	@Test
	public void boardHoleTest() throws IOException {
		PacManSprites sprites = new PacManSprites();
		MapParser parser = new MapParser(new LevelFactory(sprites, new GhostFactory(sprites)), new BoardFactory(sprites));
		Board b = parser.parseMap(Lists.newArrayList("H")).getBoard();
		Square s1 = b.squareAt(0, 0);
		List<Unit> occupants =  s1.getOccupants();
		assertEquals(occupants.size(), 1);
		assertThat(occupants.get(0),  instanceOf(Hole.class));
	}
	
	/**
	 * Verifies that the mobility state of a Player and a Ghost object is
	 * temporarily set to false when they collide with a hole and that the hole
	 * disappear after the collision. 
	 * 
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	@Test
	public void temporaryImmobilityTest() throws IOException, InterruptedException {
		PacManSprites pms = new PacManSprites();
		MapParser parser = new MapParser(new LevelFactory(pms, new GhostFactory(pms)), new BoardFactory(pms));
		Board b = parser.parseMap(Lists.newArrayList("HH")).getBoard();
		Square s1 = b.squareAt(0, 0);
		Square s2 = b.squareAt(1, 0);
		Player p = new Player(pms.getPacmanSprites(),pms.getPacManDeathAnimation());
		Ghost g = new GhostFactory(pms).createBlinky();
		CollisionMap cm = new PlayerCollisions();
		Unit hole = s1.getOccupants().get(0);
		p.occupy(s1);
		cm.collide(p, hole);
		assertFalse(p.getMobility());
		assertEquals(s1.getOccupants().size(), 1);
        assertTrue(s1.getOccupants().get(0) instanceof Player);
		// Sleeping in tests is generally a bad idea.
        // Here we do it just to let the hole effect disappear.
        Thread.sleep(HOLE_TIME_TEST * 1000);
        assertTrue(p.getMobility());
        hole = s2.getOccupants().get(0);
		g.occupy(s2);
		cm.collide(g, hole);
		assertFalse(g.getMobility());
		assertEquals(s2.getOccupants().size(), 1);
        assertTrue(s2.getOccupants().get(0) instanceof Ghost);
        Thread.sleep(HOLE_TIME_TEST * 1000);
        assertTrue(g.getMobility());
	}
}