package board;

public class Board {
	
	int[][] mat;
	int[][] matIminus1;
	// For ko rule, matIminus2 should never equal mat
	int boardSize;

	int P1 = 1;
	int P2 = 2;
	
	int scoreP1=0;
	int scoreP2=0;
	
	public Board(int boardSize) {
		
		this.mat = new int[boardSize][boardSize];
		this.matIminus1 = new int[boardSize][boardSize];
		this.boardSize = boardSize;
		
		for (int r = 0; r < boardSize; r++)	{
			for (int c = 0; c < boardSize; c++)	{
				this.mat[r][c] = 0;
				this.matIminus1[r][c] = 0;
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
		if (this.matIminus1[row][col] == playerNo) {
			
			if (this.mat[row][col] == 0) {
				// Now the same player as matIminus1 is putting a stone, not allowed!
				response = false;
				System.out.println("ko rule, forbidden!");
			}
		}
		
		// 3 
		
		return response;
	}
	
	// Leftabove = mat[0][0] [row][collumn]
	
	public void putStoneForPlayer(int PlayerNo, int row, int col) {
		
		this.matIminus1[row][col] = mat[row][col];
		this.mat[row][col] = PlayerNo;
		
	}
	
	public void updateBoardScore() {
		
	}
	

}
