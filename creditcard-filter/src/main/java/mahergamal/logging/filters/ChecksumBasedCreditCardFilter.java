package mahergamal.logging.filters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mahergamal.cryptography.Checksum;
import mahergamal.cryptography.ChecksumFactory;
import mahergamal.cryptography.impl.LuhnChecksum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Written as part of Square's coding challenge.
 * http://corner.squareup.com/2011/11/luhny-bin.html
 * 
 * @author Maher Gamal
 */
public class ChecksumBasedCreditCardFilter implements Runnable {
    
    private static final String MASK_CHARACTER = "X";
    
    private static Logger log = LoggerFactory.getLogger(ChecksumBasedCreditCardFilter.class);
    
    private BufferedReader reader;
    private OutputStreamWriter writer;
    private Class<? extends Checksum> checksumClass;
    
    Pattern[] sequencePatterns = new Pattern[] { Pattern.compile("(?=(\\d{14}))"), Pattern.compile("(?=(\\d{15}))"),
            Pattern.compile("(?=(\\d{16}))"), Pattern.compile("(?=(\\d{4}[-\\s]\\d{4}[-\\s]\\d{4}[-\\s]\\d{4}))") };
    
    public ChecksumBasedCreditCardFilter(InputStream inputStream, OutputStream outputStream,
            Class<? extends Checksum> checksumClass) {
        this.reader = new BufferedReader(new InputStreamReader(inputStream));
        this.writer = new OutputStreamWriter(outputStream);
        this.checksumClass = checksumClass;
    }
    
    @Override
    public void run() {
        try {
            String currentLine = null;
            int lineCounter = 1;
            while ((currentLine = reader.readLine()) != null) {
                log.debug("==== Line {} ====", lineCounter++);
                String filteredOutput = filterOutPotentialCreditCards(currentLine);
                writeToOutputStream(filteredOutput);
                writeToOutputStream("\n");
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            closeStreams();
        }
    }
    
    private void closeStreams() {
        try {
            reader.close();
            writer.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
    
    private String filterOutPotentialCreditCards(String input) {
        log.debug("Read  {} chars : '{}'", input.length(), input);
        List<MatchedCreditCard> creditCards = findMatchingCreditCards(input);
        return maskMatchedCreditCards(input, creditCards);
    }
    
    private List<MatchedCreditCard> findMatchingCreditCards(String input) {
        List<MatchedCreditCard> creditCards = new ArrayList<MatchedCreditCard>();
        for (Pattern sequencePattern : sequencePatterns) {
            Matcher sequenceMatcher = sequencePattern.matcher(input);
            while (sequenceMatcher.find()) {
                int groupIndex = sequenceMatcher.groupCount();
                String matchedText = sequenceMatcher.group(groupIndex);
                int matchStartIndex = sequenceMatcher.start(groupIndex);
                int matchEndIndex = sequenceMatcher.end(groupIndex);
                
                log.debug("Match {} chars : '{}' (start = {}, end = {})", new Object[] { matchedText.length(),
                        matchedText, matchStartIndex, matchEndIndex });
                
                Checksum checksum = ChecksumFactory.instantiate(checksumClass, matchedText);
                if (checksum.isValid()) {
                    MatchedCreditCard creditCard = new MatchedCreditCard(matchedText, matchStartIndex, matchEndIndex);
                    creditCards.add(creditCard);
                }
            }
        }
        
        return creditCards;
    }
    
    private String maskMatchedCreditCards(String input, List<MatchedCreditCard> creditCards) {
        StringBuilder output = new StringBuilder(input);
        for (MatchedCreditCard creditCard : creditCards) {
            creditCard.maskTheInputAccordingly(output);
        }
        return output.toString();
    }
    
    private void writeToOutputStream(String text) throws IOException {
        writer.write(text);
        log.debug("Wrote {} chars : '{}'", text.length(), text.replace("\n", "\\n"));
    }
    
    public static void main(String[] args) throws IOException {
        new ChecksumBasedCreditCardFilter(System.in, System.out, LuhnChecksum.class).run();
    }
    
    private class MatchedCreditCard {
        
        private String text;
        private int start;
        private int end;
        
        public MatchedCreditCard(String text, int start, int end) {
            this.text = text;
            this.start = start;
            this.end = end;
        }
        
        @Override
        public String toString() {
            return this.text.replaceAll("\\d", MASK_CHARACTER);
        }
        
        public void maskTheInputAccordingly(StringBuilder input) {
            log.debug("Masked credit card : '{}' (start = {}, end = {})", new Object[] { text, start, end });
            input.replace(start, end, this.toString());
        }
        
    }
}
