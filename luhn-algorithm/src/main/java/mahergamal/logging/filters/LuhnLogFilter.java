package mahergamal.logging.filters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Written as part of Square's coding challenge.
 * http://corner.squareup.com/2011/11/luhny-bin.html
 * 
 * @author Maher Gamal
 */
public class LuhnLogFilter {
    
    private static Logger log = LoggerFactory.getLogger(LuhnLogFilter.class);
    private Character MASK_CHARACTER = 'X';
    private String rawInput;
    private char[] maskedOutputCharacters;
    private String doubledDigits;
    private Integer doubledDigitsSummation = 0;
    private Boolean result = null;
    
    public LuhnLogFilter(String rawInput) {
        log.debug("Initializing {} with input : {}", LuhnLogFilter.class.getSimpleName(), rawInput);
        this.rawInput = rawInput;
        this.maskedOutputCharacters = new char[rawInput.length()];
    }
    
    public boolean isValid() {
        if (result == null) {
            maskCharactersAccordinglyAndDoubleTheSecondDigits();
            sumTheDoubledDigits();
            result = isThereAZeroRemainderFromTheDivisionByTen();
            log.debug("==> {}", result);
        }
        return result;
    }
    
    private void maskCharactersAccordinglyAndDoubleTheSecondDigits() {
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
                maskedOutputCharacters[i] = MASK_CHARACTER;
            } else {
                maskedOutputCharacters[i] = currentCharacter;
            }
            
        }
        this.doubledDigits = sb.toString();
    }
    
    private void sumTheDoubledDigits() {
        for (int i = 0; i < doubledDigits.length(); i++) {
            this.doubledDigitsSummation += Character.getNumericValue(doubledDigits.charAt(i));
        }
        log.debug("Input {} has double digits summation = {}", this.rawInput, this.doubledDigitsSummation);
    }
    
    private boolean isThereAZeroRemainderFromTheDivisionByTen() {
        return this.doubledDigitsSummation % 10 == 0;
    }
    
    public String getMaskedOutput() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maskedOutputCharacters.length; i++) {
            sb.append(maskedOutputCharacters[i]);
        }
        return sb.toString();
    }
    
    public String getRawInput() {
        return rawInput;
    }
    
    public String getFilteredOutput() {
        if (this.isValid()) {
            return this.getMaskedOutput();
        } else {
            return this.getRawInput();
        }
    }
    
    public static void main(String[] args) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String line = null;
        while ((line = reader.readLine()) != null) {
            
            long timestamp = System.nanoTime();
            LuhnLogFilter filter = new LuhnLogFilter(line);
            String filteredOutput = filter.getFilteredOutput();
            System.out.print(filteredOutput);
            log.debug("Output {}", filteredOutput);
            log.debug("Time-elapsed : {}", (System.nanoTime() - timestamp) / 1000000);
        }
        log.debug("No remaining inputs, exiting !");
    }
    
}
