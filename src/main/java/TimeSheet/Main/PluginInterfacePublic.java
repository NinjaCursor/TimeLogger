package TimeSheet.Main;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PluginInterfacePublic {

    /* getTimePackage()
     * @params: uuid of player, eventName
     * @precondition: eventName exists
     * @returns: completablefuture of PlayerTimePackage containing logs and summary data if event is registered, else null
     */
    CompletableFuture<PlayerTimePackage> getTimePackage(final UUID uuid, final String eventName);

    /* createHandler()
     * @params: name of event type to register
     * @precondition: none
     * @postcondition: if name is unique, it will be registered
     * @returns: completable future returning true if unique, else false
     * note: once an event is registered by your plugin, your plugin is responsible for remembering what events you registered
     */
    CompletableFuture<Boolean> createHandler(String name);

    /* start(), stop()
     * @params: name of event types, e.g., logging in and out or going afk; uuid of player; timeStamp of event in epoch time
     * @precondition: name of event type is registered with the createHandler
     * @postcondition: sum_table and log_table are updated with given data asynchronously if event is registered
     */
    void start(final String name, final UUID uuid, final long timeStamp);
    void stop(final String name, final UUID uuid, final long timeStamp);
}
