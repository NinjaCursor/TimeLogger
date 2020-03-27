package YourPluginName.Storage;

public interface GeneralHandler<K, T> extends BooleanSetup {
    Handler<K, T> getHandler();
}
