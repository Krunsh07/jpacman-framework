package nl.tudelft.jpacman.level;

import nl.tudelft.jpacman.Launcher;
import org.junit.Before;
import org.junit.rules.ExpectedException;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.instanceOf;

import java.io.IOException;
import java.util.List;

import nl.tudelft.jpacman.PacmanConfigurationException;
import nl.tudelft.jpacman.board.*;
import nl.tudelft.jpacman.npc.ghost.Ghost;
import nl.tudelft.jpacman.npc.ghost.GhostFactory;
import nl.tudelft.jpacman.sprite.PacManSprites;
import nl.tudelft.jpacman.sprite.Sprite;
import nl.tudelft.jpacman.sprite.SpriteStore;

import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.Lists;


public class TeleportTest {

	private PacManSprites pms;
	private MapParser parser;
	private GhostFactory gf;
	private Player p;
	private CollisionMap cm;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() {
		Launcher launcher;
		launcher = new Launcher();
		launcher.setBoardToUse("/boardFruit.txt");
		pms = new PacManSprites();
		parser = new MapParser(new LevelFactory(pms,
				new GhostFactory(pms)), new BoardFactory(pms));
		p = new Player(pms.getPacmanSprites(),pms.getPacManDeathAnimation());
		cm = new PlayerCollisions();
	}
	
	/**
	 * Verifies that a Teleport object is initialized correctly. 
	 * 
	 * @throws IOException
	 */
	@Test
	public void initializationTest() throws IOException {
		SpriteStore store = new SpriteStore();
        Sprite sprite = store.loadSprite("/sprite/64x64white.png");
		Teleport testTeleport = new Teleport(sprite);
		assertEquals(testTeleport.getSprite(), sprite);
		assertNull(testTeleport.getReference());
	}
	
	/**
	 * Verifies that an 'T' character in a string representing a board produce
	 * a Square object only occupied by a Teleport object that point to the
	 * square at the coordinate specified in the teleport section of the board string.
	 * 
	 * @throws IOException
	 */
	@Test
	public void boardteleportTest() throws IOException {
		Board b = parser.parseMap(Lists.newArrayList(Lists.newArrayList("####",
				"#T #", "####","----", "2 1 "))).getBoard();
		Square s1 = b.squareAt(1, 1);
		Square s2 = b.squareAt(2, 1);
		List<Unit> occupants =  s1.getOccupants();
		assertEquals(occupants.size(), 1);
		assertThat(occupants.get(0),  instanceOf(Teleport.class));
		Teleport t = (Teleport) occupants.get(0);
		assertEquals(t.getReference(), s2);
	}
	
	/**
	 * Verifies that a teleport reference can't be an integer negative number.
	 * 
	 * @throws IOException
	 */
	@Test
	public void fomatFailTest1() {
        thrown.expect(PacmanConfigurationException.class);
		parser.parseMap(Lists.newArrayList(Lists.newArrayList("####", "#T #",
				"####","----", "-1 1"))).getBoard();
	}
	
	/**
	 * Verifies that a teleport reference coordinate can't be another string
	 * than an positive integer.
	 * 
	 * @throws IOException
	 */
	@Test
	public void fomatFailTest2() {
        thrown.expect(PacmanConfigurationException.class);
		parser.parseMap(Lists.newArrayList(Lists.newArrayList("####", "#T #",
				"####","----", "1 A "))).getBoard();
	}
	
	/**
	 * Verifies that a teleport reference coordinate can't contain
	 * only one number.
	 * 
	 * @throws IOException
	 */
	@Test
	public void fomatFailTest3() {
        thrown.expect(PacmanConfigurationException.class);
		parser.parseMap(Lists.newArrayList(Lists.newArrayList("####", "#T #",
				"####","----", "  2 "))).getBoard();
	}
	
	/**
	 * Verifies that a teleport reference coordinate can't point out of the board.
	 * 
	 * @throws IOException
	 */
	@Test
	public void fomatFailTest4() {
        thrown.expect(PacmanConfigurationException.class);
		parser.parseMap(Lists.newArrayList(Lists.newArrayList("####", "#TP#",
				"####","----", "3 3 "))).getBoard();
	}
	
	/**
	 * Verifies that all teleport reference cannot have no references in the
	 * case where the reference list is incomplete.
	 * 
	 * @throws IOException
	 */
	@Test
	public void fomatFailTest5() {
		PacManSprites sprites = new PacManSprites();
		MapParser parser = new MapParser(new LevelFactory(sprites,
				new GhostFactory(sprites)), new BoardFactory(sprites));
        thrown.expect(PacmanConfigurationException.class);
		parser.parseMap(Lists.newArrayList(Lists.newArrayList("#####", "#TPT#",
				"#####","-----", "1 1  "))).getBoard();
	}
	
	/**
	 * Verifies that all teleport reference cannot have no references in the
	 * case where the reference list is empty.
	 * 
	 * @throws IOException
	 */
	@Test
	public void fomatFailTest6() {
        thrown.expect(PacmanConfigurationException.class);
		parser.parseMap(Lists.newArrayList(Lists.newArrayList("####", "#TP#",
				"####"))).getBoard();
	}
	
