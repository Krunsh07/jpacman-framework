package nl.tudelft.jpacman.level;

import static org.junit.Assert.*;

import java.util.List;

import nl.tudelft.jpacman.Launcher;

import com.google.common.collect.Lists;

import nl.tudelft.jpacman.board.*;
import nl.tudelft.jpacman.npc.Bullet;
import nl.tudelft.jpacman.npc.ghost.Ghost;
import nl.tudelft.jpacman.npc.ghost.GhostFactory;
import nl.tudelft.jpacman.sprite.PacManSprites;
import nl.tudelft.jpacman.sprite.Sprite;
import org.junit.Before;
import org.junit.Test;


public class BulletTest {

	private PacManSprites pms;
	private GhostFactory gf;
	private Player p;
	private MapParser parser;

	@Before
	public void setUp() {
		Launcher launcher;
		launcher = new Launcher();
		launcher.setBoardToUse("/boardFruit.txt");
		pms = new PacManSprites();
		p = new Player(pms.getPacmanSprites(),pms.getPacManDeathAnimation());
		gf = new GhostFactory(pms);
		parser = new MapParser(new LevelFactory(pms, gf), new BoardFactory(pms));
	}

	/**
	 * Test that a bullet is initialized correctly
	 */
	@Test
	public void initTest(){
		Sprite bulletSprite = pms.getBulletSprite();
	    Bullet bullet = new Bullet(pms.getBulletSprite(), p);
	    assertTrue(bullet.isAlive());
	    assertEquals(bullet.getSprite(), bulletSprite);
	    assertEquals(bullet.getDirection(), p.getDirection());
	}
	
	/**
	 * Test that a bullet can't change directions
	 */
	@Test
	public void CantChangeDirection(){
	    Bullet bullet = new Bullet(pms.getBulletSprite(), p);
	    assertEquals(bullet.getDirection(), p.getDirection());
	    bullet.setDirection(Direction.EAST);
	    assertEquals(bullet.getDirection(), p.getDirection());
	}
	
	/**
	 * Test that a bullet can't make a move when it's dead.
	 */
	@Test
	public void doesNotMovewhenDeadTest(){
		Board b = parser.parseMap(Lists.newArrayList("#####", "#   #","#   #" ,"#####")).getBoard();
		Square notNextToWall = b.squareAt(2, 2);
		p.occupy(notNextToWall);
		p.setDirection(Direction.EAST);
		p.occupy(notNextToWall);
	    Bullet bullet = new Bullet(pms.getBulletSprite(), p);
	    bullet.occupy(notNextToWall);
	    assertNotNull(bullet.nextMove());
	    bullet.setAlive(false);
	    assertNull(bullet.nextMove());
	}
	
	/**
	 * Test that a bullet can't make a move and die when blocked by a wall.
	 */
	@Test
	public void doesNotMoveAndDiewhenBlockedByWallsTest(){
		Board b = parser.parseMap(Lists.newArrayList("#####", "#   #", "#   #", "#   #", "#####")).getBoard();
		Square notNextToWall = b.squareAt(2, 2);
        Square nextToWall = b.squareAt(2, 1);
        p.occupy(notNextToWall);
		p.setDirection(Direction.NORTH);
	    Bullet bullet = new Bullet(pms.getBulletSprite(), p);
	    bullet.occupy(nextToWall);
	    assertNull(bullet.nextMove());
	    assertFalse(bullet.isAlive());
	}
	
	/**
	 * Test that a bullet can't make a move and die when blocked by a bridge.
	 */
	@Test
	public void doesNotMoveAndDiewhenBlockedByBridgesTest(){
		Board b = parser.parseMap(Lists.newArrayList("####", "#  #", "#B #", "####", "----", "----", "V N ")).getBoard();
		Square square = b.squareAt(1, 2);
		p.occupy(square);
		p.setDirection(Direction.NORTH);
	    Bullet bullet = new Bullet(pms.getBulletSprite(), p);
	    bullet.occupy(square);
	    assertNull(bullet.nextMove());
	    assertFalse(bullet.isAlive());
	}
	
	/**
	 * Test the setAlive function correctly change the life status of a bullet. 
	 */
	@Test
	public void setAliveTest(){
		Player p = new Player(pms.getPacmanSprites(),pms.getPacManDeathAnimation());
	    Bullet bullet = new Bullet(pms.getBulletSprite(), p);
	    assertTrue(bullet.isAlive());
	    bullet.setAlive(false);
	    assertFalse(bullet.isAlive());
	}
	
	/**
	 * Test that a bullet can explode ghosts.
	 */
	@Test
	public void BulletKillsGhostTest(){
		Board b = parser.parseMap(Lists.newArrayList("###", "# #", "###")).getBoard();
		Square square = b.squareAt(1, 1);
		Ghost g = gf.createBlinky();
	    Bullet bullet = new Bullet(pms.getBulletSprite(), p);
		CollisionMap cm = new PlayerCollisions();
		bullet.occupy(square);
		p.occupy(square);
		g.occupy(square);
		List<Unit> occupants = square.getOccupants();
		for(Unit occupant : occupants) {
			cm.collide(bullet, occupant);
		}
	    assertTrue(g.hasExploded());
	    assertTrue(p.isAlive());
	    assertFalse(bullet.isAlive());
	}
	
}
