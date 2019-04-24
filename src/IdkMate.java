import java.util.HashMap;
import java.util.Map;

public class IdkMate implements BotAPI {
	
	/*
	 * indicates progression of the game
	 */
	private enum Stage {
		contact,
		opposedbearoff,
		unopposedbearoff,
		unopposedprebearoff;
	}

    // The public API of Bot must not change
    // This is ONLY class that you can edit in the program
    // Rename Bot to the name of your team. Use camel case.
    // Bot may not alter the state of the game objects
    // It may only inspect the state of the board and the player objects

    private PlayerAPI me, opponent;
    private BoardAPI board;
    private CubeAPI cube;
    private MatchAPI match;
    private InfoPanelAPI info;

    IdkMate(PlayerAPI me, PlayerAPI opponent, BoardAPI board, CubeAPI cube, MatchAPI match, InfoPanelAPI info) {
        this.me = me;
        this.opponent = opponent;
        this.board = board;
        this.cube = cube;
        this.match = match;
        this.info = info;
    }

    public String getName() {
        return "IdkMate"; // must match the class name
    }
    
    private class Duo {
    	int id;
    	Play play;
    	
    	Duo (int id, Play play) {
    		this.id = id;
    		this.play = play;
    	}
    }

    public String getCommand(Plays possiblePlays) {
    	
    	String command = "1";
    	int counter = 0;
    	
    	this.getWinningProbability(me);

    	Map <Duo, Double> thisMap = new HashMap<>();
    	for (Play play : possiblePlays) {
    		thisMap.put(new Duo(++counter,play), getWeight(play));
    	}
    	
    	//returns the Duo associated with the maximum values of the map
    	Duo res = thisMap.entrySet().stream().max((e1, e2) -> e1.getValue() > e2.getValue() ? 1 : -1).get().getKey();
    	command = Integer.toString(res.id);
    	System.out.println(thisMap.get(res));
    	
    	String dDecision = getDoubleDecision();
    	
    	if (dDecision == "double") {
    		if (match.canDouble((Player) me))
    			command = "double";
    	}
    	
    	return command;
    
    	
    }
    
    private double getWeight(Play play) {
    	double weight = 0.0;
    	
    	//Hit Multiplier
    	double hitMult = 0.0;
    	if (doesHit(play) == 1)
    		hitMult = 0.05;
    	else if (doesHit(play) == 2)
    		hitMult = 0.1;
    	else if (doesHit(play) > 2)
    		hitMult = 0.15;
    		
    	
    	double blockMult = 0.0;
    	if (makesBlock(play))
    		blockMult = 0.1;
    	
    	double anchorMult = 0.0;
    	if (removesAnchor(play))
    		anchorMult = -0.1;
    	
    	double escapeMult = 0.0;
    	if (escapeTrappedChecker(play))
    		escapeMult = 0.1;
    	
    	double largeStackMult = 0.0;
    	if (flagLargeMoves(play))
    		largeStackMult = -0.1;
    	
    	double movesHBMult = 0.0;
    	if (movesHBCheckers(play))
    		movesHBMult = -0.1;
    	
    	double blotMult = 0.0;
    	if (makesBlot(play))
    		blotMult = -0.125;
    	
    	//less important features are exponentiated
    	
    	if (getCurrentGameStatus(me).equals(Stage.unopposedbearoff)) {
    		weight = (numOfBearOffMoves(play)/10);   
    	} else if (getCurrentGameStatus(me).equals(Stage.opposedbearoff)) {
    		weight = (numOfBearOffMoves(play)/10)
    				+ hitMult
    				+ movesHBMult
    				+ Math.pow(blotMult,2)
    				+ Math.pow(largeStackMult,2);
    	} else {    	
	    	weight = (getDistanceTravelled(play) / 24) 
	    			+ hitMult 
	    			+ (blotMult*1.5)
	    			+ Math.pow(anchorMult,2) 
	    			+ blockMult 
	    			+ escapeMult
	    			+ largeStackMult
	    			+ Math.pow(movesHBMult,3);    	
    	}
    	
    	return weight;
    }
    
    /*
     * Bot movement features
     */
    
    //returns the number of hits a given move will perform
    private int doesHit(Play play) {
    	int numOfHits = 0;
    	for (Move move : play.moves) {
    		if (move.isHit())
    			numOfHits++;
    	}
    	
    	return numOfHits;
    }
    
    //returns true if a give move will place 2 checkers on an empty pip
    private boolean makesBlock (Play play) {
    	Map<Integer, Integer> map = new HashMap<>();
    	for (Move move : play.moves) {
    		if (!map.containsKey(move.getToPip())) 
    			map.put(move.getToPip(), 1);
    		else
    			map.replace(move.getToPip(), map.get(move.getToPip()) + 1);
    	}
    	
    	if (map.containsValue(2))
    		return true;
    	else
    		return false;    		
    }
    
