package utils;

public enum MessageType {
    /** Undefined Message type */
    UNDEFINED(-1),
    /** Authentication request */
    AUTH_REQUEST(0),
    /** Authentication challange */
    AUTH_CHALLENGE(1),
    /** Authentication failed */
    AUTH_FAILURE(2),
    /** Authentication successfull */
    AUTH_SUCCESS(3),
    DATA_CONNECTION_REQUEST(4),
    DATA_CONNECTION_ACCEPTED(5),
    DATA_CONNECTION_DECLINED(6),
    API_REQUEST(7),
    API_RESPONSE(8),
    API_REQUEST_DATA(9),
    API_DATA_HASH(10),
    API_DATA_RECEIVED(11),
    /** Connection closed due timeout */
    TIMEOUT(12);

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

    private MessageType(int value) {
        this.value = value;
    }
}