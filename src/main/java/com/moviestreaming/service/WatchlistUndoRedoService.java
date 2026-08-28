package com.moviestreaming.service;

import com.moviestreaming.command.WatchlistCommand;
import com.moviestreaming.config.AppConstants;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing dual bounded history stacks for Watchlist Undo/Redo operations. Implements the
 * Command Pattern invoker lifecycle with capacity limits.
 */
public class WatchlistUndoRedoService {

    private final int maxStackSize;
    private final Map<String, Deque<WatchlistCommand>> userUndoStacks = new ConcurrentHashMap<>();
    private final Map<String, Deque<WatchlistCommand>> userRedoStacks = new ConcurrentHashMap<>();

    public WatchlistUndoRedoService() {
        this(AppConstants.MAX_UNDO_REDO_STACK_SIZE);
    }

    public WatchlistUndoRedoService(int maxStackSize) {
        if (maxStackSize <= 0) {
            throw new IllegalArgumentException("Max stack size must be positive.");
        }
        this.maxStackSize = maxStackSize;
    }

    /**
     * Executes a command and pushes it onto the user's undo stack, invalidating the redo stack.
     *
     * @param userId the user ID
     * @param command the watchlist command
     */
    public synchronized void execute(String userId, WatchlistCommand command) {
        if (userId == null || userId.trim().isEmpty() || command == null) {
            throw new IllegalArgumentException("User ID and Command must not be null or empty.");
        }
        command.execute();

        Deque<WatchlistCommand> undoStack =
                userUndoStacks.computeIfAbsent(userId.trim(), k -> new ArrayDeque<>());
        if (undoStack.size() >= maxStackSize) {
            undoStack.removeLast(); // Evict oldest action from bottom of stack
        }
        undoStack.push(command);

        Deque<WatchlistCommand> redoStack = userRedoStacks.get(userId.trim());
        if (redoStack != null) {
            redoStack.clear();
        }
    }

    /**
     * Undoes the last executed command for the user.
     *
     * @param userId the user ID
     * @return the undone command, or empty if nothing to undo
     */
    public synchronized Optional<WatchlistCommand> undo(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return Optional.empty();
        }
        Deque<WatchlistCommand> undoStack = userUndoStacks.get(userId.trim());
        if (undoStack == null || undoStack.isEmpty()) {
            return Optional.empty();
        }

        WatchlistCommand command = undoStack.pop();
        command.undo();

        Deque<WatchlistCommand> redoStack =
                userRedoStacks.computeIfAbsent(userId.trim(), k -> new ArrayDeque<>());
        if (redoStack.size() >= maxStackSize) {
            redoStack.removeLast();
        }
        redoStack.push(command);

        return Optional.of(command);
    }

    /**
     * Redoes the last undone command for the user.
     *
     * @param userId the user ID
     * @return the redone command, or empty if nothing to redo
     */
    public synchronized Optional<WatchlistCommand> redo(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return Optional.empty();
        }
        Deque<WatchlistCommand> redoStack = userRedoStacks.get(userId.trim());
        if (redoStack == null || redoStack.isEmpty()) {
            return Optional.empty();
        }

        WatchlistCommand command = redoStack.pop();
        command.redo();

        Deque<WatchlistCommand> undoStack =
                userUndoStacks.computeIfAbsent(userId.trim(), k -> new ArrayDeque<>());
        if (undoStack.size() >= maxStackSize) {
            undoStack.removeLast();
        }
        undoStack.push(command);

        return Optional.of(command);
    }

    /**
     * Checks if the user has actions available to undo.
     *
     * @param userId the user ID
     * @return true if undo is available
     */
    public boolean canUndo(String userId) {
        if (userId == null) {
            return false;
        }
        Deque<WatchlistCommand> stack = userUndoStacks.get(userId.trim());
        return stack != null && !stack.isEmpty();
    }

    /**
     * Checks if the user has actions available to redo.
     *
     * @param userId the user ID
     * @return true if redo is available
     */
    public boolean canRedo(String userId) {
        if (userId == null) {
            return false;
        }
        Deque<WatchlistCommand> stack = userRedoStacks.get(userId.trim());
        return stack != null && !stack.isEmpty();
    }

    /**
     * Returns the list of command descriptions currently in the undo stack.
     *
     * @param userId the user ID
     * @return unmodifiable list of descriptions
     */
    public List<String> getUndoHistory(String userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        Deque<WatchlistCommand> stack = userUndoStacks.get(userId.trim());
        if (stack == null || stack.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<>();
        for (WatchlistCommand cmd : stack) {
            list.add(cmd.getDescription());
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * Returns the list of command descriptions currently in the redo stack.
     *
     * @param userId the user ID
     * @return unmodifiable list of descriptions
     */
    public List<String> getRedoHistory(String userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        Deque<WatchlistCommand> stack = userRedoStacks.get(userId.trim());
        if (stack == null || stack.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<>();
        for (WatchlistCommand cmd : stack) {
            list.add(cmd.getDescription());
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * Clears undo and redo histories for a user.
     *
     * @param userId the user ID
     */
    public synchronized void clearHistory(String userId) {
        if (userId != null) {
            userUndoStacks.remove(userId.trim());
            userRedoStacks.remove(userId.trim());
        }
    }
}
