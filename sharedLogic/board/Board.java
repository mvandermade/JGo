package board;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import serverModel.GameObj;

public class Board {
	
	int[][] mat;
	int[][] matIminus1;
	int[][] matIminus2;
	// For ko rule, matIminus2 should never equal mat
	
	int[][] slotDegsOfFreeDom;
	int boardSize;

	int P1 = 1;
	int P2 = 2;
	
	int scoreP1=0;
	int scoreP2=0;
	
	// Stones have label of the playerNo. Use to calculate scoring.
	final List<Stone> removedStones = new ArrayList<>();
	
	List<Chain> chains = new ArrayList<>();
	
	
	public Board(int boardSize) {
		
		this.mat = new int[boardSize][boardSize];
		this.matIminus1 = new int[boardSize][boardSize];
		this.matIminus2 = new int[boardSize][boardSize];
		this.boardSize = boardSize;
		
		for (int r = 0; r < boardSize; r++)	{
			for (int c = 0; c < boardSize; c++)	{
				this.mat[r][c] = 0;
				this.matIminus1[r][c] = 0;
				this.matIminus2[r][c] = 0;
			}
		}
		
	}
	
	public void toLinePrint() {
		
		System.out.println(".............");
		for (int r = 0; r < boardSize; r++)	{
			System.out.print("|");
			for (int c = 0; c < boardSize; c++)	{
				System.out.print(this.mat[r][c]);
			}
			System.out.println("|");
		}
		System.out.println(".............");
		
	}
	
	public void ArraytoLinePrint(int[][] arrayToPrint) {
		
		System.out.println(".............");
		for (int r = 0; r < boardSize; r++)	{
			System.out.print("|");
			for (int c = 0; c < boardSize; c++)	{
				if (arrayToPrint[r][c]!=0) {
					System.out.print(arrayToPrint[r][c]);
				} else {
					System.out.print(" ");
				}
			}
			System.out.println("|");
		}
		System.out.println(".............");
		
	}
	
