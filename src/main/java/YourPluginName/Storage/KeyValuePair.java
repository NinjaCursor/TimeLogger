package YourPluginName.Storage;

public interface KeyValuePair<K, T> {
    K getKey();
    T getValue();
}
