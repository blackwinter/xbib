
package org.latency;

/**
 * Accepts pause notification events.
 * <p>
 * All times and time units are in nanoseconds
 */
public interface PauseDetectorListener {

    /**
     * handle a pause event notification.
     *
     * @param pauseLength  Length of reported pause (in nanoseconds)
     * @param pauseEndTime Time sampled at end of reported pause (in nanoTime units).
     */
    public void handlePauseEvent(long pauseLength, long pauseEndTime);
}
