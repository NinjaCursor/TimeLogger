package YourPluginName.Storage;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SerializableAs("LogData")
public class LogData implements ConfigurationSerializable, VertXDataType {

    private boolean logoutIsNull;
    private UUID uuid;
    private long loginTime, logoutTime;
    private String type;

    private void setup(String type, UUID uuid, long loginTime) {
        this.type = type;
        this.uuid = uuid;
        this.loginTime = loginTime;
    }

    public LogData(String type, UUID uuid, long loginTime, long logoutTime) {
        setup(type, uuid, loginTime);
        this.logoutTime = logoutTime;
        this.logoutIsNull = false;
    }

    public LogData(String type, UUID uuid, long loginTime) {
        setup(type, uuid, loginTime);
        this.logoutIsNull = true;
    }

    public long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(long loginTime) {
        this.loginTime = loginTime;
    }

    public long getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(long logoutTime) {
        this.logoutTime = logoutTime;
        this.logoutIsNull = false;
    }

    public boolean isLogoutIsNull() {
        return logoutIsNull;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uuid", getUuid());
        hashMap.put("login", getLoginTime());
        hashMap.put("logout", getLogoutTime());
        hashMap.put("type", this.type);
        return hashMap;
    }

    public static LogData deserialize(Map<String, Object> args) {
        LogData logData = new LogData((String) args.get("type"), (UUID) args.get("uuid"), (long) args.get("login"), (long) args.get("logout"));
        return logData;
    }

}
