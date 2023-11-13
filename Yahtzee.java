/*
 * File: Yahtzee.java
 * ------------------
 * This program will eventually play the Yahtzee game.
 */

import acm.io.*;
import acm.program.*;
import acm.util.*;

public class Yahtzee extends GraphicsProgram implements YahtzeeConstants {
	
/* inits part of the yahtzee program */
	public static void main(String[] args) {
		new Yahtzee().start(args);
	}
	
/* init method */
	public void init() {
		dialog = getDialog();
		initNPlayers();
		initPlayerNames();
		display = new YahtzeeDisplay(getGCanvas(), playerNames);
		initTotalScores();
		initCategoriesUsed();
	}
		
/* run method */
	public void run() {
		playGame();
	}
	
/* asks number of players */
	private void initNPlayers() {
		nPlayers = 0;
		while(nPlayers == 0) {
			nPlayers = dialog.readInt("Enter number of players from 1 to " + MAX_PLAYERS);
			if(nPlayers < 1 || nPlayers > MAX_PLAYERS) {
				nPlayers = 0;
			}
		}
	}
	
/* asks for player names */
	private void initPlayerNames() {
		playerNames = new String[nPlayers];
		for (int i = 0; i < nPlayers; i++) {
			while(playerNames[i] == null) {
				playerNames[i] = dialog.readLine("Enter name for player " + (i + 1));
				if(playerNames[i].equals("")) {
					playerNames[i] = null;
				}
			}
		}
	}
	
/* inits total scores to equal 0 */
	private void initTotalScores() {
		totalScores = new int[3][nPlayers];
		for(int i = 0; i < totalScores.length; i++) {
			for(int j = 0; j < totalScores[0].length; j++) {
				totalScores[i][j] = 0;
			}
		}
		updateTotalScores();
	}
	
/* inits a string that keeps track of characters used */
	private void initCategoriesUsed() {
		categoriesUsed = new String[nPlayers];
		for(int i = 0; i < categoriesUsed.length; i++) {
			categoriesUsed[i] = "";
		}
	}

/* method where game is played */
	private void playGame() {
		for(int i = 0; i < TOTAL - 4; i++) {
			for(player = 1; player <= nPlayers; player++) {
				display.printMessage(playerNames[player - 1] + "'s turn! Click \"Roll Dice\" button to roll the dice.");
				nextTurn();
			}
		}
		updateUpperBonus();
		calculateWinner();
	}
	
/* calls the next turn of each player */
	private void nextTurn() {
		rollDice();
		rollAgain();
		updateScore();
		updateTotalScores();
	}
	
/* allows player to roll dice */
	private void rollDice() {
		display.waitForPlayerToClickRoll(player);
		dice = new int[N_DICE];
		setDice();
		display.displayDice(dice);
	}
	
/* randomizes values of the dice */
	private void setDice() {
		for(int i = 0; i < N_DICE; i++) {
			dice[i] = rgen.nextInt(1, 6);
		}
	}
	
/* allows player to roll dice twice more */
	private void rollAgain() {
		for(int i = 0; i < 2; i++) {
			display.printMessage("Select the dice you wish to re-roll and click \"Roll Again\"");
			display.waitForPlayerToSelectDice();
			for(int j = 0; j < N_DICE; j++) {
				if(display.isDieSelected(j)) {
					dice[j] = rgen.nextInt(1, 6);
				}
			}
			display.displayDice(dice);
		}
	}
	
/* updates score of category picked */
	private void updateScore() {
		display.printMessage("Select a category for this roll.");
		pickCategory();
		display.updateScorecard(category, player, score);
	}
	
/* has player pick category */
	private void pickCategory() {
		category = display.waitForPlayerToSelectCategory();
		checkCategoryAndScore();
	}
	
/* checks if category chosen is appropriate then gives the appropriate score */
	private void checkCategoryAndScore() {
		index = categoriesUsed[player - 1].indexOf(" " + category + " ");
		if(index != -1) {
			display.printMessage("This category has already been used.  Select another category.");
			pickCategory();
		} else if(category >= ONES && category <= SIXES) {
			upperCategoryScoring();
			categoriesUsed[player - 1] += " " + category + " ";
		} else if(category >= THREE_OF_A_KIND && category <= CHANCE) {
			lowerCategoryScoring();
			categoriesUsed[player - 1] += " " + category + " ";
		}
	}
	
/* calculation for upper category scores */
	private void upperCategoryScoring() {
		score = 0;
		for(int i = 0; i < N_DICE; i++) {
			if(dice[i] == category) {
				score += dice[i];
			}
		}
		addScores(0);
	}
	
/* calculation for lower category scores */
	private void lowerCategoryScoring() {
		score = 0;
		if(YahtzeeMagicStub.checkCategory(dice, category)) {
			checkCategory();
		}
		addScores(1);
	}
	
/* scores chosen category appropriately */
	private void checkCategory() {
		if(category == THREE_OF_A_KIND || category == FOUR_OF_A_KIND) {
			addAllValues();
		} else if(category == FULL_HOUSE) {
			score = 25;
		} else if(category == SMALL_STRAIGHT) {
			score = 30;
		} else if(category == LARGE_STRAIGHT) {
			score = 40;
		} else if(category == YAHTZEE) {
			score = 50;
		} else {
			addAllValues();
		}
	}
	
/* adds all dice values together */
	private void addAllValues() {
		for(int i = 0; i < N_DICE; i++) {
			score += dice[i];
		}
	}
	
/* adds up upper and lower scores and adds both into total scores */
	private void addScores(int upperOrLower) {
		totalScores[upperOrLower][player - 1] += score;
		totalScores[2][player - 1] = totalScores[0][player - 1] + totalScores[1][player - 1];
	}
	
/* updates all total scores onto screen */
	private void updateTotalScores() {
		for(int i = 0; i < nPlayers; i++) {
			display.updateScorecard(UPPER_SCORE, i + 1, totalScores[0][i]);
			display.updateScorecard(LOWER_SCORE, i + 1, totalScores[1][i]);
			display.updateScorecard(TOTAL, i + 1, totalScores[2][i]);
		}
	}
	
/* when game is finished, upper bonus is updated */
	private void updateUpperBonus() {
		for(int i = 0; i < nPlayers; i++) {
			if(totalScores[0][i] >= 63) {
				totalScores[2][i] += 35;
				display.updateScorecard(UPPER_BONUS, i + 1, 35);
				display.updateScorecard(TOTAL, i + 1, totalScores[2][i]);
			}
		}
	}
	
/* calculates the winner then prints name and score to the screen */
	private void calculateWinner() {
		int winningScore = 0;
		for(int i = 0; i < nPlayers; i++) {
			if(winningScore < totalScores[2][i]) {
				winningScore = totalScores[2][i];
			}
		}
		for(int i = 0; i < nPlayers; i++) {
			if(winningScore == totalScores[2][i]) {
				display.printMessage("Congratulations, " + playerNames[i] + ", you're the winner with a total score of " + winningScore);
			}
		}
	}

/* private instance variables */
	private IODialog dialog;
	private int nPlayers, category, score, player, index;
	private int[] dice;
	private int[][] totalScores;
	private String[] playerNames, categoriesUsed;
	private YahtzeeDisplay display;
	private RandomGenerator rgen = new RandomGenerator();

}
