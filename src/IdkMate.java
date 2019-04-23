import java.util.HashMap;
import java.util.Map;

public class IdkMate implements BotAPI {
	
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

    public String getCommand(Plays possiblePlays) {
    	
    	String command = "1";
    	
    	this.getWinningProbability(me);

    	
    	Map <Play, Integer> map = new HashMap<>();
    	for (Play play : possiblePlays) {
    		map.put(play, getWeight(play));
    	}
    	
    	String dDecision = getDoubleDecision();
    	
    	if (dDecision == "double") {
    		if (match.canDouble((Player) me))
    			command = "double";
    	}
    	
    	return command;
    
    	
    }

    public String getDoubleDecision() {
    	
    	String decision = "n"; 
    	
    	//double probability = getWinningProbability(me);
    	
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
    			System.out.println("dec6: " + decision);
    		}
    	}
    	System.out.println("dec0: " + decision);
        return decision;
    }
    
    private int getWeight(Play play) {
    	return 1;
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
    
    //to finish
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
    
    private double getNumOppBarredCheckers(PlayerAPI thisPlayer) {
    	PlayerAPI otherPlayer = (thisPlayer.getId() == 0) ? opponent : me;
    	return board.getNumCheckers(otherPlayer.getId(), 25);
    }
    
    private double getBearedOffDiff (PlayerAPI thisPlayer) {
    	PlayerAPI otherPlayer = (thisPlayer.getId() == 0) ? opponent : me;
    	return (board.getNumCheckers(otherPlayer.getId(), 0)) - (board.getNumCheckers(thisPlayer.getId(), 0));

    }
    
    private boolean ifTwoOffWinning() {
    	return ((me.getScore() + 2) == match.getLength() && (opponent.getScore() + 2) == match.getLength());
    }
    
    
    //utility function which makes winning probability less biased towards
    //end stages of the game
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


