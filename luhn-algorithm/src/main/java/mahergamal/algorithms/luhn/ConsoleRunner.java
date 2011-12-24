package mahergamal.algorithms.luhn;

public class ConsoleRunner {
    
    public static void main(String[] args) {
        if (args.length == 1) {
            LuhnChecker checker = new LuhnChecker(args[0]);
            if (checker.isValid()) {
                System.out.println(checker.getMaskedOutput());
            } else {
                System.out.println(checker.getRawInput());
            }
        }
    }
}
