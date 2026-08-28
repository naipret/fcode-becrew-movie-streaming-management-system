package com.moviestreaming.command;

/**
 * Interface representing an undoable and redoable command for watchlist operations.
 */
public interface WatchlistCommand {

    /**
     * Executes the command operation.
     */
    void execute();

    /**
     * Undoes the command operation, reversing the state change.
     */
    void undo();

    /**
     * Redoes the previously undone command operation.
     */
    void redo();

    /**
     * Returns a human-readable description of the command.
     *
     * @return command description
     */
    String getDescription();
}