    //returns true if a given move will remove an "anchor" (2 checkers in opponents home)
    private boolean removesAnchor(Play play) {
    	Map<Integer, Integer> map = new HashMap<>();
    	for (Move move : play.moves) {
    		if (move.getFromPip() <= 24 && move.getFromPip() >= 19) {
	    		if (!map.containsKey(move.getFromPip())) 
	    			map.put(move.getFromPip(), 1);
	    		else
	    			map.replace(move.getFromPip(), map.get(move.getFromPip()) + 1);
    		}
    	}
    	
    	if (map.containsValue(2))
    		return true;
    	else
    		return false;
    }
    
    //returns the total pip difference for all moves of a given play
    private double getDistanceTravelled(Play play) {
    	int distance = 0;
    	for (Move move : play.moves) {
    		distance += move.getPipDifference();
    	}
    	
    	return distance;
    }
    
    //returns true if a give play will remove a "trapped" checker from the opponents Home board
    private boolean escapeTrappedChecker(Play play) {
    	if (!board.lastCheckerInOpponentsInnerBoard((Player) opponent) &&
    			board.lastCheckerInOpponentsInnerBoard((Player) me)) {
    		for (Move move : play.moves) {
    			if (move.getFromPip() <= 24 && move.getFromPip() >= 19) {
    				if (move.getToPip() < 19)
    					return true;
    			}    				
    		}
    	}
    	
    	return false;
    }
    
    //returns true if a move will stack a large number of checkers onto 1 pip
    private boolean flagLargeMoves (Play play) {
    	Map<Integer, Integer> map = new HashMap<>();
    	for (Move move : play.moves) {
    		if (!map.containsKey(move.getToPip()))
    			map.put(move.getToPip(), 1);
    		else
    			map.replace(move.getToPip(), map.get(move.getToPip()) + 1);
    	}
    	
    	if (map.containsValue(5))
    		return true;
    	else if (map.containsValue(6))
    		return true;
    	else if (map.containsValue(7))
    		return true;
    	else if (map.containsValue(8))
    		return true;
    	else if (map.containsValue(9))
    		return true;
    	
    	return false;
    }
    
    //returns true if a given move only moves checkers forward in the players homeboard without a hit
    private boolean movesHBCheckers (Play play) {
    	for (Move move : play.moves) {
    		if (move.getFromPip() <= 6) {
    			if (move.getToPip() <= 5)
    				if (!move.isHit())
    					return true;
    		}
    	}
    	
    	return false;
    }    
    
    //Returns the number of checkers moved to position 0 (bear off) for a given play
    private double numOfBearOffMoves (Play play) {
    	int offCounter = 0;
    	for (Move move : play.moves) {
    		if (move.getToPip() == 0)
    			offCounter++;
    	}
    	
    	return offCounter;
    }
    
    //returns true if a given play creates a blot.
    private boolean makesBlot(Play play) {
    	Map<Integer,Integer> map = new HashMap<>();
    	for (Move move : play.moves) {
    		if (!map.containsKey(move.getToPip()))
    			map.put(move.getToPip(), 1);
    		else
    			map.replace(move.getToPip(), map.get(move.getToPip()) + 1);
    	}
    	
    	if (map.containsValue(1))
    		return true;
    	else
    		return false;
    }
    
    /*
     * Bot doubling features
     */

