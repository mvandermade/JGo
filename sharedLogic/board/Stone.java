package board;

public class Stone {
	
	public int playerNo;
	public int row;
	public int col;
	
	/**
	 * Holder of stone data of a particular playerNo.
	 * 
	 * @param 	playerNo	Owner of the stone
	 * @param	row			row of position on the board
	 * @param	col			collumn of position on the board
	 * 
	 */
	
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