	/**
	 * Verifies that a teleport reference coordinate can't contain more than two element.
	 * 
	 * @throws IOException
	 */
	@Test
	public void fomatFailTest7() {
        thrown.expect(PacmanConfigurationException.class);
		parser.parseMap(Lists.newArrayList(Lists.newArrayList("####", "#T #",
				"####","----", " 2 3 2 "))).getBoard();
	}
	
	/**
	 * Verifies that there can't be more teleport reference coordinate than teleports on the board string.
	 * 
	 * @throws IOException
	 */
	@Test
	public void fomatFailTest8() {
        thrown.expect(PacmanConfigurationException.class);
		parser.parseMap(Lists.newArrayList(Lists.newArrayList("####", "# P#",
				"####","----", "2 3 "))).getBoard();
	}
	
	/**
	 * Verifies that a player really warp to the square pointed by a
	 * teleport
	 * 
	 * @throws IOException
	 */
	@Test
	public void playerWarpTest() {
		Board b = parser.parseMap(Lists.newArrayList("######","# T  #", "######", "------", "4 1   ")).getBoard();
		Square teleportSquare = b.squareAt(2, 1);
		Square destinationSquare = b.squareAt(4, 1);
		Unit teleport = teleportSquare.getOccupants().get(0);
		p.occupy(teleportSquare);
		cm.collide(p, teleport);
		assertEquals(teleportSquare.getOccupants().size(), 1);
        assertTrue(teleportSquare.getOccupants().get(0) instanceof Teleport);
		assertEquals(destinationSquare.getOccupants().size(), 1);
        assertTrue(destinationSquare.getOccupants().get(0) instanceof Player);
	}
	
	/**
	 * Verifies that a player can't warp to the square pointed by a
	 * teleport if that square is inaccessible to that player.
	 * 
	 * @throws IOException
	 */
	@Test
	public void playerImpossibleWarpTest() {
		// Square (4, 1) is a wall, so the player shouldn't be able to be there.
		Board b = parser.parseMap(Lists.newArrayList("######","# T ##", "######", "------", "4 1   ")).getBoard();
		Square teleportSquare = b.squareAt(2, 1);
		Unit teleport = teleportSquare.getOccupants().get(0);
		p.occupy(teleportSquare);
		cm.collide(p, teleport);
		assertEquals(teleportSquare.getOccupants().size(), 2);
        assertTrue(teleportSquare.getOccupants().get(0) instanceof Teleport);
        assertTrue(teleportSquare.getOccupants().get(1) instanceof Player);
	}
	
	/**
	 * Verifies that a player die if it warp to a square where a ghost is
	 * 
	 * @throws IOException
	 */
	@Test
	public void playerWarpAndDieTest() {
		GhostFactory gf = new GhostFactory(pms);
		Board b = parser.parseMap(Lists.newArrayList("######","# T  #", "######", "------", "4 1   ")).getBoard();
		Square teleportSquare = b.squareAt(2, 1);
		Square GhostSquare = b.squareAt(4, 1);
		Ghost g = gf.createBlinky();
		Unit teleport = teleportSquare.getOccupants().get(0);
		p.occupy(teleportSquare);
		g.occupy(GhostSquare);
		cm.collide(p, teleport);
        assertFalse(p.isAlive());
	}
	
	/**
	 * Verifies that a player is trapped if it warp to a square where a hole is
	 * 
	 * @throws IOException
	 */
	@Test
	public void playerWarpAndTrapTest() {
		Board b = parser.parseMap(Lists.newArrayList("######","# T H#", "######", "------", "4 1   ")).getBoard();
		Square teleportSquare = b.squareAt(2, 1);
		Unit teleport = teleportSquare.getOccupants().get(0);
		p.occupy(teleportSquare);
		cm.collide(p, teleport);
        //assertFalse(p.getMobility());
	}
	
	/**
	 * Verifies that a player is really warp to the square pointed by a
	 * teleport
	 * 
	 * @throws IOException
	 */
	@Test
	public void playerNoconsecutiveWarpTest() {
		Board b = parser.parseMap(Lists.newArrayList("######","# T T#",
				"######", "------", "4 1   ", "1 1   ")).getBoard();
		Square teleportSquare = b.squareAt(2, 1);
		Square SecondTeleportSquare = b.squareAt(4, 1);
		Square SecondDestinationSquare = b.squareAt(1, 1);
		Unit teleport = teleportSquare.getOccupants().get(0);
		p.occupy(teleportSquare);
		cm.collide(p, teleport);
		assertEquals(SecondTeleportSquare.getOccupants().size(), 2);
	    assertTrue(SecondTeleportSquare.getOccupants().get(0) instanceof Teleport);
	    assertTrue(SecondTeleportSquare.getOccupants().get(1) instanceof Player);
		assertEquals(SecondDestinationSquare.getOccupants().size(), 0);
	}
	
	/**
	 * Verifies that when a player warp to a square occupied by a bridge he's
	 * always on this bridge
	 * 
	 * @throws IOException
	 */
	@Test
	public void playerWarpBridgeTest() {
		Board b = parser.parseMap(Lists.newArrayList("#######", "#     #" ,"# T B #","#     #",
				"#######", "-------", "4 2    ", "-------", "V N    ")).getBoard();
		Square teleportSquare = b.squareAt(2, 2);
		Unit teleport = teleportSquare.getOccupants().get(0);
		p.occupy(teleportSquare);
		assertFalse(p.isOnBridge());
		cm.collide(p, teleport);
        assertTrue(p.isOnBridge());
	}
}