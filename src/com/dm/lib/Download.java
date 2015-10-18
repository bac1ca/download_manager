package com.dm.lib;

import java.net.URL;
import java.nio.file.Path;

public interface Download {

    /**
     * Returns URL address which represents the source
     * @return URL address which represents the source
     */
    URL getSource();

    /**
     * Returns path to destination file
     * @return path to destination file
     */
    Path getDestination();

    /**
     * Returns progress value which is defined in interval [0.0, 1.0],
     *
     * @return progress value which is defined in interval [0.0, 1.0],
     *         if returned value less than 0 it means that "content-length"
     *         is not provided by the server
     */
    double getProgress();


    /**
     * Returns the value of the "content-length" header field
     *
     * @return  the content length of the resource that this connection's URL
     *          references, or {@code -1} if the content length is not known.
     */
    long getContentLen();

    /**
     * Returns current {@link com.dm.lib.Download.Status} of this download task
     * @return current {@link com.dm.lib.Download.Status} of this download task
     */
    Status getStatus();

    /**
     * Interrupts current download task,
     * after this action download task should be moved to CANCELED state,
     * destination file should be removed
     */
    void cancel();

    /**
     * Blocks until current download task is finished
     * @return the latest {@link com.dm.lib.Download.Status} of the download
     * @throws InterruptedException
     */
    Status waitForFinish() throws InterruptedException;

    /**
     * Sets status/progress listener
     * @param listener status/progress listener
     */
    void setStatusListener(StatusListener listener);


    /**
     * Status lisneter interface
     */
    interface StatusListener {
        void notify(Status status, double progress);
    }

    enum State {
        INPROGRESS,
        COMPLETED,
        CANCELED,
        FAILED,
    }

    class Status {

        private final State state;
        private final Exception error;

        Status(State state, Exception error) {
            this.state = state;
            this.error = error;
        }

        public State getState() {
            return state;
        }

        public Exception getError() {
            return error;
        }

        @Override
        public String toString() {
            String res = state.toString();
            if (error != null) {
                res += error.getCause();
            }
            return res;
        }
    }

}
