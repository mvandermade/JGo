package board;



public class Board {
	
	int[][] mat;
	int boardSize;
	
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
	
	// Leftabove = mat[0][0] [row][collumn]

}
