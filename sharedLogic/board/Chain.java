package board;

import java.util.ArrayList;
import java.util.List;

public class Chain {
	final List<Stone> stones = new ArrayList<>();
	final int playerNo;
	int degreesOfFreedom;
	
	Chain(int playerNo) {
		this.playerNo = playerNo;
		
	}
	
	public List<Stone> getStones () {
		
		return stones;
	}
	
	public void addStone(Stone stone) {
		
		stones.add(stone);
	}
	
	public void addStoneList(List<Stone> stonesInput) {
		
		stones.addAll(stonesInput);
		
	}
	
	public int getPlayerNo() {
		
		return playerNo;
	}
	
	public int getDegreesOfFreedom() {
		
		return degreesOfFreedom;
	}
	
	public void setDegreesOfFreedom(int degreesOfFreedom) {
		
		this.degreesOfFreedom = degreesOfFreedom;
	}
	
	public int calculateDegreesOfFreedom(int[][] mat) {
		
		// Map the matrix on a bigger one to prevent boundary conditions
		int boardSize = mat.length;
		
		int[][] paddedBoard = new int[boardSize+2][boardSize+2];
		// Fill the list with an arbitrary number on the edges
		for(int i=0; i<boardSize+2; i++){
			paddedBoard[0][i] = 3;
			paddedBoard[i][0] = 3;
			paddedBoard[boardSize][i] = 3;
			paddedBoard[i][boardSize] = 3;
		}
		
		for(int r=0; r<boardSize; r++) {
			for(int c=0; c<boardSize; c++) {
				paddedBoard[r+1][c+1] = mat[r][c];
			}
		}
		
		// Because of foreach
		int[] dOF= new int[1];
		dOF[0] = 0;
		
		stones.forEach((stone)-> {
			
			// Remap to the padded board, this is because the wall is always +0 DOF
			// Saves a lot of boundary conditions checkers.
			int sRow = stone.getRow()+1;
			int sCol = stone.getCol()+1;
			if (paddedBoard[sRow+1][sCol] == 0) {
				dOF[0]++;
			}
			if (paddedBoard[sRow-1][sCol] == 0) {
				dOF[0]++;
			}
			if (paddedBoard[sRow][sCol+1] == 0) {
				dOF[0]++;
			}
			if (paddedBoard[sRow][sCol-1] == 0) {
				dOF[0]++;
			}
			
		});
		
		return dOF[0];
	}
	
	public Boolean isAdjacentToChain(int row, int col) {
		
		// Trick to write to the field in the foreach
		final Boolean[] actionPerformed= new Boolean[1];
		actionPerformed[0] = false;
		
		//System.out.println("STONE PLACED:"+row+col);
		
		// See if the stone can attach to a chain.
		stones.forEach((stone)->{
			
			//System.out.println("Trying to match:"+stone.getRow()+stone.getCol());
			
			
			// if the distance equals 1 in any direction, it's a chain candidate!
			
			// To the left
			if (stone.getCol()-col == 1 && stone.getRow()==row) {
				//System.out.println("Stone to the left");
				actionPerformed[0] = true;
			}
			
			// to the right
			if (col-stone.getCol() == 1 && stone.getRow()==row) {
				//System.out.println("Stone to the right");
				actionPerformed[0] = true;
			}
			
			// to the top
			if (stone.getCol()==col && row-stone.getRow() == 1) {
				//System.out.println("Stone to the top");
				actionPerformed[0] = true;
			}
			
			// to the bottom
			if (stone.getCol()==col && stone.getRow()-row == 1) {
				//System.out.println("Stone to the bottom");
				actionPerformed[0] = true;
			}
			
		});
		
		return actionPerformed[0];
	}

}