    public String getDoubleDecision() {
    	
    	String decision = "n"; 
    	
    	
    	/**
    	 * Cube not owned
    	 */
    	if (!cube.isOwned()) {
    		/* 1 point off, no double  */
    		if (me.getScore() + 1 == match.getLength())
    			decision = "n";
    		/* 2 points off: */
    		else if (ifTwoOffWinning()) {
    			if (getWinningProbability(me) < 0.50)
    				decision = "n";    			
    			else if (getWinningProbability(me) < 0.75 && getWinningProbability(me) >= 0.47)
    				decision = "double";
    			else if (getWinningProbability(me) >= 0.75)
    				decision = "double";
    		}
    		
    		/* Repeated below code in the next 2 big logic statements but it will be changed. */
    		
    		/* winning prob is good and gammon chance is good, no double */
    		else if (getWinningProbability(me) > 0.75 && getGammonChance(me) > 0.8)
    			decision = "n";
    		/* winning prob is good and no gammon, double. */
    		else if (getWinningProbability(me) > 0.75 && getGammonChance(me) < 0.2)
    			decision = "double";
			/* winning prob is good and gammon chance is good, no double */
    		else if (getWinningProbability(me) > 0.66 && getGammonChance(me) >= 0.75)
    			decision = "n";
    		/* winning prob is good and no gammon, double. */
    		else if (getWinningProbability(me) > 0.75 && getGammonChance(me) <= 0.66)
    			decision = "double";
    		else if (getWinningProbability(me) > 0.85)
    			decision = "double";
    	} 
    	
    	/**
    	 * Cube is owned
    	 */
    	else {
    		/**
    		 * Responding to a proposal
    		 */
    		if (cube.getOwnerId() != me.getId()) {
    			/* automatic lose, accept double. */
    			if (opponent.getScore() + 1 == match.getLength())
    				decision = "y";
    			/* 2 points off: */ 
    			else if (ifTwoOffWinning()) {
        			if (getWinningProbability(opponent) < 0.50)
        				decision = "y";
        			else if (getWinningProbability(me) < 0.75 && getWinningProbability(me) >= 0.50)
        				decision = "y";
        			else if (getWinningProbability(opponent) >= 0.75)
        				decision = "n";
        		}
    			
    			/* winning prob is good and gammon chance is good, no double */
        		else if (getWinningProbability(opponent) > 0.75 && getGammonChance(opponent) > 0.8)
        			decision = "n";
        		/* winning prob is good and no gammon, double. */
        		else if (getWinningProbability(opponent) > 0.75 && getGammonChance(opponent) < 0.2)
        			decision = "n";
    			/* winning prob is good and gammon chance is good, no double */
        		else if (getWinningProbability(opponent) > 0.66 && getGammonChance(opponent) >= 0.75)
        			decision = "y";
        		/* winning prob is good and no gammon, double. */
        		else if (getWinningProbability(opponent) > 0 && getGammonChance(opponent) <= 0.66)
        			decision = "y";
    		}
    		/**
    		 * Proposing a double 
    		 */
    		else {
    			/* 1 point off, post crawford: double */
    			if (me.getScore() + 1 == match.getLength())
    				decision = "double";
    			/* 2 points off: */
    			else if (ifTwoOffWinning()) {
        			if (getWinningProbability(me) < 0.50)
        				decision = "n";
        			else if (getWinningProbability(me) < 0.75 && getWinningProbability(me) >= 0.50)
        				decision = "double";
        			else if (getWinningProbability(me) >= 0.75)
        				decision = "double";
        		}
    			
    			/* winning prob is good and gammon chance is good, no double */
        		else if (getWinningProbability(me) > 0.75 && getGammonChance(me) > 0.8)
        			decision = "n";
        		/* winning prob is good and no gammon, double. */
        		else if (getWinningProbability(me) > 0.75 && getGammonChance(me) < 0.2)
        			decision = "double";
    			/* winning prob is good and gammon chance is good, no double */
        		else if (getWinningProbability(me) > 0.66 && getGammonChance(me) >= 0.75)
        			decision = "double";
        		/* winning prob is good and no gammon, double. */
        		else if (getWinningProbability(me) > 0 && getGammonChance(me) <= 0.66)
        			decision = "n";
    		}
    	}
        return decision;
    }
    
    
    
    //Based on the status of p's last checker compared to the opponents last checker,
    //returns the stage of the game from Stage enum.
    private Stage getCurrentGameStatus(PlayerAPI p) {
    	Stage returnThis = Stage.contact;
    	Player thisPlayer = (Player) p;
    	Player otherPlayer = (Player) ((thisPlayer.getId() == 0) ? opponent : me );
    	
    	if (board.lastCheckerInInnerBoard(thisPlayer) && board.lastCheckerInInnerBoard(otherPlayer)) 
    		returnThis = Stage.unopposedbearoff;
    	else if (board.lastCheckerInInnerBoard(thisPlayer) && board.lastCheckerInOpponentsInnerBoard(otherPlayer))
    		returnThis = Stage.opposedbearoff;
    	else if (board.lastCheckerInInnerBoard(otherPlayer) && !(board.lastCheckerInInnerBoard(thisPlayer)))
    		returnThis = Stage.unopposedprebearoff;
    	
    	return returnThis;   	
    }
    
    /* Based on certain aspects of the board, this method calculates the circumstancial 
     * probability of winning for a given player
     */
    private double getWinningProbability(PlayerAPI thisPlayer) {
    	double probability = 0.0;
    	
    	PlayerAPI otherPlayer = ((thisPlayer.getId() == 0)? opponent : me);
    	
    	double pcDiff = getPipCount(thisPlayer) - getPipCount(otherPlayer);
    	double hbCheckersDiff = getNumHBCheckers(otherPlayer) - getNumHBCheckers(thisPlayer);
    	double numBarredOpp = this.getNumOppBarredCheckers(thisPlayer);
    	double numBarredMe = board.getNumCheckers(thisPlayer.getId(), 25);
    	double numBearedOffDiff = this.getBearedOffDiff(thisPlayer);    
    	
    	int handicap = getHandicap(thisPlayer);
    	double handicapMod = 0;
    	if (handicap == 1)
    		handicapMod = -0.2;
    	else if (handicap == 2)
    		handicapMod = -0.1;   	
    	
    	probability = Math.abs((1 - ( getPipCount(thisPlayer) / 167 )  ) 
				- ((pcDiff + 0.001) / (167 * 1.5)))
    			+ ((Math.pow(hbCheckersDiff,2) + 0.001) / 100)
    			+ handicapMod
    			+ (((Math.pow(numBearedOffDiff,2)) + 0.001) / 100)
    			- (((Math.pow(numBarredMe, 2)) + 0.001) / 100)
    			+ (((Math.pow(numBarredOpp, 1.5)) + 0.001 ) / 100)
    			;
    	
    	if (probability >= 1)
    		probability = 0.98;
    	
    	return probability; 
    }
    
