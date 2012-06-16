package nl.lukasvermeer.connectfourservice;

import java.util.Hashtable;

public class Minimax {
	public static int OFF_BOARD = 2;
	public static int WIN_SCORE = 9999;
	
	private static Hashtable<String, Integer> scoreCache;
	private static Hashtable<String, Integer[]> moveCache;
	
	//TODO: We don't want this stored in a 'session', do we? We should be hopefully stateless.
	// LV Yes we do. The point is that each board state will be equally good irrespective of the game. If we start to use caching we will want to store this kind of thing on the server for reuse in the next game.
	// LV Current implementation is not actually storing stuff on the server. Could perhaps be made more persistent.
	//TODO: Declaration of this variable is all screwed up... public? static? correct way to initialize new int[]?
	// LV Again, JavaScript is less picky about these things. I think in Java this should be a dictionary. Hashed board states go in, cached scre comes out.
	
	// MF Re-enable all this once you have the AJAX working
	// LV That would be now. Took some time to get all the errors out. Sure it could be implemented better and more Java-like.
	public static Integer[] minMax(int p, Board b, int d) {
		// p: player designation
		// b: current board state
		// d: iteration depth
		
		Integer score = getScore(b, p);

		// some game states do not require any thinking (e.g. already lost or won)
		if (d == 0) return new Integer[] {null, score}; // max depth reached. just return the score.
		if (score == -WIN_SCORE) return new Integer[] {null,score}; // we lose, not good.
		if (score == WIN_SCORE) return new Integer[] {null,score}; // we win, good.
		if (b.isFull()) return new Integer[] {null,8888}; // board is full, pretty good, but not as good as winning.
		
		if (moveCache == null) { System.out.println("moveCache was null."); moveCache = new Hashtable<String, Integer[]>(); }
		
		// LV See if we can get the next move from the cache.
		// System.out.println(d+": "+b.getHash());
		
		if (moveCache.containsKey(b.getHash())) {
			 Integer[] c = moveCache.get(b.getHash());
			 // System.out.println("cache possible for hash = " + b.getHash());
			 if (c[0] >= d) {
				 // System.out.println("cache hit!");
				 return new Integer[] {c[1],c[2]};
			 }
		}
		
		// LV No cache. 
		/*
		// simple optimization attempt. look ahead two moves to see if win or lose possible.
		// this prevents the algorithm from exploring parts of the state space that are unrealistic to occur.
		if (d > 2) {
			for (int q = 0; q < b.getSize(); ++q) { // for each possible move.
				Board n = b.clone(); // copy current state.
				if (n.playColumn(q)) { // make move.
					Integer[] qs = minMax(p,n,2); // look ahead one move.
					if (qs[1] == WIN_SCORE || qs[1] == -WIN_SCORE)  {
						return new Integer[] {q,qs[1]}; // if I win or lose, stop exploring.
					}}}}
		 */
		
		// algorithm considers best and worst possible moves in one loop to save lines of code.
		Integer maxv = 0; // best score.
		Integer maxc = -1; // column where best score occurs.
		Integer minv = 999999; // worst score.
		Integer minc = -1; // colum where worst score occurs.
		for (int q = 0; q < b.getSize(); ++q) { // for each possible move.
			Board n = b.clone(); // copy current state.
			if (n.playColumn(q)) { // make move.
				Integer[] next = minMax(p,n,d-1); // look ahead d-1 moves.
				if (maxc == -1 || next[1] > maxv) { maxc = q; maxv = next[1]; } // compare to previous best.
				if (minc == -1 || next[1] < minv) { minc = q; minv = next[1]; } // compare to previous worst.
			}}

		if (b.getTurn()==p) { // if it is our turn.
			moveCache.put(b.getHash(), new Integer[] {d,maxc,maxv/2+score/2});
			return new Integer[] {maxc,maxv/2+score/2}; // make best possible move.
		} else { // otherwise.
			moveCache.put(b.getHash(), new Integer[] {d,minc,minv/2+score/2});
			return new Integer[] {minc,minv/2+score/2}; // make worst possible move.
		}
	}
	
	// LV Moved this function here. Seemed to make sense.
	public static Integer getScore(Board b, int p) {
		if (scoreCache==null) { System.out.println("moveCache was null."); scoreCache = new Hashtable<String, Integer>(); }
		
		if (scoreCache.containsKey(b.getHash())) {
			return scoreCache.get(b.getHash());
		} else {
			int score = 0;
			for (int i = 0; i < b.getSize(); ++i) {
				for (int j = 0; j < b.getSize() - 4 + 1; ++j) {
					Integer[][] line = new Integer[6][4];
					for (int k = 0; k < 6; ++k) { 
						line[k][0] = 0;line[k][1] = 0;line[k][2] = 0;line[k][3] = 0; 
					}

					for (int n = j; n < j + 4; ++n) {
						line[0][b.getNNCell(n,i)]++; // columns
						line[1][b.getNNCell(i,n)]++; // rows
						line[2][b.getNNCell(n,n-i)]++; // diagonal southwest half
						line[3][b.getNNCell(b.getSize()-n-1,n-i)]++; // diagonal northwest half
						if (i>0) {
							line[4][b.getNNCell(n-i,n)]++; // diagonal northweast half
							line[5][b.getNNCell(n+i,b.getSize()-n-1)]++; // diagonal southeast half
						}
					}

					for (int k = 0; k < 6; ++k) {
						if (line[k][p]==4) return WIN_SCORE; // win
						if (line[k][Math.abs(p-1)]==4) return -WIN_SCORE; // lose
						if (line[k][Math.abs(p-1)]==0 && line[k][OFF_BOARD]==0) { score += Math.pow(line[k][p],2); }
					}
				}
			}
			
			scoreCache.put(b.getHash(),score);
			return score;
		}
	}
}
