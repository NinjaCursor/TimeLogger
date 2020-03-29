package TimeSheet.Storage;

@FunctionalInterface
public interface ThrowingFunction<K, T> {
    T apply(K k) throws Exception;
}
