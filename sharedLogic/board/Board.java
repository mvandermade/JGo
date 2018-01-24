package board;



public class Board {
	
	int[][] mat;
	int boardSize;
	int P1 = 1;
	int P2 = 2;
	
	public Board(int boardSize) {
		
		this.mat = new int[boardSize][boardSize];
		this.boardSize = boardSize;
		
		for (int r = 0; r < boardSize; r++)	{
			for (int c = 0; c < boardSize; c++)	{
				this.mat[r][c] = 0;
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
	
	public Boolean isMoveValid(int row, int col) {
		
		Boolean response = false;
		
		// 1 is the field taken ?
		
		if (this.mat[row][col] == 0) {
			
			response=true;
			
		}
		
		return response;
	}
	
	// Leftabove = mat[0][0] [row][collumn]
	
	public void putStoneForPlayer(int PlayerNo, int row, int col) {
		
		this.mat[row][col] = PlayerNo;
	}

}
