package nl.tudelft.jpacman.level;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import nl.tudelft.jpacman.Launcher;
import nl.tudelft.jpacman.PacmanConfigurationException;
import nl.tudelft.jpacman.board.Board;
import nl.tudelft.jpacman.board.BoardFactory;
import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.npc.NPC;

/**
 * Creates new {@link Level}s from text representations.
 * 
 * @author Jeroen Roosen 
 */
public class MapParser {
	
	/**
	 * The character used to separate different sections of the board file.
	 */
	private final char SEPARATOR = '-';

	/**
	 * The factory that creates the levels.
	 */
	private final LevelFactory levelCreator;

	/**
	 * The factory that creates the squares and board.
	 */
	private final BoardFactory boardCreator;

	/**
	 * Creates a new map parser.
	 * 
	 * @param levelFactory
	 *            The factory providing the NPC objects and the level.
	 * @param boardFactory
	 *            The factory providing the Square objects and the board.
	 */
	public MapParser(LevelFactory levelFactory, BoardFactory boardFactory) {
		this.levelCreator = levelFactory;
		this.boardCreator = boardFactory;
	}

	/**
	 * Parses the text representation of the board into an actual level.
	 *
	 * The text representation is divided into three part :
	 *
	 * the first part describe the level and is obligatory
	 *
	 * <ul>
	 * <li>Supported characters:
	 * <li>' ' (space) an empty square.
	 * <li>'#' (bracket) a wall.
	 * <li>'.' (period) a square with a pellet.
	 * <li>'P' (capital P) a starting square for players.
	 * <li>'G' (capital G) a square with a ghost.
	 * <li>'T' (capital T) a teleport.
	 * <li>'H' (capital H) a hole.
	 * <li>'B' (capital B) a bridge.
	 * <li>'F' (capital F) a square where a fruit can appear.
	 * </ul>
	 *
	 * The second section must be specified when the the map contains teleports
	 * and begin by a line full of '-' (minus) characters, each line of this
	 * section contain the x coordinate and y coordinate separated by only
	 * one space of the places pointed by teleports following the
	 * order left to right and up to down.
	 *
	 * The third section must be specified when the map contains bridges,
	 * even if there are no teleports, this section must follow the second section, so if
	 * there are no teleports the third section have to follow an empty second
	 * section that just consists of the line full of (minus) characters.
	 * like the second sections, this one also begin by a line full of
	 * '-' (minus) characters. Each lines of this sections consists of two
	 * characters separated by only one spaces representing the orientation of
	 * the bridge followed by what's under that bridge following the
	 * order left to right and up to down.
	 *
	 * <ul>
	 * <li>Supported characters:
	 * <li>'H' (capital H) The orientation of this bridge is horizontal
	 * <li>'V' (capital V) The orientation of this bridge is vertical
	 * </ul>
	 *
	 * <ul>
	 * <li>Supported characters:
	 * <li>'P' (capital P) There is a pellet under that bridge.
	 * <li>'F' (capital F) A fruit can appear under that bridge.
	 * <li>'N' (capital N) There is nothing under that bridge.
	 * </ul>
	 *
	 * @param map
	 *            The text representation of the board, with map[x][y]
	 *            representing the square at position x,y.
	 * @return The level as represented by this text.
	 */
	public Level parseMap(char[][] map, List<int[]> teleportrefs, List<char[]> bridgeRefs){
		int width = map.length;
		int height = map[0].length;

		Square[][] grid = new Square[width][height];

		List<NPC> ghosts = new ArrayList<>();
		List<Square> startPositions = new ArrayList<>();
		List<Teleport> teleportList = new ArrayList<>();
		List<Bridge> bridgeList = new ArrayList<>();
		List<Square> fruitPositions = new ArrayList<>();

		makeGrid(map, width, height, grid, ghosts, startPositions, teleportList, bridgeList, fruitPositions);
		Board board = boardCreator.createBoard(grid);
		setTeleports(teleportList, teleportrefs, board);
		setBridges(bridgeList, bridgeRefs, fruitPositions);
		Level l = levelCreator.createLevel(board, ghosts, startPositions, fruitPositions);
		if(fruitPositions.size() > 0){
			l.setupFruits(fruitPositions, ghosts);
		}
		return l;
	}

