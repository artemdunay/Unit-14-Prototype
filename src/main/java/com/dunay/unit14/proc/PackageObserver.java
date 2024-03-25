package com.dunay.unit14.proc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ArrayBlockingQueue;

@Component
public class PackageObserver {
    private final ArrayBlockingQueue<WatchEvent<?>> watchEventsQueue;
    private final WatchService watchService;
    public static final String DIRECTORY_PATH = "E:\\watch";


    @Autowired
    public PackageObserver(ArrayBlockingQueue<WatchEvent<?>> watchEventsQueue) throws IOException {
        this.watchEventsQueue = watchEventsQueue;
        watchService = FileSystems.getDefault().newWatchService();
        Path path = Paths.get(DIRECTORY_PATH);
        path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
    }

    @Scheduled(initialDelay = 100)
    public void observation() throws InterruptedException {
        WatchKey key;
        while ((key = watchService.take()) != null) {
            for (WatchEvent<?> event : key.pollEvents()) {
                watchEventsQueue.put(event);
            }
            key.reset();
        }
    }

}

