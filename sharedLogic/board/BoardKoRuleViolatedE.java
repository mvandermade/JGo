package board;


public class BoardKoRuleViolatedE extends Exception {

	/**
	 * Triggers when the ko rule is violated by the board.
	 */
	private static final long serialVersionUID = 1L;

	public BoardKoRuleViolatedE() {
	}

	public BoardKoRuleViolatedE(String arg0) {
		super(arg0);
	}

	public BoardKoRuleViolatedE(Throwable arg0) {
		super(arg0);
	}

	public BoardKoRuleViolatedE(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public BoardKoRuleViolatedE(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