	/**
	 * Parses the text representation of the board into an actual level.
	 *
	 * The text representation is divided into three part :
	 *
	 * the first part describe the level and is obligatory
	 *
	 * <ul>
	 * <li>Supported characters:
	 * <li>' ' (space) an empty square.
	 * <li>'#' (bracket) a wall.
	 * <li>'.' (period) a square with a pellet.
	 * <li>'P' (capital P) a starting square for players.
	 * <li>'G' (capital G) a square with a ghost.
	 * <li>'T' (capital T) a teleport.
	 * <li>'H' (capital H) a hole.
	 * <li>'B' (capital B) a bridge.
	 * <li>'F' (capital F) a square where a fruit can appear.
	 * </ul>
	 *
	 * The second section must be specified when the the map contains teleports
	 * and begin by a line full of '-' (minus) characters, each line of this
	 * section contain the x coordinate and y coordinate separated by only
	 * one space of the places pointed by teleports following the
	 * order left to right and up to down.
	 *
	 * The third section must be specified when the map contains bridges,
	 * even if there are no teleports, this section must follow the second section, so if
	 * there are no teleports the third section have to follow an empty second
	 * section that just consists of the line full of (minus) characters.
	 * like the second sections, this one also begin by a line full of
	 * '-' (minus) characters. Each lines of this sections consists of two
	 * characters separated by only one spaces representing the orientation of
	 * the bridge followed by what's under that bridge following the
	 * order left to right and up to down.
	 *
	 * <ul>
	 * <li>Supported characters:
	 * <li>'H' (capital H) The orientation of this bridge is horizontal
	 * <li>'V' (capital V) The orientation of this bridge is vertical
	 * </ul>
	 *
	 * <ul>
	 * <li>Supported characters:
	 * <li>'P' (capital P) There is a pellet under that bridge.
	 * <li>'F' (capital F) A fruit can appear under that bridge.
	 * <li>'N' (capital N) There is nothing under that bridge.
	 * </ul>
	 *
	 * @param map
	 *            The text representation of the board, with map[x][y]
	 *            representing the square at position x,y.
	 * @return The level as represented by this text.
	 */
	public Level parseMap(char[][] map){
		int width = map.length;
		int height = map[0].length;

		Square[][] grid = new Square[width][height];

		List<NPC> ghosts = new ArrayList<>();
		List<Square> startPositions = new ArrayList<>();
		List<Square> fruitPositions = new ArrayList<>();

		makeGrid(map, width, height, grid, ghosts, startPositions, null, null, fruitPositions);
		Board board = boardCreator.createBoard(grid);
		Level l = levelCreator.createLevel(board, ghosts, startPositions, fruitPositions);
		/*if(fruitPositions.size() > 0){
			l.setupFruits(fruitPositions, ghosts);
		}*/
		return l;
	}
	
	private void setTeleports(List<Teleport> teleportList, List<int[]> teleportRefs, Board b){
		if(teleportList.size() == teleportRefs.size()){
			int[] t;
			for(int i = 0; i < teleportList.size(); i++){
				t = teleportRefs.get(i);
				if(b.withinBorders(t[0], t[1])){
					teleportList.get(i).setReference(b.squareAt(t[0], t[1]));
				}
				else{
					throw new PacmanConfigurationException(
				        "The teleport refereces must be a place in the board");
				}
			}
		}
		else{
			throw new PacmanConfigurationException("there can't be more references than teleports");
		}
	}
	
	private void setBridges(List<Bridge> bridgeList, List<char[]> bridgeRefs, List<Square> fruitPositions){
		if(bridgeList.size() == bridgeRefs.size()){
			char[] t;
			for(int i = 0; i < bridgeList.size(); i++){
				t = bridgeRefs.get(i);
				if(t[0] == 'H'){
					bridgeList.get(i).setDirection(Direction.EAST);
				}
				else if(t[0] == 'V'){
					bridgeList.get(i).setDirection(Direction.NORTH);
				}
				if(t[1] == 'P'){
					Square pelletSquare = bridgeList.get(i).getSquare();
					Unit p = levelCreator.createPellet();
					p.setOnBridge(false);
					p.occupy(pelletSquare);
				}
				else if(t[1] == 'F'){
					fruitPositions.add(bridgeList.get(i).getSquare());
					break;
				}
			}
		}
		else{
			throw new PacmanConfigurationException("there can't be more references than bridges");
		}
	}

