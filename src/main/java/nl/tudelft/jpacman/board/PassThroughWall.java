package nl.tudelft.jpacman.board;

import nl.tudelft.jpacman.sprite.Sprite;

/**
 * This is the only unit that is able to pass through walls and its only
 * purpose is to use the shortest algorithm in the class navigation while
 * removing the restriction of having to walk around walls.
 */
public class PassThroughWall extends Unit {

	@Override
	public Sprite getSprite() {
		return null;
	}
}
