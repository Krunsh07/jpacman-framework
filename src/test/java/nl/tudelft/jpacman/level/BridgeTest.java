package nl.tudelft.jpacman.level;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import nl.tudelft.jpacman.Launcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.Lists;

import nl.tudelft.jpacman.PacmanConfigurationException;
import nl.tudelft.jpacman.board.Board;
import nl.tudelft.jpacman.board.BoardFactory;
import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.npc.ghost.Ghost;
import nl.tudelft.jpacman.npc.ghost.GhostFactory;
import nl.tudelft.jpacman.sprite.PacManSprites;
import nl.tudelft.jpacman.sprite.Sprite;

public class BridgeTest {

	private Launcher launcher;
	
	@Rule
    public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() {
		launcher = new Launcher();
		launcher.setBoardToUse("/boardFruit.txt");
	}
	
	/**
	 * Verifies that a Bridge object is initialized correctly. 
	 * 
	 * @throws IOException
	 */
	@Test
	public void initializationTest() throws IOException {
		PacManSprites pms = new PacManSprites();
		Map<Direction, Sprite> sprites = pms.getBridgeSprites();
		Bridge bridge = new Bridge(sprites);
		bridge.setDirection(Direction.EAST);
		assertEquals(bridge.getSprite(), sprites.get(bridge.getDirection()));
		bridge.setDirection(Direction.NORTH);
		assertEquals(bridge.getSprite(), sprites.get(bridge.getDirection()));
		bridge.setDirection(Direction.SOUTH);
		assertEquals(bridge.getSprite(), sprites.get(bridge.getDirection()));
		bridge.setDirection(Direction.WEST);
		assertEquals(bridge.getSprite(), sprites.get(bridge.getDirection()));
	}
	
	/**
	 * Verifies that an 'B' character in a string representing a board produce
	 * a Square object only occupied by a Bridge object which has the
	 * orientation and the content specified
	 * 
	 * @throws IOException
	 */
	@Test
	public void boardBridgeTest() throws IOException {
		PacManSprites sprites = new PacManSprites();
		MapParser parser = new MapParser(new LevelFactory(sprites,
				new GhostFactory(sprites)), new BoardFactory(sprites));
		Board b = parser.parseMap(Lists.newArrayList(Lists.newArrayList(
				"####", "#BB#", "####","----", "----", "H N ", "V P "))).getBoard();
		Square s1 = b.squareAt(1, 1), s2 = b.squareAt(2, 1);
		List<Unit> occupants1 =  s1.getOccupants();
		List<Unit> occupants2 =  s2.getOccupants();
		assertEquals(occupants1.size(), 1);
		assertEquals(occupants2.size(), 2);
		assertThat(occupants1.get(0),  instanceOf(Bridge.class));
		assertThat(occupants2.get(0),  instanceOf(Bridge.class));
		assertThat(occupants2.get(1),  instanceOf(Pellet.class));
		assertEquals(occupants1.get(0).getDirection(), Direction.EAST);
		assertEquals(occupants2.get(0).getDirection(), Direction.NORTH);
	}
	
	/**
	 * Verifies that a bridge reference parsing fail if the first character is
	 * anything else than H or V.
	 * 
	 * @throws IOException
	 */
	@Test
	public void fomatFailTest1() {
		PacManSprites sprites = new PacManSprites();
		MapParser parser = new MapParser(new LevelFactory(sprites,
				new GhostFactory(sprites)), new BoardFactory(sprites));
        thrown.expect(PacmanConfigurationException.class);
		parser.parseMap(Lists.newArrayList(Lists.newArrayList("###", "#B#",
				"###","---", "---",  "B N"))).getBoard();
	}
	
	/**
	 * Verifies that a the board parsing fail in case when there's no teleport
	 * and no empty teleport reference section has been specified before the
	 * bridge reference section.
	 * 
	 * @throws IOException
	 */
	@Test
	public void fomatFailTest2() {
		PacManSprites sprites = new PacManSprites();
		MapParser parser = new MapParser(new LevelFactory(sprites,
				new GhostFactory(sprites)), new BoardFactory(sprites));
        thrown.expect(PacmanConfigurationException.class);
		parser.parseMap(Lists.newArrayList(Lists.newArrayList("###", "#B#",
				"###","---",  "B N"))).getBoard();
	}
	
	/**
	 * Verifies that all bridge reference cannot have no references in the
	 * case where the reference list is incomplete.
	 * 
	 * @throws IOException
	 */
	@Test
	public void fomatFailTest3() {
		PacManSprites sprites = new PacManSprites();
		MapParser parser = new MapParser(new LevelFactory(sprites,
				new GhostFactory(sprites)), new BoardFactory(sprites));
        thrown.expect(PacmanConfigurationException.class);
		parser.parseMap(Lists.newArrayList(Lists.newArrayList("#####", "#BPB#",
				"#####","-----", "-----", "H P  "))).getBoard();
	}
	
