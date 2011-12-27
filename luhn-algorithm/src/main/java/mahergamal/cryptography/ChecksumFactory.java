package mahergamal.cryptography;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public class ChecksumFactory {
    
    private static Logger log = LoggerFactory.getLogger(ChecksumFactory.class);
    
    private ChecksumFactory() {
        
    }
    
    public static <T extends Checksum> T instantiate(Class<T> checksumClass, Object... args) {
        try {
            Class[] argumentTypes = extractConstructorArgumentTypes(args);
            return checksumClass.getConstructor(argumentTypes).newInstance(args);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
        
    }
    
    private static Class[] extractConstructorArgumentTypes(Object... args) {
        Class[] argumentTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            argumentTypes[i] = args[i].getClass();
        }
        return argumentTypes;
    }
    
}
