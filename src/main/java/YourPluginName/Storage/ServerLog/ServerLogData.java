package YourPluginName.Storage.ServerLog;

import YourPluginName.Storage.KeyValuePair;

public class ServerLogData implements KeyValuePair {

    @Override
    public Object getKey() {
        return null;
    }

    @Override
    public Object getValue() {
        return null;
    }
    private ServerEvent event;
    private long timeStamp;

    public ServerLogData(ServerEvent event, long timeStamp) {
        this.event = event;
        this.timeStamp = timeStamp;
    }

    public ServerEvent getEvent() {
        return event;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}
