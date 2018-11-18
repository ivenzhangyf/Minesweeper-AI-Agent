

/*

AUTHOR:      John Lu

DESCRIPTION: This file contains your agent class, which you will
             implement.

NOTES:       - If you are having trouble understanding how the shell
               works, look at the other parts of the code, as well as
               the documentation.

             - You are only allowed to make changes to this portion of
               the code. Any changes to other portions of the code will
               be lost when the tournament runs your code.
*/

package src;
import src.Action.ACTION;

import java.util.Queue;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MyAI extends AI {
	// ########################## INSTRUCTIONS ##########################
	// 1) The Minesweeper Shell will pass in the board size, number of mines
	// 	  and first move coordinates to your agent. Create any instance variables
	//    necessary to store these variables.
	//
	// 2) You MUST implement the getAction() method which has a single parameter,
	// 	  number. If your most recent move is an Action.UNCOVER action, this value will
	//	  be the number of the tile just uncovered. If your most recent move is
	//    not Action.UNCOVER, then the value will be -1.
	// 
	// 3) Feel free to implement any helper functions.
	//
	// ###################### END OF INSTURCTIONS #######################
	
	// This line is to remove compiler warnings related to using Java generics
	// if you decide to do so in your implementation.
	@SuppressWarnings("unchecked")
	
	// private variables
	private final int ROW_DIMENSION;
	private final int COL_DIMENSION;
	private final int TOTAL_MINES;
	// NEED TO MODIFY IF UNFLAG
	private boolean[][] visited; // check if the <x,y> has been visited
	// record the last Action, which contains ACTION, lastX and lastY
	private Queue<Action> toUncover;
	private Queue<Action> toFlag;
	private int lastX;
	private int lastY;
	private ACTION lastA;
	private int[][] board;

	/*
	 * x,y coordinates are 1-indexed. That is,
	 * 1 <= x <= colDimension & 1 <= y <= rowDimension
	 */
	public MyAI(int rowDimension, int colDimension, int totalMines, int startX, int startY) {
		// ################### Implement Constructor (required) ####################	
		this.ROW_DIMENSION = rowDimension;
		this.COL_DIMENSION = colDimension;
		this.board = new int[this.ROW_DIMENSION][this.COL_DIMENSION];
		this.visited = new boolean[this.ROW_DIMENSION][this.COL_DIMENSION];
		this.TOTAL_MINES = totalMines;
		this.toUncover = new LinkedList<Action>();
		this.toFlag = new LinkedList<Action>();
		// coordinates need to be translated 
		int[] translatedXY = this.translateToBoard(startX, startY);
		this.lastX = translatedXY[0]; 
		this.lastY = translatedXY[1];
		this.lastA = ACTION.UNCOVER;
			
		// initialize the board as uncovered region with value -2
		for (int i = 0; i < this.ROW_DIMENSION; i++) {
			for (int j = 0; j < this.COL_DIMENSION; j++) {
				board[i][j] = -2;
			}
		}	
	}
	
	// ################## Implement getAction(), (required) #####################
	public Action getAction(int number) {
		// System.out.println("myai action");
		
		// if use UNFLAG, change the operation
		board[lastX][lastY] = number;
		
		// explore the neighbors of (lastX, lastY) including itself
		for (int row = lastX - 1; row <= lastX + 1; row++) {
			for (int col = lastY - 1; col <= lastY + 1; col++) {
				if (isValid(row, col) && board[row][col] >= 0) {
					// push the neighbors with certain state (safe or mine)
					pushCandidates(row, col);	
				}
			}
		}
		
		/* check if toUncover & toFlag is empty
		 * as long as any of them is not empty, return a new action according to the queue type
		 * if both are empty, choose a random node and go to next action	
		 */

		Action nextAction;
		if (!toFlag.isEmpty()) { 
			nextAction = toFlag.poll(); 
			}
		else if (!toUncover.isEmpty()) { nextAction = toUncover.poll(); }
		else {
			// if toUncover and toFlag are both empty
			nextAction = this.getRandomAction();
		}	
		// before we send back the result to the world, we need to translate the coordinate into a world format
		lastX = nextAction.x;
		lastY = nextAction.y;
		lastA = nextAction.action;
		int[] worldXY = this.translateToWorld(lastX, lastY);
		return new Action(lastA, worldXY[0], worldXY[1]);
	}

	// ################### Helper Functions Go Here (optional) ##################
	// ...
	
	// push the potential candidates (all free neighbors or all mine neighbors of cell [row][col]) to the corresponding queue
	private void pushCandidates(int row, int col) {
		if (isAFN(row, col, board[row][col])) {
		    // put all the neighbors to the toUncover queue
		    List<int[]> toUncoverList = getCoveredNeighbors(row,col);
		    for(int[] coordinate: toUncoverList){
		    	//add Uncover action to the queue
		    	// System.out.println("added new action uncover: ("+coordinate[0]+", "+coordinate[1]+")");  	
		    	this.toUncover.add(new Action(ACTION.UNCOVER,coordinate[0],coordinate[1]));	    	
		    }
		}
		else if (isAMN(row, col, board[row][col])) {
		    // put all the neighbors to the toFlag queue
			List<int[]> toFlagList = getCoveredNeighbors(row,col);
		    for(int[] coordinate:toFlagList){
		    	this.toFlag.add(new Action(ACTION.FLAG,coordinate[0],coordinate[1]));
		    }
	    }
	}
	
	// return if the [x,y] has all free neighbors
	private boolean isAFN(int x, int y, int number) {
		int[] countNeighborAndMine = countNeighborsAndMines(x,y);
		if (number - countNeighborAndMine[1] == 0) return true;	   
		return false;
	}
	
	// return if the [x,y] has all mine neighbors
	private boolean isAMN(int x, int y, int number) {
		int[] countNeighborAndMine = countNeighborsAndMines(x,y);
		if(countNeighborAndMine[0] == number - countNeighborAndMine[1]) return true;	 
		return false;
	}
	
	// return all the covered neighbors, possibly free neighbors or mines
	private List<int[]> getCoveredNeighbors(int x, int y) {
		List<int[]> neighbors = new ArrayList<int[]>();
		for (int row = x - 1; row <= x + 1; row++) {
			for (int col = y - 1; col <= y + 1; col++) {
				if (row == x && col == y) continue;
				if (this.isValid(row, col) && !visited[row][col] && board[row][col] == -2) {
					neighbors.add(new int[] {row, col});
					visited[row][col] = true;
				}
			}
		}
		return neighbors;
	}

	
	// return the number of [covered neighbors, uncovered mines]
	private int[] countNeighborsAndMines(int x, int y) {
		int coveredNeighbors = 0;
		int uncoveredMines = 0;
		for (int row = x - 1; row <= x + 1; row++) {
			for (int col = y - 1; col <= y + 1; col++) {
				if (row == x && col == y) continue; 
				if (this.isValid(row, col) ) {
					if (board[row][col] == -1) { uncoveredMines++; }
					else if (board[row][col] == -2) { coveredNeighbors++; }
				}
			}
		}
		return new int[] {coveredNeighbors, uncoveredMines};
	}
	
	// return a random uncover action -- NEED TO IMPROVE
	private Action getRandomAction() {
		int randX, randY;
		while (true) {
			randX = (int) (Math.random() * this.ROW_DIMENSION);
			randY = (int) (Math.random() * this.COL_DIMENSION);
			if (board[randX][randY] == -2) break;
		}	
		return new Action(ACTION.UNCOVER, randX, randY);
	}
	
	// return if the coordinate (x, y) is valid
	private boolean isValid(int x, int y) {
		if (x < 0 || y < 0 || x >= this.ROW_DIMENSION || y >= this.COL_DIMENSION) return false;
		return true;
	}
	
	//
	private int[] translateToBoard(int x, int y) {
		/*	Inputs:
		 * 		x - x coordinate of board
		 * 		y - y coordinate of board 
		 * 
		 * 	Outputs:
		 * 		TwoTuple, t, where:
		 * 			t.x is the corresponding row index in the board
		 * 			t.y is the corresponding col index in the board
		 * 
		 * 	Description:
		 * 	Translates the given (x,y) coordinate to a (row, col) tuple used
		 * 	for indexing into the board instance variable. (See Note below).
		 * 
		 * 	Notes:
		 * 	The internal representation of a board is a 2-d array, 0-indexed 
		 * 	array. However, users, specify locations on the board using 1-indexed
		 * 	(x,y) Cartesian coordinates. 
		 * 	Hence, to access the proper indicies into the board array, a translation 
		 * 	must be performed first.
		 */
		return new int[] {this.ROW_DIMENSION - y , x - 1};
	}
	
	private int[] translateToWorld(int x, int y) {
		return new int[] {y + 1, this.ROW_DIMENSION - x};
	}
}





