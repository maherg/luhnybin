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
    
    Pattern sequencePattern = Pattern.compile("(\\d)(?=(\\d{13,15}))");
    
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
        log.debug("Processing {} chars : {}", input.length(), input);
        Matcher sequenceMatcher = sequencePattern.matcher(input);
        int filterIndex = -1;
        int endIndex = input.length();
        if (sequenceMatcher.matches()) {
            log.debug("Found {} possible matches", sequenceMatcher.groupCount());
            for (int i = 1; i <= sequenceMatcher.groupCount(); i++) {
                String matchedText = sequenceMatcher.group(i);
                int matchStartIndex = sequenceMatcher.start(i);
                int matchEndIndex = matchStartIndex + sequenceMatcher.end(i);
                
                log.debug("Possible match : {} ({},{})", new Object[] { matchedText, matchStartIndex, matchEndIndex });
                
                if ((filterIndex + 1) < matchStartIndex) {
                    writeToOutputStream(input.substring(filterIndex + 1, matchStartIndex));
                }
                
                Checksum checksum = ChecksumFactory.instantiate(checksumClass, matchedText);
                if (checksum.isValid()) {
                    writeToOutputStream(mask(matchedText));
                } else {
                    writeToOutputStream(matchedText);
                }
                
                filterIndex = matchEndIndex;
            }
        }
        if (filterIndex < endIndex) {
            String remainingInputString = input.substring(filterIndex + 1, endIndex);
            writeToOutputStream(remainingInputString);
        }
    }
    
    private String mask(String input) {
        return input.replaceAll("\\d", MASK_CHARACTER);
    }
    
    private void writeToOutputStream(String text) throws IOException {
        writer.write(text);
        writer.write("\n");
        log.debug("Wrote : {}", text);
    }
    
    public static void main(String[] args) throws IOException {
        new ChecksumBasedCreditCardFilter(System.in, System.out, LuhnChecksum.class).run();
    }
    
}