	/**
	 * Verifies that all bridge reference cannot have no references in the
	 * case where the list is empty.
	 * 
	 * @throws IOException
	 */
	@Test
	public void fomatFailTest4() {
		PacManSprites sprites = new PacManSprites();
		MapParser parser = new MapParser(new LevelFactory(sprites,
				new GhostFactory(sprites)), new BoardFactory(sprites));
        thrown.expect(PacmanConfigurationException.class);
		parser.parseMap(Lists.newArrayList(Lists.newArrayList("#####", "#BPB#",
				"#####","-----", "-----"))).getBoard();
	}
	
	/**
	 * Verifies that a bridge reference parsing fail if the second character is
	 * anything else than N, P or F.
	 * 
	 * @throws IOException
	 */
	@Test
	public void fomatFailTest5() {
		PacManSprites sprites = new PacManSprites();
		MapParser parser = new MapParser(new LevelFactory(sprites,
				new GhostFactory(sprites)), new BoardFactory(sprites));
        thrown.expect(PacmanConfigurationException.class);
		parser.parseMap(Lists.newArrayList(Lists.newArrayList("###", "#B#",
				"###","---", "---",  "H -"))).getBoard();
	}
	
	/**
	 * Verifies that a bridge reference can't contain more than two elements
	 * 
	 * @throws IOException
	 */
	@Test
	public void fomatFailTest6() {
		PacManSprites sprites = new PacManSprites();
		MapParser parser = new MapParser(new LevelFactory(sprites,
				new GhostFactory(sprites)), new BoardFactory(sprites));
        thrown.expect(PacmanConfigurationException.class);
		parser.parseMap(Lists.newArrayList(Lists.newArrayList("#####", "#B  #",
				"#####","-----", "-----",  "H P N"))).getBoard();
	}
	
	/**
	 * Verifies that a bridge reference can't contain less than two elements
	 * 
	 * @throws IOException
	 */
	@Test
	public void fomatFailTest7() {
		PacManSprites sprites = new PacManSprites();
		MapParser parser = new MapParser(new LevelFactory(sprites,
				new GhostFactory(sprites)), new BoardFactory(sprites));
        thrown.expect(PacmanConfigurationException.class);
		parser.parseMap(Lists.newArrayList(Lists.newArrayList("###", "#B#",
				"###","---", "---",  "H  "))).getBoard();
	}
	
	/**
	 * Verifies that a bridge reference can't contain less than two elements
	 * 
	 * @throws IOException
	 */
	@Test
	public void fomatFailTest8() {
		PacManSprites sprites = new PacManSprites();
		MapParser parser = new MapParser(new LevelFactory(sprites,
				new GhostFactory(sprites)), new BoardFactory(sprites));
        thrown.expect(PacmanConfigurationException.class);
		parser.parseMap(Lists.newArrayList(Lists.newArrayList("#######", "#B    #",
				"#######","-------", "-------",  "HM NN "))).getBoard();
	}
	
	/**
	 * Verifies that a player is in the correct bridge position state when he
	 * is colliding in each direction with an horizontal bridge
	 * 
	 * @throws IOException
	 */
	@Test
	public void playerHorizontalBridgeTest() {
		PacManSprites pms = new PacManSprites();
		MapParser parser = new MapParser(new LevelFactory(pms,
				new GhostFactory(pms)), new BoardFactory(pms));
		Board b = parser.parseMap(Lists.newArrayList("######", "#    #","# B  #",
                "#    #", "######", "------", "------", "H N   ")).getBoard();
		Square bridgeSquare = b.squareAt(2, 2);
		Player p = new Player(pms.getPacmanSprites(),pms.getPacManDeathAnimation());
		CollisionMap cm = new PlayerCollisions();
		Unit bridge = bridgeSquare.getOccupants().get(0);
		Direction[] dirs = {Direction.WEST, Direction.EAST, Direction.NORTH,
				Direction.SOUTH};
		for(int i = 0; i < dirs.length; i++) {
            p.occupy(bridgeSquare);
			p.setDirection(dirs[i]);
			assertFalse(p.isOnBridge());
			cm.collide(p, bridge);
			if(i <= 1)
				assertTrue(p.isOnBridge());
			else
				assertFalse(p.isOnBridge());
			p.leaveSquare();
			p.setOnBridge(false);
		}
	}
	
