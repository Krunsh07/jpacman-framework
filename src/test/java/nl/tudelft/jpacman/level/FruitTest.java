package nl.tudelft.jpacman.level;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.tudelft.jpacman.Launcher;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import nl.tudelft.jpacman.board.Board;
import nl.tudelft.jpacman.board.BoardFactory;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.fruit.Fruit;
import nl.tudelft.jpacman.fruit.FruitFactory;
import nl.tudelft.jpacman.npc.NPC;
import nl.tudelft.jpacman.npc.ghost.Ghost;
import nl.tudelft.jpacman.npc.ghost.GhostFactory;
import nl.tudelft.jpacman.sprite.PacManSprites;

public class FruitTest {

	/**
	 * The duration of the power of a fruit that has a positive effect for the player.
	 */
	private static final int GOOD_EFFECT_DURATION = 4;

	/**
	 * The duration of the power of a fruit that has a negative effect for the player.
	 */
	private static final int BAD_EFFECT_DURATION = 2;

	private PacManSprites pms;
	private MapParser parser;
	private GhostFactory gf;
	private FruitFactory ff;
	private Player p;
	private CollisionMap cm;


	@Before
	public void setUp() {
		Launcher launcher;
		launcher = new Launcher();
		launcher.setBoardToUse("/boardFruit.txt");
		pms = new PacManSprites();
		parser = new MapParser(new LevelFactory(pms, new GhostFactory(pms)), new BoardFactory(pms));
		p = new Player(pms.getPacmanSprites(),pms.getPacManDeathAnimation());
		ff = new FruitFactory(pms, null);
		gf = new GhostFactory(pms);
		cm = new PlayerCollisions();
	}

