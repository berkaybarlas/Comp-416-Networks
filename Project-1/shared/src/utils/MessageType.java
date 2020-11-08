package utils;

public enum MessageType {
    /** Undefined Message type */
    UNDEFINED(-1),
    /** Authentication request */
    AUTH_REQUEST(0),
    /** Authentication challange */
    AUTH_CHALLENGE(1),
    /** Authentication failed */
    AUTH_FAIL(2),
    /** Authentication successfull */
    AUTH_SUCCESS(3),
    /** Data connection request from client */
    DATA_CONNECTION_REQUEST(4),
    /** Data connection accepted by server */
    DATA_CONNECTION_ACCEPTED(5),
    /** Data connection declined by server */
    DATA_CONNECTION_DECLINED(6),
    /** API request by client */
    API_REQUEST(7),
    /** API request successful */
    API_RESPONSE_SUCCESS(8),
    /** A failure during API Response */
    API_RESPONSE_FAIL(9),
    /** Message type for requesting data from server */
    API_REQUEST_DATA(10),
    /** Message type that includes file hash */
    API_DATA_HASH(11),
    /** Data received correctly */
    API_DATA_RECEIVED(12),
    /** Data transfer failed due to hash mismatch */
    API_DATA_FAILED(13),
    /** Request process completed */
    API_PROCESS_COMPLETE(14),
    /** Connection closed */
    CONNECTION_CLOSED(90),
    /** Connection closed due timeout */
    TIMEOUT(99);

    public int value;

    @Override
    public String toString() {
        return "" + value;
    }

    public static MessageType getMessageType(int value) {
        for (MessageType messageType : MessageType.values()) {
            if (messageType.value == (value)) {
                return messageType;
            }
        }
        return  UNDEFINED;
    };

    MessageType(int value) {
        this.value = value;
    }
}