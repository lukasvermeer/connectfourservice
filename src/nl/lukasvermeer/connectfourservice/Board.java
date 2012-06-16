package nl.lukasvermeer.connectfourservice;

import java.util.Arrays;
import java.util.HashMap;

public class Board implements Cloneable {	
	private int height;
	private int width;
	
	private int turn;
	private Integer[] state;
	
	// LV int cannot be null. Converted to Integer. 
	public Board(int height, int width, int turn, Integer[] state) {
		this.height = height;
		this.width = width;
		
		this.turn = turn;
		this.state = state;
	}
	
	public Board(int height, int width) {
		this.height = height;
		this.width = width;
		
		this.turn = 0;
		this.state = new Integer[(int) width * height];
	}
	
	public Board(HashMap<String, Integer[]> ser) {
		this.height = ser.get("height")[0];
		this.width = ser.get("width")[0];
		
		this.turn = ser.get("turn")[0];
		this.state = ser.get("state");
	}
	
	// LV Needs to be rewritten. Hash does not take into account size of board or whose turn it is.
	public String getHash() {
		return Arrays.toString(state); 
	}
	
	public HashMap<String, Integer[]> getSerializedState() {
		HashMap<String, Integer[]> ser = new HashMap<String, Integer[]>();
		ser.put("width", new Integer[] {width});
		ser.put("height", new Integer[] {height});
		ser.put("turn", new Integer[] {turn});
		ser.put("state", state);
		return ser;
	}
	
	// MF Not sure why String getHash is needed, try to replace it with simple int[] getState.
	// LV Is needed to use as a key for a dictionary to enable caching. Each hash represents a unique board state.
	public Integer[] getState() {
		return state;
	}
	
	// MF If an error, this returns NULL. I cast it to an integer for now, not sure if that is the right way to go....  will that just be 0, and does 0 mean something else? 
	// LV Yeah, well. Let's just say JavaScript is less picky about these things. I'll write a wrapper func.
	// LV Also, let's just let the damn thing return an Integer.
	public Integer getCell(int r, int c) {
		if (r >= 0 && c >= 0 && r < height && c < width) {
			try { return (state[(r*width)+c]); } catch(Exception err) { return null; }
		} else { return Minimax.OFF_BOARD; }
	}
	
	// LV Yeah, well. Kinda ugly. But it's 22.19, I've had half a bottle of wiskey and the karaoke is just about starting to get interesting. Feel free to help this sorry piece of code out of its misery.
	public Integer getNNCell(int r, int c) {
		Integer ret = getCell(r,c);
		if (ret == null) {
			return 3;
		}
		return ret;
	}

	public boolean playColumn(int c) {
		if (this.getCell(0,c) == null) {
			int i = this.height - 1;
			while (this.getCell(i,c) != null) {
				i--;
			}
			this.state[i*this.width+c] = this.turn;
		
			this.turn = Math.abs(this.turn-1);
			return true;
		} else { return false; }
	};
	
	public boolean isFull() {
		for (int n = 0; n < (height*width); ++n) {
			if (state[n]==null) { return false; }
		}
		return true;
	}

	public int getTurn() {
		return turn;
	}
	
	public int getSize() {
		return height*width;
	}
	
	@Override
	public Board clone() {
        return new Board(height, width, turn, state.clone());
    } 
}