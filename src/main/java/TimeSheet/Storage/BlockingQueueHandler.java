package TimeSheet.Storage;

import java.util.concurrent.LinkedBlockingQueue;

public class BlockingQueueHandler {

    private LinkedBlockingQueue<SequentialRunnable> blockingQueue;
    private Thread thread;

    public void addRunnable(SequentialRunnable runnable) {
        blockingQueue.add(runnable);
    }

    public BlockingQueueHandler() {

        blockingQueue = new LinkedBlockingQueue<>();

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (!blockingQueue.take().run()) break;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

}
