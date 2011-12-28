package mahergamal.cryptography.impl;

import mahergamal.cryptography.Checksum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of Peter Hans Luhn's checksum algorithm.
 * 
 * @see http://en.wikipedia.org/wiki/Luhn_algorithm
 * @author Maher Gamal
 */
public class LuhnChecksum implements Checksum {
    
    private Logger log = LoggerFactory.getLogger(LuhnChecksum.class);
    private String digits;
    private String doubledDigits;
    private int digitsSummation;
    private Boolean valid = null;
    
    public LuhnChecksum(String text) {
        log.debug("Checking input sequence : {}", text);
        this.digits = stripNonDigits(text);
    }
    
    @Override
    public boolean isValid() {
        if (valid == null) {
            doubleTheSecondDigits();
            sumTheDoubledDigits();
            valid = isThereAZeroRemainderFromTheDivisionByTen();
            log.debug("Validity : {}", valid.toString());
        }
        return valid;
    }
    
    private String stripNonDigits(String input) {
        return input.replaceAll("[^\\d]", "");
    }
    
    private void doubleTheSecondDigits() {
        StringBuilder sb = new StringBuilder();
        int lastIndex = digits.length() - 1;
        
        for (int i = lastIndex; i >= 0; i--) {
            char currentCharacter = digits.charAt(i);
            int currentNumber = Character.getNumericValue(currentCharacter);
            int backwardIndex = lastIndex - i;
            if (backwardIndex % 2 == 0) {
                sb.insert(0, currentCharacter);
            } else {
                sb.insert(0, currentNumber * 2);
            }
        }
        this.doubledDigits = sb.toString();
    }
    
    private void sumTheDoubledDigits() {
        for (int i = 0; i < doubledDigits.length(); i++) {
            this.digitsSummation += Character.getNumericValue(doubledDigits.charAt(i));
        }
        log.debug("Digits sum : {}", this.digitsSummation);
    }
    
    private boolean isThereAZeroRemainderFromTheDivisionByTen() {
        return this.digitsSummation % 10 == 0;
    }
}
