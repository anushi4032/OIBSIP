import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Anushi_Task2 {

    // Configuration rules for each difficulty tier
    static class GameDifficulty {
        String levelName;
        int lowerBound, upperBound, totalChances;
        
        GameDifficulty(String levelName, int lowerBound, int upperBound, int totalChances) {
            this.levelName = levelName;
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
            this.totalChances = totalChances;
        }
    }

    // Tracks performance summary of individual sessions
    static class FirstRound {
        int sequenceNum;
        int turnsTaken;
        boolean isVictorious;
        
        FirstRound(int sequenceNum, int turnsTaken, boolean isVictorious) {
            this.sequenceNum = sequenceNum;
            this.turnsTaken = turnsTaken;
            this.isVictorious = isVictorious;
        }
    }

    private static final Scanner userInput = new Scanner(System.in);
    private static final Random dynamicRandom = new Random();
    private static final List<FirstRound> gameLog = new ArrayList<>();

    public static void main(String[] args) {
       
        System.out.println("  Welcome to Guess The Number!   ");
    
        int currentMatch = 1;
        boolean keepPlaying = true;

        while (keepPlaying) {
            GameDifficulty config = selectDifficultySetting();
            FirstRound matchSummary = executeMatch(currentMatch, config);
            gameLog.add(matchSummary);
            currentMatch++;

            displayPerformanceLog();
            keepPlaying = verifyUserResponse("Would you like to play another match? (y/n): ");
        }

        System.out.println("\nThank you for playing! Final Performance Dashboard:");
        displayPerformanceLog();
        userInput.close();
    }

    // Prompts user to choose computational boundaries
    private static GameDifficulty selectDifficultySetting() {
        System.out.println("\nSelect a Game Mode:");
        System.out.println("  1) Easy   (Range: 1-50,  10 chances)");
        System.out.println("  2) Medium (Range: 1-100, 7 chances)");
        System.out.println("  3) Hard   (Range: 1-200, 5 chances)");

        while (true) {
            System.out.print("Select choice (1-3): ");
            String selection = userInput.nextLine().trim();
            switch (selection) {
                case "1":
                    return new GameDifficulty("Easy", 1, 50, 10);
                case "2":
                    return new GameDifficulty("Medium", 1, 100, 7);
                case "3":
                    return new GameDifficulty("Hard", 1, 200, 5);
                default:
                    System.out.println("Invalid input. Please input option 1, 2, or 3.");
            }
        }
    }

    // Handles core mechanics of an active session
    private static FirstRound executeMatch(int matchId, GameDifficulty tier) {
        int secretKey = dynamicRandom.nextInt(tier.upperBound - tier.lowerBound + 1) + tier.lowerBound;
        int currentTurn = 0;
        boolean matchWon = false;

        System.out.println("\n--- Match " + matchId + " (" + tier.levelName + ") ---");
        System.out.println("Picking a hidden target between " + tier.lowerBound +
                " and " + tier.upperBound + ". Allowed chances: " + tier.totalChances);

        while (currentTurn < tier.totalChances) {
            int chancesLeft = tier.totalChances - currentTurn;
            System.out.print("Chance " + (currentTurn + 1) + " of " + tier.totalChances +
                    " (Remaining: " + chancesLeft + ") - Input your guess: ");

            int proposedGuess;
            try {
                proposedGuess = Integer.parseInt(userInput.nextLine().trim());
            } catch (NumberFormatException exc) {
                System.out.println("Invalid number configuration. Please try again.");
                continue;
            }

            currentTurn++;

            if (proposedGuess < tier.lowerBound || proposedGuess > tier.upperBound) {
                System.out.println("Out of bounds! Guess within " + tier.lowerBound +
                        "-" + tier.upperBound + ". (This chance is consumed.)");
                if (currentTurn >= tier.totalChances) break;
                continue;
            }

            if (proposedGuess == secretKey) {
                System.out.println("Spot on! You successfully cracked it in " + currentTurn + " attempts.");
                matchWon = true;
                break;
            } else if (proposedGuess < secretKey) {
                System.out.println("Too Low!");
            } else {
                System.out.println("Too High!");
            }
        }

        if (!matchWon) {
            System.out.println("Chances exhausted! The targeted number was: " + secretKey);
        }

        return new FirstRound(matchId, currentTurn, matchWon);
    }

    // Aggregates session telemetry outputs
    private static void displayPerformanceLog() {
        System.out.println("\n----- Performance History Dashboard -----");
        for (FirstRound logEntry : gameLog) {
            String statusReport = logEntry.isVictorious ? "resolved inside " + logEntry.turnsTaken + " turns"
                                                        : "failed after utilizing all " + logEntry.turnsTaken + " turns";
            System.out.println("Match ID " + logEntry.sequenceNum + " — " + statusReport);
        }
        long winCount = gameLog.stream().filter(entry -> entry.isVictorious).count();
        System.out.println("Total Matches: " + gameLog.size() + " | Wins: " + winCount +
                " | Defeats: " + (gameLog.size() - winCount));
        System.out.println("------------------------------------------");
    }

    // Evaluates binary confirmation loops
    private static boolean verifyUserResponse(String outputPrompt) {
        while (true) {
            System.out.print(outputPrompt);
            String confirmationText = userInput.nextLine().trim().toLowerCase();
            if (confirmationText.equals("y") || confirmationText.equals("yes")) return true;
            if (confirmationText.equals("n") || confirmationText.equals("no")) return false;
            System.out.println("Invalid preference. Provide 'y' or 'n'.");
        }
    }
}