package board;

import java.util.ArrayList;
import java.util.List;

public class Board {
	
	int[][] boardMat;
	int[][] matIminus1;
	int[][] matIminus2;
	// For ko rule, matIminus2 should never equal mat
	
	int[][] slotDegsOfFreeDom;
	int boardSize;

	int player1 = 1;
	int player2 = 2;
	
	int scoreP1 = 0;
	int scoreP2 = 0;
	
	// Stones have label of the playerNo. Use to calculate scoring.
	final List<Stone> removedStones = new ArrayList<>();
	
	List<Chain> chains = new ArrayList<>();
	
    /**
     * Create a board in a 2x2 matrix
     * The possible slot combinations are 0 1 or 2 at the moment.
     * !! this board works a little different in terms of counting.
     * Here 0 means 1 in the Tui!!
     * This script expects 0 as input. Meaning the boardsize inputted
     * is always boardSize-1. (because 0 counts as 1).
     * 
     * @param boardSize	integer size of the square board to initialize.
     */
	public Board(int boardSize) {
		
		this.boardMat = new int[boardSize][boardSize];
		this.matIminus1 = new int[boardSize][boardSize];
		this.matIminus2 = new int[boardSize][boardSize];
		this.boardSize = boardSize;
		
		for (int r = 0; r < boardSize; r++)	{
			for (int c = 0; c < boardSize; c++)	{
				this.boardMat[r][c] = 0;
				this.matIminus1[r][c] = 0;
				this.matIminus2[r][c] = 0;
			}
		}
	}
	
    /**
     * Prints out boardMat to the console.
     * 
     * @param 
     */
	
	public void toLinePrint() {
		
		System.out.println(".............");
		for (int r = 0; r < boardSize; r++)	{
			System.out.print("|");
			for (int c = 0; c < boardSize; c++)	{
				System.out.print(this.boardMat[r][c]);
			}
			System.out.println("|");
		}
		System.out.println(".............");
		
	}
	
    /**
     * Prints out an 2x2 array to the console.
     * 
     * @param int[][]
     */
	
	public void arraytoLinePrint(int[][] arrayToPrint) {
		
		System.out.println(".............");
		for (int r = 0; r < boardSize; r++)	{
			System.out.print("|");
			for (int c = 0; c < boardSize; c++)	{
				if (arrayToPrint[r][c] != 0) {
					System.out.print(arrayToPrint[r][c]);
				} else {
					System.out.print(" ");
				}
			}
			System.out.println("|");
		}
		System.out.println(".............");
		
	}
	
    /**
     * Prints out a gui for the legacy clients to string.
     * 
     * @param 
     */
	public String toStringClient() {
		
		String lines = "";
		for (int r = 0; r < boardSize; r++)	{
			
			if (r == 0) {
				// Write till 99
				// or like this:
				// 9
				// 9
				// Little bit more pleasant to look at.
				
				String colLabelWriter1 = "   ";
				String colLabelWriter2 = "   ";
				String colLabelSpacer3 = "   ";
				
				for (int colLabelInt = 0; colLabelInt < boardSize; colLabelInt++) {
					
					int colLabelIntPlus1 = colLabelInt + 1;
					String colLabelStr = "" + colLabelIntPlus1;
					
					// Array boardsize
					if (colLabelInt < 9) {
						// Two spaces
						colLabelWriter1 = colLabelWriter1 + "  ";						
					} else {
						// Number, One space
						colLabelWriter1 = colLabelWriter1 + colLabelStr.charAt(1) + " ";
					}
					
					colLabelWriter2 = colLabelWriter2 + colLabelStr.charAt(0) + " ";
					
					colLabelSpacer3 = colLabelSpacer3 + "  ";
					
				}
				
				lines = lines + colLabelWriter1 
						+ "\n" + colLabelWriter2 + "\n" + colLabelSpacer3 + "\n";
				
			}
			
			// Now push in the numberline
			String numberLine = "";
			
			// Correction r+1 only for gui !!
			int rplus1 = r + 1;
			
			if (r < 9) {
				
				numberLine = numberLine + " " + rplus1 + " ";
				
			} else {
				
				numberLine = numberLine + rplus1 + " ";
			}
			
			for (int c = 0; c < boardSize; c++)	{
				if (this.boardMat[r][c] == 0) {
					numberLine = numberLine + "+" + " ";
				} else {
					
					numberLine = numberLine + this.boardMat[r][c] + " ";
				}
			}
			
			// Write the line
			lines = lines + numberLine + "\n";
			
		}
		
		return lines;
		
	}
	
    /**
     * Checks if the field is not already taken and flips a boolean to notify the move is not valid.
     * More functional checks such as the Ko rule are in putStoneForPlayer.
     * 
     * @param	playerNo	player that makes the move
     * @param	row			row that stone is placed.
     * @param	col			collumn that stone is placed
     */
	
	public Boolean isMoveValid(int playerNo, int row, int col) {
		
		// If any is not in order response becomes false.		
		Boolean response = true;
		
		// 1 is the field taken ?
		
		if (this.boardMat[row][col] != 0) {
			
			response = false;
			
		}
		return response;
	}
	
