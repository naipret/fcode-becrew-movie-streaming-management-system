package com.moviestreaming.util;

import com.moviestreaming.config.AppConstants;
import com.moviestreaming.exception.StorageException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Utility class that guarantees atomic, safe file writes by utilizing temporary files,
 * automatic backups, and OS-level atomic file replacement.
 */
public final class AtomicFileWriter {

    private static final String TEMP_EXTENSION = ".tmp";
    private static final String BACKUP_EXTENSION = ".bak";

    private AtomicFileWriter() {
        // Utility class constructor
    }

    /**
     * Atomically writes a list of lines to the specified target file path.
     *
     * @param targetFilePath the destination file path (e.g. "data/movies.csv")
     * @param lines          the lines to write
     */
    public static void writeLines(String targetFilePath, List<String> lines) {
        writeLines(Paths.get(targetFilePath), lines, StandardCharsets.UTF_8);
    }

    /**
     * Atomically writes a list of lines to the specified target Path using a given Charset.
     *
     * @param targetPath the destination Path
     * @param lines      the lines to write
     * @param charset    the character encoding
     */
    public static void writeLines(Path targetPath, List<String> lines, Charset charset) {
        if (targetPath == null) {
            throw new IllegalArgumentException("Target path cannot be null");
        }

        Path parentDir = targetPath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            try {
                Files.createDirectories(parentDir);
            } catch (IOException e) {
                throw new StorageException("Failed to create parent directory for " + targetPath, e);
            }
        }

        // 1. Create automated backup if target file already exists
        if (Files.exists(targetPath)) {
            createBackup(targetPath);
        }

        // 2. Write to temporary file
        Path tempPath = targetPath.resolveSibling(targetPath.getFileName().toString() + TEMP_EXTENSION);
        try (BufferedWriter writer = Files.newBufferedWriter(tempPath, charset)) {
            if (lines != null) {
                for (int i = 0; i < lines.size(); i++) {
                    writer.write(lines.get(i));
                    if (i < lines.size() - 1) {
                        writer.newLine();
                    }
                }
            }
            writer.flush();
        } catch (IOException e) {
            cleanUpFile(tempPath);
            throw new StorageException("Failed to write to temporary file " + tempPath, e);
        }

        // 3. Perform atomic replacement
        try {
            Files.move(tempPath, targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            // Fallback for file systems that do not support ATOMIC_MOVE across partitions
            try {
                Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                cleanUpFile(tempPath);
                throw new StorageException("Failed to replace target file " + targetPath, ex);
            }
        } catch (IOException e) {
            cleanUpFile(tempPath);
            throw new StorageException("Failed to atomically move file to " + targetPath, e);
        }
    }

    private static void createBackup(Path targetPath) {
        try {
            Path backupDir = Paths.get(AppConstants.BACKUP_DIR);
            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
            }
            String backupFileName = targetPath.getFileName().toString() + BACKUP_EXTENSION;
            Path backupPath = backupDir.resolve(backupFileName);
            Files.copy(targetPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // Backup failure should log a warning but not halt the core save operation
            System.err.println("[WARN] Unable to create backup for " + targetPath + ": " + e.getMessage());
        }
    }

    private static void cleanUpFile(Path path) {
        if (path != null && Files.exists(path)) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
                // Best-effort cleanup
            }
        }
    }
}
