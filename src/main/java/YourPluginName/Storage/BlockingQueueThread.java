package YourPluginName.Storage;

public class BlockingQueueThread {

    private BlockingQueueHandler queueHandler;

    public BlockingQueueThread() {
        this.queueHandler = new BlockingQueueHandler();
    }

    /* run() runs runnable in queue
     */
    public void run(SequentialRunnable runnable) {
        queueHandler.addRunnable(runnable);
    }

    /* stop() stops the queueHandler thread
     * @precondition: none
     * @postcondition: queue completes and returns to main thread
     */
    public void stop() {
        this.queueHandler.addRunnable(new SequentialRunnable() {
            @Override
            public boolean run() {
                return false;
            }
        });
    }

}
