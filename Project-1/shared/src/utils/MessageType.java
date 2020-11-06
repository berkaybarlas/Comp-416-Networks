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
    DATA_CONNECTION_REQUEST(4),
    DATA_CONNECTION_ACCEPTED(5),
    DATA_CONNECTION_DECLINED(6),
    API_REQUEST(7),
    API_RESPONSE_SUCCESS(8),
    API_RESPONSE_FAIL(9),
    API_REQUEST_DATA(10),
    API_DATA_HASH(11),
    API_DATA_RECEIVED(12),
    API_DATA_FAILED(13),
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