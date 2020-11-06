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
    public MessageType type;
    public int headerSize = DEFAULT_MESSAGE_HEADER_SIZE;
    public int tokenSize = DEFAULT_TOKEN_SIZE;
    public String payload;
    public RequestType requestType;
    public String[] params;
    private String token;

    /**
     * Creates AUTH message without token
     *
     * @param type type of message
     * @param payload payload to be send in message
     */
    public MessageProtocol(MessageType type, String payload) {
        this(MessageMode.AUTH.phase, type, payload);
        this.tokenSize = 0;
    }


    /**
     * Creates a QUERY message with token
     *
     * @param type type of message
     * @param token message authentication token
     * @param payload payload to be send in message
     */
    public MessageProtocol(MessageType type, String token, String payload) {
        this(MessageMode.QUERY.phase, type, payload);
        // TOKEN SIZE SHOULD BE EQUAL TO TOKEN SIZE
        this.token = normalizeToken(token);
    }

    /**
     * Creates a socket message
     *
     * @param phase message mode
     * @param type type of message
     * @param payload payload to be send in message
     */
    private MessageProtocol(int phase, MessageType type, String payload) {
        this.phase = phase;
        this.type = type;
        this.payload = payload;
    }

    public static MessageProtocol createAPIRequest(String token, RequestType requestType, String[] params) {
        MessageProtocol messageProtocol = new MessageProtocol(MessageType.API_REQUEST, token, "");
        messageProtocol.requestType = requestType;
        messageProtocol.params = params;
        // Create payload for debug purposes
        String tempPayload = "" + requestType.value + params.length;

        for(int i=0; i<params.length; i++) {
            tempPayload += "X" + params[i];
        }
        messageProtocol.payload = tempPayload;

        return messageProtocol;
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
        type = MessageType.getMessageType((int) byteMessage[1]);
        int payloadSize = from4ByteArray(Arrays.copyOfRange(byteMessage, 2, headerSize));

        if (phase == MessageMode.QUERY.phase) {

            this.token = new String(Arrays.copyOfRange(byteMessage, headerSize, headerSize + tokenSize));
            int currentByteInd = headerSize + tokenSize;
            if (type == MessageType.API_REQUEST) {
                /** Special case for request type */

                requestType = RequestType.getRequestType((int) byteMessage[currentByteInd++]);

                /** General de-constructor for request params */
                int paramNumber = (int) byteMessage[currentByteInd++];

                params = new String[paramNumber];
                /** Temp Payload for debug purposes */
                String tempPayload = "" + requestType + paramNumber;

                for(int i=0; i<paramNumber; i++) {
                    int paramSize = (int) byteMessage[currentByteInd++];
                    params[i] = new String(Arrays.copyOfRange(byteMessage, currentByteInd, currentByteInd + paramSize));
                    currentByteInd = currentByteInd + paramSize;
                    tempPayload += paramSize + params[i];
                }
                this.payload = tempPayload;

            } else {
                /** Default case for QUERY Phase messages */
                this.payload = new String(Arrays.copyOfRange(
                        byteMessage,headerSize + tokenSize, currentByteInd + payloadSize));
            }

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
        byteMessage[1] = (byte) type.value;
        /** Add payload size */
        System.arraycopy(to4ByteArray(payloadBytes.length), 0, byteMessage,  2, 4);

        if (phase == MessageMode.QUERY.phase) {
            /** Add token */
            byte[] tokenByte = token.getBytes();
            System.arraycopy(tokenByte, 0, byteMessage,  headerSize, tokenSize);

            int currentByteInd = headerSize + tokenSize;
            if (type == MessageType.API_REQUEST) {
                /**  Special case for request type */
                /** Add Request type */
                byteMessage[currentByteInd++] = (byte) requestType.value;
                /** Add Param number */
                byteMessage[currentByteInd++] = (byte) params.length;

                /** General constructor for request params */
                for(int i=0; i < params.length; i++) {
                    int paramSize = params[i].length();
                    byteMessage[currentByteInd++] = (byte) paramSize;

                    System.arraycopy(params[i].getBytes(), 0, byteMessage,  currentByteInd, paramSize);
                    currentByteInd = currentByteInd + paramSize;
                }

            } else {
                /** Default case for QUERY Phase messages */
                /** Add payload */
                System.arraycopy(payloadBytes, 0, byteMessage,  currentByteInd, payloadBytes.length);
            }

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
