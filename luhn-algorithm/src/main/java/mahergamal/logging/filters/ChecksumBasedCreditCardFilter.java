package mahergamal.logging.filters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
    
    Pattern sequencePattern = Pattern.compile("(\\d{14,16}|\\d{4}[-\\s]\\d{4}[-\\s]\\d{4}[-\\s]\\d{4})");
    
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
                filterAnyPossibleCreditCards(currentLine);
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
    
    private void filterAnyPossibleCreditCards(String input) throws IOException {
        log.debug("Read  {} chars : '{}'", input.length(), input);
        
        Matcher sequenceMatcher = sequencePattern.matcher(input);
        int filterIndex = 0;
        int endIndex = input.length();
        
        while (sequenceMatcher.find()) {
            int groupIndex = sequenceMatcher.groupCount();
            String matchedText = sequenceMatcher.group(groupIndex);
            int matchStartIndex = sequenceMatcher.start(groupIndex);
            int matchEndIndex = matchStartIndex + sequenceMatcher.end(groupIndex);
            
            log.debug("Filter index : {}", filterIndex);
            
            log.debug("Match {} chars : '{}' (start = {}, end = {})", new Object[] { matchedText.length(), matchedText,
                    matchStartIndex, matchEndIndex });
            
            if (filterIndex < matchStartIndex) {
                log.debug("Writing unmatched text from {} to {}", filterIndex, matchStartIndex - 1);
                writeToOutputStream(input.substring(filterIndex, matchStartIndex));
            }
            
            Checksum checksum = ChecksumFactory.instantiate(checksumClass, matchedText);
            if (checksum.isValid()) {
                log.debug("Writing masked text...");
                writeToOutputStream(mask(matchedText));
            } else {
                log.debug("Writing plain text...");
                writeToOutputStream(matchedText);
            }
            
            filterIndex = matchEndIndex;
            
        }
        
        if (filterIndex < endIndex) {
            String remainingInputString = input.substring(filterIndex, endIndex);
            writeToOutputStream(remainingInputString);
        }
    }
    
    private String mask(String input) {
        return input.replaceAll("\\d", MASK_CHARACTER);
    }
    
    private void writeToOutputStream(String text) throws IOException {
        writer.write(text);
        log.debug("Wrote {} chars : '{}'", text.length(), text.replace("\n", "\\n"));
    }
    
    public static void main(String[] args) throws IOException {
        new ChecksumBasedCreditCardFilter(System.in, System.out, LuhnChecksum.class).run();
    }
    
}
