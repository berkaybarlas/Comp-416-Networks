package utils;

public enum RequestType {
    /** Undefined Message type */
    UNDEFINED(-1, ""),
    /** Current weather request */
    CURRENT(0,"current"),
    /** Daily weather request */
    DAILY(1,"daily"),
    /** Minutely weather request */
    MINUTELY(2,"minutely"),
    /** Weather history request */
    HISTORY(3,"history"),
    /** Weather map request */
    MAP(4, "map");

    public int value;
    public String name;

    @Override
    public String toString() {
        return "" + value;
    }

    public static RequestType getRequestType(int value) {
        for (RequestType messageType : RequestType.values()) {
            if (messageType.value == (value)) {
                return messageType;
            }
        }
        return  UNDEFINED;
    };

    public static RequestType getRequestType(String name) {
        for (RequestType messageType : RequestType.values()) {
            if (messageType.name.equals(name)) {
                return messageType;
            }
        }
        return  UNDEFINED;
    };

    RequestType(int value, String name) {
        this.value = value; this.name=name;
    }
}