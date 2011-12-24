package mahergamal.algorithms.luhn;

/**
 * Written as part of Square's coding challenge.
 * http://corner.squareup.com/2011/11/luhny-bin.html
 * 
 * @author Maher Gamal
 */
public class LuhnChecker {
    
    private Character MASK_CHARACTER = 'X';
    private String rawInput;
    private StringBuilder maskedOutput;
    private String doubledDigits;
    private Integer doubledDigitsSummation = 0;
    
    public LuhnChecker(String rawInput) {
        this.rawInput = rawInput;
        this.maskedOutput = new StringBuilder();
    }
    
    public boolean isValid() {
        maskTheNonNumbersAndDoubleTheSecondDigits();
        sumTheDoubledDigits();
        return isThereAZeroRemainderFromTheDivisionByTen();
    }
    
    private void maskTheNonNumbersAndDoubleTheSecondDigits() {
        StringBuilder sb = new StringBuilder();
        int lastIndex = rawInput.length() - 1;
        
        for (int i = lastIndex; i >= 0; i--) {
            char currentCharacter = rawInput.charAt(i);
            int currentNumber = Character.getNumericValue(currentCharacter);
            
            if (currentNumber >= 0 && currentNumber <= 9) {
                int backwardIndex = lastIndex - i;
                if (backwardIndex % 2 == 0) {
                    sb.insert(0, currentCharacter);
                } else {
                    sb.insert(0, currentNumber * 2);
                }
                this.maskedOutput.insert(0, MASK_CHARACTER);
            } else {
                this.maskedOutput.insert(0, ' ');
            }
            
        }
        this.doubledDigits = sb.toString();
    }
    
    private void sumTheDoubledDigits() {
        for (int i = 0; i < doubledDigits.length(); i++) {
            this.doubledDigitsSummation += Character.getNumericValue(doubledDigits.charAt(i));
        }
    }
    
    private boolean isThereAZeroRemainderFromTheDivisionByTen() {
        return this.doubledDigitsSummation % 10 == 0;
    }
    
    public String getMaskedOutput() {
        return this.maskedOutput.toString();
    }
    
    public String getRawInput() {
        return rawInput;
    }
}