    /*
     * Simple method which estimates the probability of a given player to
     * win by a gammon.
     */
    private double getGammonChance(PlayerAPI thisPlayer) {
    	double chance = 0;
    	
    	PlayerAPI otherPlayer = (thisPlayer.getId() == 0) ? opponent : me;
    	final double PER_CHECKER = 0.02;
    	chance = board.getNumCheckers(thisPlayer.getId(), 0) * PER_CHECKER;    	
    	
    	if (getNumHBCheckers(thisPlayer) == 15) {
    		if (board.getNumCheckers(otherPlayer.getId(), 25) == 1)
    			chance += 0.05;
    		else if (board.getNumCheckers(otherPlayer.getId(), 25) == 2)
    			chance += 0.40;
    		else if (board.getNumCheckers(otherPlayer.getId(), 25) == 3)
    			chance += 0.75;
    	}
    	
    	return chance;
    }
      
    
    //num of checkers on pip * pip number 
    private double getPipCount(PlayerAPI p) {
    	double thisPlayerCount = 0;
    	
    	for (int i = 1; i < 25; i++)
    		thisPlayerCount += board.getNumCheckers(p.getId(), i) * i;
    	
    	return thisPlayerCount;
    }    
    
    
    //number of homeboard checkers + position 7 (imporant)
    private double getNumHBCheckers(PlayerAPI thisPlayer) {
    	int number = 0;
    	for (int i = 1; i <= 7; i++)
    		number += board.getNumCheckers(thisPlayer.getId(), i);
    	return number;
    }
    
    //weighed sum of the homeboard checkers + pos 7. The higher the number, the more checkers there
    //are on important positions 
    private double getWeighedNumHBCheckers (PlayerAPI thisPlayer) {
    	double number = 0;
    	double multiplier = 1.0;
    	for (int i = 1; i <= 6; i++) {
    		if (i == 2) multiplier = 1.2;
    		else if (i == 3) multiplier = 1.3;
    		else if (i == 4) multiplier = 1.4; 
    		else if (i == 5) multiplier = 1.6;
    		else if (i == 6) multiplier = 1.7;
    		else if (i == 7) multiplier = 1.5;
    			
    		number += board.getNumCheckers(thisPlayer.getId(), i) * multiplier;
    	}
    	
    	return number;
    }
    
    /*
     * Returns the number of checker the opposite player has on the bar
     */
    private double getNumOppBarredCheckers(PlayerAPI thisPlayer) {
    	PlayerAPI otherPlayer = (thisPlayer.getId() == 0) ? opponent : me;
    	return board.getNumCheckers(otherPlayer.getId(), 25);
    }
    
    /*
     * Returns the difference between the number of beared of checkers between
     * the 2 players
     */
    private double getBearedOffDiff (PlayerAPI thisPlayer) {
    	PlayerAPI otherPlayer = (thisPlayer.getId() == 0) ? opponent : me;
    	return (board.getNumCheckers(otherPlayer.getId(), 0)) - (board.getNumCheckers(thisPlayer.getId(), 0));

    }
    
    /*
     * Returns true if both of the current players are 2 points away from winning the match
     */
    private boolean ifTwoOffWinning() {
    	return ((me.getScore() + 2) == match.getLength() && (opponent.getScore() + 2) == match.getLength());
    }
    
    
    /*
     * utility function which makes winning probability less biased 
     * towards the end stages of the game
     */
    private int getHandicap(PlayerAPI thisPlayer) {
    	PlayerAPI otherPlayer = (thisPlayer.getId() == 0) ? opponent : me;
    	int handicap = 0;
    	
    	if (board.lastCheckerInInnerBoard((Player) otherPlayer) &&
    			!board.lastCheckerInInnerBoard((Player) thisPlayer))
    		handicap = 2;
    	
    	if (board.lastCheckerInInnerBoard((Player) otherPlayer) &&
    			board.lastCheckerInOpponentsInnerBoard((Player) thisPlayer))
    		handicap = 1;
    	
    	return handicap;
    }
}


