public interface BoardAPI {

    int[][] get();

    int getNumCheckers(int playerId, int pip);

    Plays getPossiblePlays(Player player, Dice dice);

    boolean lastCheckerInInnerBoard(Player player);

    boolean lastCheckerInOpponentsInnerBoard(Player player);

    boolean allCheckersOff(Player player);

    boolean hasCheckerOff(Player player);

    Plays getPossiblePlays(int playerId, Dice dice);

    boolean lastCheckerInInnerBoard(int playerId);

    boolean lastCheckerInOpponentsInnerBoard(int playerId);

    boolean allCheckersOff(int playerId);

    boolean hasCheckerOff(int playerId);
}