    /**
     * puts a stone on the board for the player and checks if 
     * the Ko rule is not violated.
     * In order to determine the Ko rule violation (or not)
     * a backup is retained in memory, as well as previous
     * copies of the board situation.
     * If the Ko rule is violated the backup is reverted.
     * 
     * If no violation occurs the stone is placed and 
     * the existing stones are chained if possible, or 
     * captured if is the case.
     * 
     * The removed stones are returned for use with for ex. the GUI.
     * (*can also be improved by polling list)
     * 
     * @param	playerNo	player that makes the move
     * @param	row			row that stone is placed.
     * @param	col			collumn that stone is placed
     * @throws	BoardKoRuleViolatedE	Exception ko rule
     * @return	List<Stone>	List of stones removed after adding.
     */
	public List<Stone> putStoneForPlayer(int playerNo, int row, int col,
			Boolean persistent, Boolean getAvgChainLength)
			throws BoardKoRuleViolatedE {
		
		int[][] matBegin = new int[boardSize][boardSize];
		
		for (int r = 0; r < boardSize; r++)	{
			for (int c = 0; c < boardSize; c++)	{
				matBegin[r][c] = boardMat[r][c];
			}
		}
		
		List<Chain> chainsBegin = new ArrayList<Chain>(chains);
		
		this.boardMat[row][col] = playerNo;
		
		// Attempt to put the stone in a chain
		
		final Chain newChain = new Chain(playerNo);
		newChain.addStone(new Stone(playerNo, row, col));
		
		chains.removeIf((chain) -> {
			
			if (chain.getPlayerNo() == playerNo) {
				//System.out.println("removeIf in loop");
				if (chain.isAdjacentToChain(row, col)) {
					
					// Collect stones and add them
					newChain.addStoneList(chain.getStones());
					// Remove this chain using removeIf
					return true;
					
				} else {
					return false;
				}
				
			} else {
				return false;
			}
			
		});
		
		chains.add(newChain);
		newChain.getStones().forEach((stone) -> {
			//System.out.print(stone.row); System.out.print(stone.col);
		});

		
		final List<Stone> toRemoveStones = new ArrayList<>();
		List<Stone> capturedStones = new ArrayList<>();
		
		chains.removeIf((chain) -> {
			// Suicide move only allowed IF the player plays it currently
			// (compared with row and col)
			if (chain.getStones().size() == 1 
					&& chain.getStones().get(0).getRow() == row 
					&& chain.getStones().get(0).getCol() == col) {
				return false;
				
			} else if (chain.calculateDegreesOfFreedom(boardMat) == 0) {
				// Put the stones in an array.
				capturedStones.addAll(chain.getStones());
				
				toRemoveStones.addAll(chain.getStones());
				return true;

			} else {
				
				return false;
			}
		});
		
		// Clear the board of any captured stones.
		toRemoveStones.forEach((stone) -> {
			boardMat[stone.getRow()][stone.getCol()] = 0;
		});
		
		Boolean koRuleWarn = true;
		// Manually check .equals doesn't work
		for (int r = 0; r < boardSize; r++)	{
			for (int c = 0; c < boardSize; c++)	{
				if (matIminus2[r][c] != boardMat[r][c]) {
					koRuleWarn = false;
				}
			}
		}
		
		if (koRuleWarn) {
			
			// Ko rule violation!
			// Revert, and remove the last stone.
			toRemoveStones.clear();
			toRemoveStones.add(new Stone(playerNo, row, col));
			
			for (int r = 0; r < boardSize; r++)	{
				for (int c = 0; c < boardSize; c++)	{
					boardMat[r][c] = matBegin[r][c];
				}
			}
			
			this.chains = new ArrayList<Chain>(chainsBegin);
			
			//System.out.println("Fault! Ko Rule!");
			
			throw new BoardKoRuleViolatedE("Ko Rule violation!");
			
		} else if (!persistent) {
			// For AI and such trying a move, non persistence mode !
			// Removal procedure equals above.
			toRemoveStones.clear();
			toRemoveStones.add(new Stone(playerNo, row, col));
			
			for (int r = 0; r < boardSize; r++)	{
				for (int c = 0; c < boardSize; c++)	{
					boardMat[r][c] = matBegin[r][c];
				}
			}
			
			this.chains = new ArrayList<Chain>(chainsBegin);
			
			
			
			// Persistent !
		} else {
			
			// Ko rule is OK.
			// Write changes
			
			for (int r = 0; r < boardSize; r++)	{
				for (int c = 0; c < boardSize; c++)	{
					this.matIminus1[r][c] = boardMat[r][c];
					this.matIminus2[r][c] = matBegin[r][c];
				}
			}
			
			// For each chain calculate the degrees of freedom with 0
			
			//System.out.println("-----DOF-----");
			calculateScore(playerNo, capturedStones);
			
		}
		if (getAvgChainLength) {
			return newChain.getStones();
		} else {
			return toRemoveStones;
		}
		
	}
	
    /**
     * Calculate the game score based on removal and addition of stones.
     * 
     * If a stone is placed the player gets 1 point.
     * If a stone is captured the player loses a point.
     * 
     * All captured stones are saved in in removedStones.
     * 
     * @param	playerNo		player that makes the move
     * @param	toRemoveStones	list of stones that are being removed
     * @return	void
     */
	
	public void calculateScore(int playerNo, List<Stone> toRemoveStones) {
		
		// Capture, so reverse of Id's
		toRemoveStones.forEach((stone) -> {
			if (stone.getPlayerNo() == 2) {
				this.scoreP1++;
				this.scoreP2--;
				
			} 
			if (stone.getPlayerNo() == 1) {
				this.scoreP1--;
				this.scoreP2++;
			}
		});
		
		if (playerNo == 1) {
			this.scoreP1++;
			
		} else if (playerNo == 2) {
			this.scoreP2++;
		}
		
		// Store all stones in depository.
		
		this.removedStones.addAll(toRemoveStones);
		
	}
	
	public int getScoreP1() {
		return scoreP1;
	}

	public int getScoreP2() {
		return scoreP2;
	}
	
	public int getBoardSize() {
		return boardSize;
	}
	

}
