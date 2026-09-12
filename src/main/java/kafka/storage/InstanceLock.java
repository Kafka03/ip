package kafka.storage;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import kafka.exception.KafkaException;

/**
 * Holds an operating system lock while Kafka uses a task file.
 * The separate lock file stays in place because task saves replace the data file.
 */
public final class InstanceLock implements AutoCloseable {
    private final FileChannel channel;

    private InstanceLock(FileChannel channel) {
        this.channel = channel;
    }

    /**
     * Acquires the lock for a task file without waiting for another instance to exit.
     * The operating system releases the lock when this handle closes or the process exits.
     *
     * @param dataFile task file whose session must be exclusive.
     * @return handle to keep open throughout the session.
     * @throws KafkaException if another Kafka instance already holds the lock.
     * @throws IOException if the lock file cannot be opened or locked.
     */
    public static InstanceLock acquire(Path dataFile) throws KafkaException, IOException {
        Path absoluteFile = dataFile.toAbsolutePath().normalize();
        Files.createDirectories(absoluteFile.getParent());
        Path lockFile = absoluteFile.resolveSibling(absoluteFile.getFileName() + ".lock");
        FileChannel channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        try {
            if (!tryLock(channel)) {
                throw new KafkaException("Another Kafka window is already running. "
                        + "Close it before opening Kafka again.");
            }
            return new InstanceLock(channel);
        } catch (KafkaException | IOException exception) {
            try {
                channel.close();
            } catch (IOException closeException) {
                exception.addSuppressed(closeException);
            }
            throw exception;
        }
    }

    /**
     * Treats contention within this process the same as contention from another process.
     */
    private static boolean tryLock(FileChannel channel) throws IOException {
        try {
            return channel.tryLock() != null;
        } catch (OverlappingFileLockException exception) {
            return false;
        }
    }

    /**
     * Releases the lock by closing its channel, allowing another instance to start.
     */
    @Override
    public void close() throws IOException {
        channel.close();
    }
}
