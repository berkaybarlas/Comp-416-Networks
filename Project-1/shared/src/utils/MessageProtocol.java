package utils;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class MessageProtocol {
    static public final int MAX_BYTE_SIZE = 1024;
    private final int DEFAULT_MESSAGE_HEADER_SIZE = 6;
    private final int DEFAULT_TOKEN_SIZE = 6;

    /**
     * Message phase/mod
     */
    public enum MessageMode {
        /** Authentication phase */
        AUTH(0),
        /** Query phase */
        QUERY(1);

        public final int phase;

        private MessageMode(int mode) {
            this.phase = mode;
        }
    }

    public int phase;
    public int type;
    public int headerSize = DEFAULT_MESSAGE_HEADER_SIZE;
    public int tokenSize = DEFAULT_TOKEN_SIZE;
    public String payload;
    private String token;

    /**
     * Creates a message without token
     *
     * @param type type of message
     * @param payload payload to be send in message
     */
    public MessageProtocol(int type, String payload) {
        this(MessageMode.AUTH.phase, type, payload);
        this.tokenSize = 0;
    }


    /**
     * Creates a message without token
     *
     * @param type type of message
     * @param token message authentication token
     * @param payload payload to be send in message
     */
    public MessageProtocol(int type, String token, String payload) {
        this(MessageMode.QUERY.phase, type, payload);
        // TOKEN SIZE SHOULD EQUAL TO TOKEN SIZE
        this.token = normalizeToken(token);
    }

    /**
     * Creates a socket message
     *
     * @param phase message mode
     * @param type type of message
     * @param payload payload to be send in message
     */
    public MessageProtocol(int phase, int type, String payload) {
        this.phase = phase;
        this.type = type;
        this.payload = payload;
    }

    /**
     * Creates a socket message with given bytes (de-constructor)
     *
     * @param byteMessage byte array to be constructed
     */
    public MessageProtocol(byte[] byteMessage) {
        // Prevent Index out of bound exception
        if (byteMessage.length == 0) {
            this.payload = "";
            return;
        }
        phase = (int) byteMessage[0];
        type = (int) byteMessage[1];
        int payloadSize = from4ByteArray(Arrays.copyOfRange(byteMessage, 2, headerSize));
        if (phase == MessageMode.QUERY.phase) {
            this.token = new String(Arrays.copyOfRange(byteMessage, headerSize, headerSize + tokenSize));
            this.payload = new String(Arrays.copyOfRange(
                    byteMessage,headerSize + tokenSize, payloadSize + headerSize + tokenSize));
        } else if (phase == MessageMode.AUTH.phase){
            this.payload =  new String(Arrays.copyOfRange(byteMessage, headerSize, payloadSize + headerSize));
        }
    }
    /**
     * Converts message to byte array
     *
     */
    public byte[] getByteMessage() {
        if (payload == null) {
            return new byte[0];
        }
        byte[] payloadBytes = payload.getBytes();
        byte[] byteMessage = new byte[headerSize + tokenSize + payloadBytes.length];
        byteMessage[0] = (byte) phase;
        byteMessage[1] = (byte) type;
        // Add payload size
        System.arraycopy(to4ByteArray(payloadBytes.length), 0, byteMessage,  2, 4);
        if (phase == MessageMode.QUERY.phase) {
            // Add token
            byte[] tokenByte = token.getBytes();
            System.arraycopy(tokenByte, 0, byteMessage,  headerSize, tokenSize);
            // Add payload
            System.arraycopy(payloadBytes, 0, byteMessage,  headerSize + tokenSize, payloadBytes.length);
        } else if (phase == MessageMode.AUTH.phase){
            // Add payload size
            System.arraycopy(payloadBytes, 0, byteMessage,  headerSize, payloadBytes.length);
        }
        return byteMessage;
    }

    /**
     * Check whether given token is same with message
     *
     */
    public boolean checkToken(String token) {
        return normalizeToken(token).equals(this.token);
    }
    /**
     * Normalizes token to a fixed size
     *
     */
    private String normalizeToken(String token) {
        return (token + new String(new char[tokenSize])).substring(0, tokenSize);
    }

    /**
     * Converts int to 4 bit byte array
     *
     */
    private byte[] to4ByteArray(int value) {
        return  ByteBuffer.allocate(4).putInt(value).array();
    }

    /**
     * Converts byte array into int
     *
     */
    private int from4ByteArray(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8 ) |
                ((bytes[3] & 0xFF) << 0 );
    }
}