	/**
	 * Test that a bell pepper accelerate pacman during 4 seconds
	 * and disapear fromn the square
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void bellPepperTest() throws IOException, InterruptedException {
		Board b = parser.parseMap(Lists.newArrayList(" ")).getBoard();
		Square fruitSquare = b.squareAt(0, 0);
		Fruit f = ff.getBellPepper();
		f.occupy(fruitSquare);
		p.occupy(fruitSquare);
		Unit fruit = fruitSquare.getOccupants().get(0);
		assertFalse(p.getAcceleration());
		cm.collide(p, fruit);
		assertTrue(p.getAcceleration());
		assertEquals(fruitSquare.getOccupants().size(), 1);
        assertTrue(fruitSquare.getOccupants().get(0) instanceof Player);
		// Sleeping in tests is generally a bad idea.
        // Here we do it just to let the fruit effect disappear.
        Thread.sleep(GOOD_EFFECT_DURATION * 1000 + 1000);
        assertFalse(p.getAcceleration());
	}
	
	/**
	 * Test that that the pomgranate kills one ghost in the
	 * radius of 4 squares away from the fruit and that one ghost
	 * out of that radius remain safe. the must fruit disappear from the square
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void PomergranateTest() throws IOException, InterruptedException {
		Board b = parser.parseMap(Lists.newArrayList("###########",
				"#         #", "#         #", "#         #", "#         #",
				"#         #", "#         #", "###########")).getBoard();
		Square fruitSquare = b.squareAt(5, 6);
		Square explodedSquare = b.squareAt(5, 3);
		Square safeSquare = b.squareAt(1, 1);
		Ghost explodedGhost = gf.createBlinky();
		Ghost safeGhost = gf.createClyde();
		List<NPC> gl = new ArrayList<NPC>();
		gl.add(explodedGhost);
        Square square = b.squareAt(1, 1);
        List<Square>  sp = new ArrayList<>();
        sp.add(square);
        Level l = new LevelFactory(pms, gf).createLevel(b, gl, sp);
		Fruit f = new FruitFactory(pms, l).getPomgranate();
		f.occupy(fruitSquare);
		explodedGhost.occupy(explodedSquare);
		safeGhost.occupy(safeSquare);
		p.occupy(fruitSquare);
		Unit fruit = fruitSquare.getOccupants().get(0);
		cm.collide(p, fruit);
        assertTrue(explodedGhost.hasExploded());
        assertFalse(safeGhost.hasExploded());
	}
	
	/**
	 * Test that a the fish make pacman motionless during 4 seconds
	 * and the fruit disappear from the square
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void FishTest() throws IOException, InterruptedException {
		Board b = parser.parseMap(Lists.newArrayList(" ")).getBoard();
		Square fruitSquare = b.squareAt(0, 0);
		Fruit f = ff.getFish();
		f.occupy(fruitSquare);
		p.occupy(fruitSquare);
		Unit fruit = fruitSquare.getOccupants().get(0);
		assertTrue(p.isMovable());
		cm.collide(p, fruit);
		assertFalse(p.isMovable());
		assertEquals(fruitSquare.getOccupants().size(), 1);
        assertTrue(fruitSquare.getOccupants().get(0) instanceof Player);
		// Sleeping in tests is generally a bad idea.
        // Here we do it just to let the fruit effect disappear.
        Thread.sleep(BAD_EFFECT_DURATION * 1000 + 1000);
        assertTrue(p.isMovable());
	}
	
	/**
	 * Test that the kidneybean fruit temporarily enable
	 * pacman's shooting state and disappear after this
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void KidneyBeanTest() throws IOException, InterruptedException {
		Board b = parser.parseMap(Lists.newArrayList(" ")).getBoard();
		Square fruitSquare = b.squareAt(0, 0);
		Fruit f =ff.getKidneyBean();
		f.occupy(fruitSquare);
		p.occupy(fruitSquare);
		Unit fruit = fruitSquare.getOccupants().get(0);
		assertFalse(p.isShooting());
		cm.collide(p, fruit);
		assertEquals(fruitSquare.getOccupants().size(), 1);
        assertTrue(fruitSquare.getOccupants().get(0) instanceof Player);
        assertTrue(p.isShooting());
		// Sleeping in tests is generally a bad idea.
        // Here we do it just to let the fruit effect disappear.
        Thread.sleep(GOOD_EFFECT_DURATION * 1000 + 1000);
        assertFalse(p.isShooting());
	}
	
	/**
	 * test that a potato temporarily change the acceleration status of ghosts.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void potatoTest() throws IOException, InterruptedException {
		Board b = parser.parseMap(Lists.newArrayList("###", "# #", "###")).getBoard();
		List<NPC> gl = new ArrayList<NPC>();
		Ghost g = gf.createBlinky();
		gl.add(g);
        Square square = b.squareAt(1, 1);
        List<Square>  sp = new ArrayList<>();
        sp.add(square);
		Level l = new LevelFactory(pms, gf).createLevel(b, gl, sp);
		Fruit f = new FruitFactory(pms, l).getPotato();
		f.occupy(square);
		p.occupy(square);
		g.occupy(square);
		Unit fruit = square.getOccupants().get(0);
		assertFalse(g.getAcceleration());
		cm.collide(p, fruit);
		assertEquals(square.getOccupants().size(), 2);
        assertTrue(square.getOccupants().get(0) instanceof Player);
        assertTrue(g.getAcceleration());
		// Sleeping in tests is generally a bad idea.
        // Here we do it just to let the fruit effect disappear.
        Thread.sleep(BAD_EFFECT_DURATION * 1000 + 1000);
        assertFalse(g.getAcceleration());
	}
	
	/**
	 * Test that a tomato make pacman temporaily invulnerable 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void tomatoTest() throws IOException, InterruptedException {
		Board b = parser.parseMap(Lists.newArrayList("###", "# #", "###")).getBoard();
		Square square = b.squareAt(1, 1);
		Ghost g = gf.createBlinky();
		List<NPC> gl = new ArrayList<NPC>();
		gl.add(g);
		Fruit f = ff.getTomato();
		f.occupy(square);
		p.occupy(square);
		g.occupy(square);
		Unit fruit = square.getOccupants().get(0);
		assertFalse(p.isInvincible());
		cm.collide(p, fruit);
		cm.collide(p, g);
		assertEquals(square.getOccupants().size(), 2);
        assertTrue(square.getOccupants().get(0) instanceof Player);
        assertTrue(square.getOccupants().get(1) instanceof Ghost);
        assertTrue(p.isInvincible());
		// Sleeping in tests is generally a bad idea.
        // Here we do it just to let the fruit effect disappear.
        Thread.sleep(GOOD_EFFECT_DURATION * 1000 + 1000);
        assertFalse(p.isInvincible());
        assertTrue(p.isAlive());
	}
}
