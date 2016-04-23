package nl.tudelft.jpacman.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import nl.tudelft.jpacman.Launcher;
import nl.tudelft.jpacman.board.Board;
import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.game.Game;
import nl.tudelft.jpacman.level.Level;
import nl.tudelft.jpacman.level.Player;

/**
 * Panel displaying a game.
 *
 * @author Jeroen Roosen 
 *
 */
class BoardPanel extends JPanel {

	/**
	 * Default serialisation ID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The background colour of the board.
	 */
	private static final Color BACKGROUND_COLOR = Color.BLACK;

	/**
	 * The size (in pixels) of a square on the board. The initial size of this
	 * panel will scale to fit a board with square of this size.
	 */
	private static final int SQUARE_SIZE = 16;

	/**
	 * The game to display.
	 */
	private final Game game;

	/**
	 * Taille d'un board de base pour recentrer le dessin
	 */
	private int scalex, scaley;


	int X, Y;

	/**
	 * Savoir si c'est le premier traçage ou non
	 */
	private boolean first = true;

	/**
	 * Creates a new board panel that will display the provided game.
	 *
	 * @param game
	 *            The game to display.
	 */
	BoardPanel(Game game) {
		super();
		assert game != null;
		this.game = game;

		Board board = game.getLevel().getBoard();

		int w = board.getWidth() * SQUARE_SIZE;
		int h = board.getHeight() * SQUARE_SIZE;

		Dimension size = new Dimension(w, h);
		setMinimumSize(size);
		setPreferredSize(size);
	}

	@Override
	public void paint(Graphics g) {
		assert g != null;
		Launcher launcher = Launcher.getLauncher();
		if(launcher.getBoardToUse() == "/board.txt") {
			render(game.getLevel().getBoard(), g, getSize());
		}
		else {
			renderInfinite(game.getLevel().getBoard(), g, getSize());
		}
	}

	/**
	 * Renders the board on the given graphics context to the given dimensions.
	 *
	 * @param board
	 *            The board to render.
	 * @param g
	 *            The graphics context to draw on.
	 * @param window
	 *            The dimensions to scale the rendered board to.
	 */
	private void render(Board board, Graphics g, Dimension window) {
		int cellW = window.width / board.getWidth();
		int cellH = window.height / board.getHeight();

		g.setColor(BACKGROUND_COLOR);
		g.fillRect(0, 0, window.width, window.height);

		for (int y = 0; y < board.getHeight(); y++) {
			for (int x = 0; x < board.getWidth(); x++) {
				int cellX = x * cellW;
				int cellY = y * cellH;
				Square square = board.squareAt(x, y);
				render(square, g, cellX, cellY, cellW, cellH);
			}
		}
	}

	/**
	 * Renders the board on the given graphics context to the given dimensions.
	 *
	 * @param board
	 *            The board to render.
	 * @param g
	 *            The graphics context to draw on.
	 * @param window
	 *            The dimensions to scale the rendered board to.
	 */
	private void renderInfinite(Board board, Graphics g, Dimension window) {
		int cellW;
		int cellH;
		if(this.first)
		{
			this.scalex = board.getWidth();
			this.scaley = board.getHeight();
			this.first = false;
		}
		cellW = window.width / this.scalex;
		cellH = window.height / this.scaley;
		Player pl = this.game.getPlayers().get(0);
		Square posPlayer = pl.getSquare();

		g.setColor(BACKGROUND_COLOR);
		g.fillRect(0, 0, window.width, window.height);

		int X = posPlayer.getCoordX();
		int Y = posPlayer.getCoordY();

		//System.out.println("X-Y : (" + X + "," + Y + ").");
		for (int y = Y-15; y < Y+6; y++)
		{
			for (int x = X-11; x < X+12; x++)
			{
				int cellX = ((this.scalex/2 - posPlayer.getCoordX()) + x) * cellW;
				int cellY = ((((int) (this.scaley/1.4)) - posPlayer.getCoordY()) + y) * cellH;
				Square square = board.squareAt(x, y);
				render(square, g, cellX, cellY, cellW, cellH);
			}
		}
		Level lvl = Level.getLevel();
		if(lvl.isInProgress()) {
			if (posPlayer.getCoordX() > board.getWidth() - 13) {
				//System.out.println("extend EAST !! Map size : (" + board.getHeight() + "," + board.getWidth() + ").");
				board.extend(Direction.EAST);
			}
			if (posPlayer.getCoordX() < 13) {
				//System.out.println("extend WEST !! Map size : (" + board.getHeight() + "," + board.getWidth() + ").");
				board.extend(Direction.WEST);
			}
			if (posPlayer.getCoordY() > board.getHeight() - 12) {
				//System.out.println("extend SOUTH !! Map size : (" + board.getHeight() + "," + board.getWidth() + ").");
				board.extend(Direction.SOUTH);
			}
			if (posPlayer.getCoordY() < 18) {
				//System.out.println("extend NORTH !! Map size : (" + board.getHeight() + "," + board.getWidth() + ").");
				board.extend(Direction.NORTH);
			}
		}
	}

	/**
	 * Renders a single square on the given graphics context on the specified
	 * rectangle.
	 *
	 * @param square
	 *            The square to render.
	 * @param g
	 *            The graphics context to draw on.
	 * @param x
	 *            The x position to start drawing.
	 * @param y
	 *            The y position to start drawing.
	 * @param w
	 *            The width of this square (in pixels.)
	 * @param h
	 *            The height of this square (in pixels.)
	 */
	private void render(Square square, Graphics g, int x, int y, int w, int h) {
		square.getSprite().draw(g, x, y, w, h);
		for (Unit unit : square.getOccupants()) {
			unit.getSprite().draw(g, x, y, w, h);
		}
	}
}