package nl.lukasvermeer.connectfourservice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class Service extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		Map<String,String> jsonResp = getResponse(req);
		 
		resp.setContentType("text/x-json;charset=UTF-8");           
        resp.setHeader("Cache-Control", "no-cache");
        
        try {
        	System.out.println(jsonResp.toString());
            JSONObject json = new JSONObject(jsonResp);
            json.write(resp.getWriter());
        } catch (Exception e) {
            throw new IOException("IOException in populateWithJSON", e);
        }
	}
	
	private Map<String,String> getResponse(HttpServletRequest req) {
		HttpSession session = req.getSession();
		Map<String,String> jsonResp = new HashMap<String,String>();

		Board board;
		
		// parse "newgame" parameter.
		// if true, should start new game.
		String newGameString = req.getParameter("newgame");
		Boolean newGame = false;
		if (newGameString != null) { newGame = Boolean.parseBoolean(newGameString); }		
		if (newGame) {
			int w = Integer.parseInt(req.getParameter("width"));
			int h = Integer.parseInt(req.getParameter("height"));
			board = new Board(w,h);
			session.setAttribute("board", board.getSerializedState());
		}
		// if not a new game, we should have saved state for this session.
		else {
			Object boardObject = session.getAttribute("board");
			if (boardObject != null) {
				@SuppressWarnings("unchecked") // if this fails, all is lost anyway.
				HashMap<String, Integer[]> serializedBoard = (HashMap<String, Integer[]>) boardObject;
				board = new Board(serializedBoard);
			}
			else {
				jsonResp.put("response", "error");
				jsonResp.put("message", "Were we playing a game? I seem to have forgotten.");
				return jsonResp;
			}
		}

		// parse "move" parameter.
		// tells us what move our opponent made (if any).
		String move = req.getParameter("move");		
		if (move != null) {
			if (board.playColumn(Integer.parseInt(move.toString()))) {
				session.setAttribute("board", board.getSerializedState());
			}
			else {
				jsonResp.put("response", "error");
				jsonResp.put("message", "Looks like an illegal move to me.");
				return jsonResp;				
			}
		}
		// if opponent did not send a move, check it's our turn.
		else if (board.getTurn() == 1) {
			jsonResp.put("response", "error");
			jsonResp.put("message", "I thought it was your turn!");
			return jsonResp;
		}
		
		// parse "state" parameter.
		// optional parameter used for testing synchronization between clients.
		String state = req.getParameter("state");
		if (state != null) {			
			String[] stateString = req.getParameter("state").split(",");
			Integer[] stateMatrix = new Integer[board.getSize()];
			for (int i = 0; i < board.getSize(); i++) {
				if (stateString[i] == null || stateString[i].isEmpty()) {
					stateMatrix[i] = null; 
				} else { 
					stateMatrix[i] = Integer.parseInt(stateString[i]);
				} 
			}		
			if (stateMatrix.equals(board.getState())) {
				jsonResp.put("response", "error");
				jsonResp.put("message", "We seem to disagree about the current board state.");
				return jsonResp;
			}
		}
		
		// check if the game is already over.
		if (Math.abs(Minimax.getScore(board, 1)) == Minimax.WIN_SCORE) {
			jsonResp.put("response", "error");
			jsonResp.put("message", "This game is over!");
			return jsonResp;
		}
		// if not over, let's play.
		else {						
			Integer[] m = Minimax.minMax(1, board, 2);

			jsonResp.put("response", "move");
			jsonResp.put("move", m[0].toString());
			board.playColumn(m[0]);
			jsonResp.put("state", board.getHash().replace("null", "").replace(" ", "").replace("[", "").replace("]", ""));
			if (Minimax.getScore(board, 0) == Minimax.WIN_SCORE) {
				jsonResp.put("message", "Looks like I won!");
			}
			session.setAttribute("board", board.getSerializedState());	
			return jsonResp;
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}
}