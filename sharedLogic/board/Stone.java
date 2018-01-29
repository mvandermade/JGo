package board;

public class Stone {
	
	public int playerNo;
	public int row;
	public int col;
	
	
	Stone(int playerNo, int row, int col) {
		this.playerNo = playerNo;
		this.row = row;
		this.col = col;
		
	}
	
	public int getPlayerNo() {
		
		return playerNo;
	}
	
	public int getCol() {
		
		return col;
	}
	
	public int getRow() {
		
		return row;
	}


}
