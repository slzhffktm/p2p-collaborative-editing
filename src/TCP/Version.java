package TCP;

import java.util.ArrayList;

/**
 * Information about each version.
 * exceptions are a set of counters for operations that our local CRDT has not
 * seen or integrated yet. Waiting for these operations.
 */

public class Version {
    private int siteId;
    private int counter;
    private ArrayList<int> exceptions;

    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public ArrayList<int> getExceptions() {
        return exceptions;
    }

    public void setExceptions(ArrayList<int> exceptions) {
        this.exceptions = exceptions;
    }

    public Version(int siteId) {
        this.siteId = siteId;
        this.counter = 0;
        this.exceptions = new ArrayList<>();
    }

    /**
     * Update a site's version based on the incoming operation that was processed
     * If the incomingCounter is less than we had previously processed, we can remove it from the exceptions
     * Else if the incomingCounter is the operation immediately after the last one we procesed, we just increment our counter to reflect that
     * Else, add an exception for each counter value that we haven't seen yet, and update our counter to match
     *
     * @param version
     */
    public void update(Version version){
        int incomingCounter = version.counter;

        if (incomingCounter <= this.counter) {
            int index = this.exceptions.indexOf(incomingCounter);
            this.exceptions.add(index, 1);
        } else if (incomingCounter == this.counter + 1) {
            this.counter += 1;
        } else {
            for (int i = this.counter + 1; i < incomingCounter; i++) {
                this.exceptions.add(i);
            }
            this.counter = incomingCounter;
        }
    }
}