	public String toStringClient() {
		
		String lines = "";
		for (int r = 0; r < boardSize; r++)	{
			
			if (r == 0) {
				// Write till 99
				// or like this:
				// 9
				// 9
				
				String colLabelWriter1 = "   ";
				String colLabelWriter2 = "   ";
				String colLabelSpacer3 = "   ";
				
				for (int colLabelInt = 0; colLabelInt < boardSize; colLabelInt++) {
					
					int colLabelIntPlus1 = colLabelInt+1;
					String colLabelStr = ""+colLabelIntPlus1;
					
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
				
				lines = lines + colLabelWriter1 + "\n" + colLabelWriter2 + "\n" + colLabelSpacer3 + "\n";
				
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
				
				if (this.mat[r][c] == 0) {
					numberLine = numberLine + "+" + " ";
				} else {
					
					numberLine = numberLine + this.mat[r][c] + " ";
				}
			}
			
			// Write the line
			
			lines = lines + numberLine + "\n";
			
		}
		
		return lines;
		
	}
	
	public Boolean isMoveValid(int playerNo, int row, int col) {
		
		// If any is not in order response becomes false.		
		Boolean response = true;
		
		// 1 is the field taken ?
		
		if (this.mat[row][col] != 0) {
			
			response=false;
			
		}
		
		// 2 ko rule
		
		// Check if a move was made previously by the same player at the same spot
		// mat = 0. Otherwise the move is illegal by 1.
		// now if the same stone was here before mat = 0, the move is repetitive.
		
		
		// 3 Self capture or suicide move is allowed.
		
		return response;
	}
	
	// Leftabove = mat[0][0] [row][collumn]
	
	public List<Stone> putStoneForPlayer(int playerNo, int row, int col) throws BoardKoRuleViolated {
		
		int[][] matBegin = new int[boardSize][boardSize];
		
		for (int r = 0; r < boardSize; r++)	{
			for (int c = 0; c < boardSize; c++)	{
				matBegin[r][c] = mat[r][c];
			}
		}
		
		List<Chain> chainsBegin = new ArrayList<Chain>(chains);
		
		this.mat[row][col] = playerNo;
		
		// Attempt to put the stone in a chain
		
		final Chain newChain = new Chain(playerNo);
		newChain.addStone(new Stone(playerNo, row, col));
		
		chains.removeIf((chain)->{
			
			if (chain.getPlayerNo() == playerNo) {
				//System.out.println("removeIf in loop");
				if(chain.isAdjacentToChain(row, col)) {
					
					// Collect stones and add them
					//System.out.println("isAdjacent: listing:");
					//chain.getStones().forEach((stone)->{System.out.print(stone.row); System.out.print(stone.col);});
					//System.out.println("endof list");
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
		//System.out.println("added to newchain:");
		newChain.getStones().forEach((stone)->{System.out.print(stone.row); System.out.print(stone.col);});

		
		final List<Stone> toRemoveStones = new ArrayList<>();
		List<Stone> capturedStones = new ArrayList<>();
		
		chains.removeIf((chain)->{
			// Suicide move only allowed IF the player plays it currently (compared with row and col)
			if (chain.getStones().size() == 1 && chain.getStones().get(0).getRow() == row && chain.getStones().get(0).getCol() == col) {
				return false;
				
			} else if (chain.calculateDegreesOfFreedom(mat) == 0) {
				// Put the stones in an array.
				capturedStones.addAll(chain.getStones());
				
				toRemoveStones.addAll(chain.getStones());
				return true;

			} else {
				
				return false;
			}
		});
		
		// Clear the board of any captured stones.
		toRemoveStones.forEach((stone)-> {
			mat[stone.getRow()][stone.getCol()] = 0;
		});
		
		Boolean koRuleWarn = true;
		int countEquals = 0;
		// Manually check .equals doesn't work
		for (int r = 0; r < boardSize; r++)	{
			for (int c = 0; c < boardSize; c++)	{
				if(matIminus2[r][c] != mat[r][c]) {
					koRuleWarn = false;
				}
			}
		}
		
		if(koRuleWarn) {
			
			// Ko rule violation!
			toRemoveStones.clear();
			toRemoveStones.add(new Stone(playerNo, row, col));
			
			for (int r = 0; r < boardSize; r++)	{
				for (int c = 0; c < boardSize; c++)	{
					mat[r][c] = matBegin[r][c];
				}
			}
			
			this.chains = new ArrayList<Chain>(chainsBegin);
			
			//System.out.println("Fault! Ko Rule!");
			
			throw new BoardKoRuleViolated("Ko Rule violation!");
			
		} else {
			
			// Ko rule OK.
			//System.out.println("Ko OK!");
			//System.out.println("mat");
			//ArraytoLinePrint(mat);
			
			//System.out.println("matIminus1");
			//ArraytoLinePrint(matIminus1);
			
			//System.out.println("matIminus2");
			//ArraytoLinePrint(matIminus2);
			
			for (int r = 0; r < boardSize; r++)	{
				for (int c = 0; c < boardSize; c++)	{
					this.matIminus1[r][c] = mat[r][c];
					this.matIminus2[r][c] = matBegin[r][c];
				}
			}
			//System.out.println("Chain count (both colours):"+chains.size());
			
			// For each chain calculate the degrees of freedom with 0
			
			
			//System.out.println("-----DOF-----");
			calculateScore(playerNo, capturedStones);
			
		}
		
		return toRemoveStones;
		
	}
	
	public void calculateScore(int playerNo, List<Stone> toRemoveStones) {
		
		//System.out.println("SCORING FOR"+playerNo);
		//System.out.println("SCORING: P1"+scoreP1+"SCORING P2:"+scoreP2);
		
		// Capture, so reverse of Id's
		toRemoveStones.forEach((stone)->{
			if(stone.getPlayerNo() == 2) {
				this.scoreP1++;
				
			} 
			if(stone.getPlayerNo() == 1) {
				this.scoreP2++;
			}
		});
		
		if(playerNo == 1) {
			this.scoreP1++;
			
		} else if (playerNo == 2) {
			this.scoreP2++;
		}
		
		//System.out.println("SCORING-END: P1"+scoreP1+"SCORING P2:"+scoreP2);
		
		this.removedStones.addAll(toRemoveStones);
		
	}
	
	public int getScoreP1() {
		return scoreP1;
	}

	public int getScoreP2() {
		return scoreP2;
	}
	

}