	private void makeGrid(char[][] map, int width, int height, Square[][] grid, List<NPC> ghosts,
	        List<Square> startPositions, List<Teleport> teleportList, List<Bridge> bridgeList, List<Square> fruitPositions) {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				char c = map[x][y];
				addSquare(grid, ghosts, startPositions, x, y, c, teleportList, bridgeList, fruitPositions);
			}
		}
	}

	private void addSquare(Square[][] grid, List<NPC> ghosts,
								   List<Square> startPositions, int x, int y, char c, List<Teleport> teleportList, List<Bridge> bridgeList, List<Square> fruitPositions){
		switch (c)
		{
			case ' ':
				grid[x][y] = boardCreator.createGround();
				break;
			case '#':
				grid[x][y] = boardCreator.createWall();
				break;
			case '.':
				Square pelletSquare = boardCreator.createGround();
				grid[x][y] = pelletSquare;
				levelCreator.createPellet().occupy(pelletSquare);
				break;
			case 'G':
				Square ghostSquare = makeGhostSquare(ghosts);
				grid[x][y] = ghostSquare;
				break;
			case 'P':
				Square playerSquare = boardCreator.createGround();
				grid[x][y] = playerSquare;
				startPositions.add(playerSquare);
				break;
			case 'H':
				Square holeSquare = boardCreator.createGround();
				grid[x][y] = holeSquare;
				levelCreator.createHole().occupy(holeSquare);
				break;
			case 'T':
				Square teleportSquare = boardCreator.createGround();
				Teleport t = levelCreator.createTeleport();
				teleportList.add(t);
				t.occupy(teleportSquare);
				grid[x][y] = teleportSquare;
				break;
			case 'B':
				Square bridgeSquare = boardCreator.createGround();
				Bridge bridge = levelCreator.createBridge();
				bridgeList.add(bridge);
				bridge.occupy(bridgeSquare);
				grid[x][y] = bridgeSquare;
				break;
			case 'F':
				Square fruitSquare = boardCreator.createGround();
				grid[x][y] = fruitSquare;
				fruitPositions.add(fruitSquare);
				break;
			case 'o':
				Square superPelletSquare = boardCreator.createGround();
				grid[x][y] = superPelletSquare;
				levelCreator.createSuperPellet().occupy(superPelletSquare);
				Level.superPelletLeft++;
				break;
			default:
				throw new PacmanConfigurationException("Invalid character at "
						+ x + "," + y + ": " + c);
		}
	}

	private Square makeGhostSquare(List<NPC> ghosts) {
		Square ghostSquare = boardCreator.createGround();
		NPC ghost = levelCreator.createGhost();
		ghosts.add(ghost);
		ghost.occupy(ghostSquare);
		return ghostSquare;
	}

	/**
	 * Parses the list of strings into a 2-dimensional character array and
	 * passes it on to {@link #parseMap(char[][])}.
	 * 
	 * @param text
	 *            The plain text, with every entry in the list being a equally
	 *            sized row of squares on the board and the first element being
	 *            the top row.
	 * @return The level as represented by the text.
	 * @throws PacmanConfigurationException If text lines are not properly formatted.
	 */
	public Level parseMap(List<String> text) {
		checkMapFormat(text);

		int height = text.size();
		int width = text.get(0).length();
		int firstSectionEnd = findSectionWidth(text, height, 0);

		char[][] map = new char[width][firstSectionEnd];
		for (int y = 0; y < height && (text.get(y).charAt(0) != SEPARATOR); y++) {
			for (int x = 0; x < width; x++) {
				map[x][y] = text.get(y).charAt(x);
			}
		}

		Launcher launcher = Launcher.getLauncher();
		if(launcher.getBoardToUse() == "/boardFruit.txt") {
			firstSectionEnd++;
			int secondSectionEnd = findSectionWidth(text, height, firstSectionEnd);
			List<int[]> teleportRefs = parseTeleport(text, firstSectionEnd, secondSectionEnd);
			secondSectionEnd++;
			List<char[]> bridgeRefs = parseBridge(text, secondSectionEnd, height);
			return parseMap(map, teleportRefs, bridgeRefs);
		}
		return parseMap(map);
	}
	
	private int findSectionWidth(List<String> text, int height, int start){
		int result;
		for (result = start; result < height && (text.get(result).charAt(0) != SEPARATOR); result++){}
		return result;
	}
	
	/**
	 * Check the correctness of the map lines in the text.
	 * @param text Map to be checked
	 * @throws PacmanConfigurationException if map is not OK.
	 */
	private void checkMapFormat(List<String> text) {	
		if (text == null) {
			throw new PacmanConfigurationException(
					"Input text cannot be null.");
		}

		if (text.isEmpty()) {
			throw new PacmanConfigurationException(
					"Input text must consist of at least 1 row.");
		}

		int width = text.get(0).length();

		if (width == 0) {
			throw new PacmanConfigurationException(
				"Input text lines cannot be empty.");
		}

		for (String line : text) {
			if (line.length() != width) {
				throw new PacmanConfigurationException(
					"Input text lines are not of equal width.");
			}
		}		
	}

	/**
	 * Parses the provided input stream as a character stream and passes it
	 * result to {@link #parseMap(List)}.
	 * 
	 * @param source
	 *            The input stream that will be read.
	 * @return The parsed level as represented by the text on the input stream.
	 * @throws IOException
	 *             when the source could not be read.
	 */
	public Level parseMap(InputStream source) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				source, "UTF-8"))) {
			List<String> lines = new ArrayList<>();
			while (reader.ready()) {
				lines.add(reader.readLine());
			}
			return parseMap(lines);
		}
	}
	
	/**
	 * Parses the list of strings into a list of array containing the coordinates of
	 * the Square objects referenced a teleport
	 * 
	 * @param text
	 *            The plain text, with every entry in the list being a equally
	 *            sized row of squares on the board and the first element being
	 *            the top row.
	 * @param y the row where the declaration of the references begin
	 * @return The level as represented by the text.
	 * @throws PacmanConfigurationException If text lines are not properly formatted.
	 */
	private List<int[]> parseTeleport(List<String> text, int y, int sectionEnd){
		List<int[]> teleportRefs = new ArrayList<>();
		String s;
		String[] ts;
		int[] elem;
		for (; y < sectionEnd; y++) {
			s = text.get(y).trim();
			ts = s.split(" ");
			if(ts.length == 2){
				try{
					if(Integer.valueOf(ts[0]) >= 0 && Integer.valueOf(ts[1]) >= 0){
						elem = new int[] {Integer.valueOf(ts[0]), Integer.valueOf(ts[1])};
					}
					else{
						throw new PacmanConfigurationException(
							"The teleport refereces section must contain two positive integer separated by a white space");
					}
				}
				catch(NumberFormatException e){
					throw new PacmanConfigurationException(
						"The teleport refereces section must contain two positive integer separated by a white space");
				}
			}
			else{
				throw new PacmanConfigurationException(
						"The teleport refereces section must contain two positive integer separated by a white space");
			}
			teleportRefs.add(elem);
		}
		return teleportRefs;
	}
	
	private List<char[]> parseBridge(List<String> text, int y, int width){
		List<char[]> bridgeRefs = new ArrayList<>();
		char c1, c2;
		char[] elem;
		String s;
		String[] ts;
		for (; y < width; y++) {
			s = text.get(y).trim();
			ts = s.split(" ");
			if(ts.length == 2 && ts[0].length() == 1 && ts[1].length() == 1){
				c1 = ts[0].charAt(0);
				c2 = ts[1].charAt(0);
				if(c1 != 'H' && c1 != 'V' || c2 != 'P' && c2 != 'N' && c2 != 'F'){
					throw new PacmanConfigurationException("Incorrect Bridge data : "+c1+c2);
				}
				else{
					elem = new char[] {c1, c2};
					bridgeRefs.add(elem);
				}
			}
			else {
				throw new PacmanConfigurationException("Bridge data must contain two character separated by only one space");
			}
		}
		return bridgeRefs;
	}
}