	/**
	 * Verifies that a player is in the correct bridge position state when he
	 * is colliding in each direction with an vertical bridge
	 * 
	 * @throws IOException
	 */
	@Test
	public void playerVerticalBridgeTest() {
		PacManSprites pms = new PacManSprites();
		MapParser parser = new MapParser(new LevelFactory(pms,
				new GhostFactory(pms)), new BoardFactory(pms));
        Board b = parser.parseMap(Lists.newArrayList("######", "#    #","# B  #",
                "#    #", "######", "------", "------", "H N   ")).getBoard();
        Square bridgeSquare = b.squareAt(2, 2);
		Player p = new Player(pms.getPacmanSprites(),pms.getPacManDeathAnimation());
		CollisionMap cm = new PlayerCollisions();
		Unit bridge = bridgeSquare.getOccupants().get(0);
		Direction[] dirs = {Direction.WEST, Direction.EAST, Direction.NORTH,
				Direction.SOUTH};
		for(int i = 0; i < dirs.length; i++) {
            p.occupy(bridgeSquare);
			p.setDirection(dirs[i]);
			assertFalse(p.isOnBridge());
			cm.collide(p, bridge);
			if(i > 1)
                assertFalse(p.isOnBridge());
            else
				assertTrue(p.isOnBridge());
			p.leaveSquare();
			p.setOnBridge(false);
		}
	}
	
	/**
	 * Verifies that a player is killed by a ghost only when they are on the
	 * same bridge position state
	 * 
	 * @throws IOException
	 */
	@Test
	public void collisionGhostBridgeTest() {
		PacManSprites pms = new PacManSprites();
		GhostFactory gf = new GhostFactory(pms);
		MapParser parser = new MapParser(new LevelFactory(pms, gf),
				new BoardFactory(pms));
		Board b = parser.parseMap(Lists.newArrayList("######","#    #","# B  #",
				"######", "------", "------", "V N   ")).getBoard();
		Square bridgeSquare = b.squareAt(2, 2);
		Unit p = (Unit) new Player(pms.getPacmanSprites(),pms.getPacManDeathAnimation());
		Ghost g = gf.createBlinky();
		CollisionMap cm = new PlayerCollisions();
		Unit bridge = bridgeSquare.getOccupants().get(0);
		p.occupy(bridgeSquare);
		p.setDirection(Direction.EAST);
		cm.collide(p, bridge);
		assertFalse(p.isOnBridge());
		g.occupy(bridgeSquare);
		g.setDirection(Direction.SOUTH);
		cm.collide(g, bridge);
		assertTrue(g.isOnBridge());
		cm.collide(p, g);
		assertTrue(((Player) p).isAlive());
		p.leaveSquare();
		p.occupy(bridgeSquare);
		p.setDirection(Direction.NORTH);
		System.out.println("collision avec pont ici."+p.getDirection()+" "+bridge.getDirection());
		cm.collide(p, bridge);
		cm.collide(p, g);
		assertFalse(((Player) p).isAlive());
	}
	
	/**
	 * Verifies that a player eat a pellet under a bridge when he's under this
	 * bridge
	 * 
	 * @throws IOException
	 */
	@Test
	public void collisionPelletBridgeTest() {
		PacManSprites pms = new PacManSprites();
		MapParser parser = new MapParser(new LevelFactory(pms,
				new GhostFactory(pms)), new BoardFactory(pms));
		Board b = parser.parseMap(Lists.newArrayList("######", "#    #","# B  #",
                "#    #", "######", "------", "------", "H P   ")).getBoard();
		Square BridgeSquare = b.squareAt(2, 2);
		Player p = new Player(pms.getPacmanSprites(),pms.getPacManDeathAnimation());
		CollisionMap cm = new PlayerCollisions();
        p.occupy(BridgeSquare);
		p.setDirection(Direction.EAST);
		List<Unit> occupants = BridgeSquare.getOccupants();
		for(Unit occupant : occupants) {
			cm.collide(p, occupant);
		}
		assertEquals(occupants.size(), 3);
		assertTrue(occupants.get(0) instanceof Bridge);
		assertTrue(occupants.get(1) instanceof Pellet);
		assertTrue(occupants.get(2) instanceof Player);
		p.setOnBridge(false);
		p.setDirection(Direction.NORTH);
		for(Unit occupant : occupants) {
			cm.collide(p, occupant);
		}
		occupants = BridgeSquare.getOccupants();
		assertEquals(occupants.size(), 2);
		assertTrue(occupants.get(0) instanceof Bridge);
		assertTrue(occupants.get(1) instanceof Player);
	}
	
	//tests d'interaction avec un fruit en dessous d'un pont.
}
