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
    API_REQUEST(4),
    API_RESPONSE(5),
    API_REQUEST_DATA(6),
    API_DATA_HASH(7),
    API_DATA_RECEIVED(8);

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