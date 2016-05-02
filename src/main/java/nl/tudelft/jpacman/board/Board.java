package nl.tudelft.jpacman.board;

import nl.tudelft.jpacman.Launcher;
import nl.tudelft.jpacman.level.Level;

import java.util.Random;

/**
 * A top-down view of a matrix of {@link Square}s.
 *
 * @author Jeroen Roosen
 */
public class Board {

	/**
	 * The grid of squares with board[x][y] being the square at column x, row y.
	 */
	private Square[][] board;

	private int widthOfOneMap;
	private int heightOfOneMap;

	/**
	 * Creates a new board.
	 *
	 * @param grid The grid of squares with grid[x][y] being the square at column
	 *             x, row y.
	 */
	Board(Square[][] grid) {
		assert grid != null;
		this.board = grid;
		this.setWidthOfOneMap(grid.length);
		this.setHeightOfOneMap(grid[0].length);
		assert invariant() : "Initial grid cannot contain null squares";
		this.setPositions(grid);
	}

	/**
	 * Whatever happens, the squares on the board can't be null.
	 *
	 * @return false if any square on the board is null.
	 */
	public boolean invariant() {
		for (int x = 0; x < board.length; x++) {
			for (int y = 0; y < board[x].length; y++) {
				if (board[x][y] == null) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Returns the width of this board, i.e. the amount of columns.
	 *
	 * @return The width of this board.
	 */
	public int getWidth() {
		return board.length;
	}

	/**
	 * Returns the height of this board, i.e. the amount of rows.
	 *
	 * @return The height of this board.
	 */
	public int getHeight() {
		return board[0].length;
	}

	/**
	 * Returns the square at the given <code>x,y</code> position.
	 *
	 * @param x The <code>x</code> position (column) of the requested square.
	 * @param y The <code>y</code> position (row) of the requested square.
	 * @return The square at the given <code>x,y</code> position (never null).
	 */
	public Square squareAt(int x, int y) {
		assert withinBorders(x, y);
		Square result = board[x][y];
		assert result != null : "Follows from invariant.";
		return result;
	}

	/**
	 * Determines whether the given <code>x,y</code> position is on this board.
	 *
	 * @param x The <code>x</code> position (row) to test.
	 * @param y The <code>y</code> position (column) to test.
	 * @return <code>true</code> iff the position is on this board.
	 */
	public boolean withinBorders(int x, int y) {
		return x >= 0 && x < getWidth() && y >= 0 && y < getHeight();
	}

	/**
	 * Option de création du nouveau Level
	 * @return Le nouveau level
	 */
	private Level setOptions()
	{
		Launcher launcher = Launcher.getLauncher();
		Random random = new Random();
		int nombre = random.nextInt(3);
		launcher.setBoardToUse("/boardExtendedAdd" + (nombre+1) + ".txt");
		Level lev = launcher.makeLevel();
		Level game = Level.getLevel();
		return lev;
	}

	/**
	 * Agrandi le board du jeu
	 * @param direction La direction dans laquelle le board doit s'agrandir.
	 */
	public void extend(Direction direction)
	{
		switch(direction) {
			case EAST:
				this.setLink(this.setSquare(this.boardCopy(
						new Square[this.getWidth() + Math.abs(direction.getDeltaX() * this.getWidth())]
								[this.getHeight() + Math.abs(direction.getDeltaY() * this.getWidth())], 0, 0),
						this.getWidth(), 0, this.getWidth(), this.getHeight()), this.getWidth() - 1,
						0, this.getWidth(), this.getHeight());
				break;
			case NORTH:
				this.setLink(this.setSquare(this.boardCopy(
						new Square[this.getWidth() + Math.abs(direction.getDeltaX() * this.getHeight())]
								[this.getHeight() + Math.abs(direction.getDeltaY() * this.getHeight())], 0,
						this.getHeight()), 0, 0, this.getWidth(), this.getHeight()), 0, 0, this.getWidth(),
						this.getHeight() + 1);
				break;
			case SOUTH:
				this.setLink(this.setSquare(this.boardCopy(
						new Square[this.getWidth() + Math.abs(direction.getDeltaX() * this.getHeight())]
								[this.getHeight() + Math.abs(direction.getDeltaY() * this.getHeight())], 0, 0),
						0, this.getHeight(), this.getWidth(), this.getHeight()), 0, this.getHeight() - 1,
						this.getWidth(), this.getHeight());
				break;
			case WEST:
				this.setLink(this.setSquare(this.boardCopy(
						new Square[this.getWidth() + Math.abs(direction.getDeltaX() * this.getWidth())]
								[this.getHeight() + Math.abs(direction.getDeltaY() * this.getWidth())], this.getWidth(),
						0), 0, 0, this.getWidth(), this.getHeight()), 0, 0, this.getWidth() + 1, this.getHeight());
				break;
			default:
				break;
		}
	}

	/**
	 * Permet de copier l'ancien board dans le nouveau plus grand.
	 * @param newBoard Le nouveau board
	 * @param startX L'abscisse de depart
	 * @param startY L'ordonnee de depart
	 * @return Le nouveau board
	 */
	private Square[][] boardCopy(Square[][] newBoard, int startX, int startY)
	{
		for (int i = 0; i < this.getWidth(); i++)
		{
			System.arraycopy(this.board[i], 0, newBoard[i + startX], startY, this.getHeight());
		}
		return newBoard;
	}

	/**
	 * Mets a jours les positions des éléments du board afficher
	 * @param grid La grille de square
	 */
	public void setPositions(Square[][] grid)
	{
		for(int i = 0; i < grid.length; i++)
		{
			for(int j = 0; j < grid[0].length; j++)
			{
				grid[i][j].setCoord(i, j);
			}
		}
	}

	/**
	 * Permet de définir quels square et ou ils doivent être placer dans le board
	 * @param grid La grille de square
	 * @param startX Abscisse de début
	 * @param startY Ordonnee de debut
	 * @param lengthX Longueur sur x
	 * @param lengthY Longueur sur y
	 * @return La nouvelle grille de square
	 */
	private Square[][] setSquare(Square[][] grid, int startX, int startY, int lengthX, int lengthY)
	{
		for(int i = 0; i < lengthX / this.getWidthOfOneMap(); i++)
		{
			for(int j = 0; j < lengthY / this.getHeightOfOneMap(); j++)
			{
				this.putSquare(grid, startX, startY, i, j);
			}
		}
		return grid;
	}

	/**
	 * Permet de placer les squares du nouveau level dans le board à afficher.
	 * @param grid La grille de square
	 * @param startX Abscisse de debut
	 * @param startY Ordonnee de debut
	 * @param posX Abscisse du square dans grid a remplacer
	 * @param posY Ordonnee du square dans grid a remplacer
	 */
	private void putSquare(Square[][] grid, int startX, int startY, int posX, int posY)
	{
		Level l = this.setOptions();
		Square[][] newGrid = l.getBoard().getBoard();
		for(int i = 0; i < newGrid.length; i++)
		{
			System.arraycopy(newGrid[i], 0, grid[startX + (posX * this.getWidthOfOneMap()) + i],
					startY + (posY * this.getHeightOfOneMap()), newGrid[i].length);
		}
	}

	/**
	 * Mets les liens entre les nouveaux square du board
	 * @param grid La grille de square
	 * @param startX L'abscisse de debut
	 * @param startY L'ordonnee de début
	 * @param lenghtX La longueur sur x
	 * @param lenghtY La longueur sur y
	 */
	private void setLink(Square[][] grid, int startX, int startY, int lenghtX, int lenghtY)
	{
		Square sq1, sq2;
		int x, y;
		for (int i = startX; i < startX + lenghtX; i++)
		{
			for (int j = startY; j < startY + lenghtY; j++)
			{
				sq1 = grid[i][j];
				for (Direction direction : Direction.values())
				{
					x = (grid.length + i + direction.getDeltaX()) % grid.length;
					y = (grid[0].length + j + direction.getDeltaY()) % grid[0].length;
					sq2 = grid[x][y];
					sq1.link(sq2, direction);
				}
			}
		}
		this.setPositions(grid);
		this.setBoard(grid);
	}

	/**
	 * Determine the square on the middle of the Board.
	 *
	 * @return The square on the middle of the Board (never null).
	 */
	public Square getMiddleOfTheMap() {
		Square result = null;
		if (((getHeight() / 2) % 2 != 0) && ((getWidth() / 2) % 2 != 0)) {
			result = board[(getWidth() / 2)][(getHeight() / 2)];
		}
		if (((getHeight() / 2) % 2 == 0) && ((getWidth() / 2) % 2 != 0)) {
			result = board[(getWidth() / 2)][(getHeight() / 2) - 1];
		}
		if (((getHeight() / 2) % 2 != 0) && ((getWidth() / 2) % 2 == 0)) {
			result = board[(getWidth() / 2) - 1][(getHeight() / 2)];
		}
		if (((getHeight() / 2) % 2 == 0) && ((getWidth() / 2) % 2 == 0)) {
			result = board[(getWidth() / 2) - 1][(getHeight() / 2) - 1];
		}
		assert result != null : "Follows from invariant.";
		return result;
	}

	/**
	 * Retourne le board actuel
	 * @return Le board du jeu
	 */
	public Square[][] getBoard() {
		return board;
	}

	/**
	 * Permet de remplacer le board actuel
	 * @param board le nouveau board.
	 */
	public void setBoard(Square[][] board) {
		this.board = board;
	}

	public int getWidthOfOneMap() {
		return widthOfOneMap;
	}

	public void setWidthOfOneMap(int widthOfOneMap) {
		this.widthOfOneMap = widthOfOneMap;
	}

	public int getHeightOfOneMap() {
		return heightOfOneMap;
	}

	public void setHeightOfOneMap(int heightOfOneMap) {
		this.heightOfOneMap = heightOfOneMap;
	}
}