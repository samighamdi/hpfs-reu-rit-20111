package kmeansmr;

/**
 * Abstract class for a two-dimension array to be stored and managed in a file
 * 
 * @author AssistantX
 */
abstract class ArrayFS {
    
    public static final byte BYTE = 1;
    public static final byte CHAR = 2;
    public static final byte SHORT = 2;
    public static final byte INT = 4;
    public static final byte DOUBLE = 8;
    public static final byte LONG = 8;
    
    protected static final byte HEADERSIZE = 13;
    protected static final int BUFFERSIZE = 1000;
}
