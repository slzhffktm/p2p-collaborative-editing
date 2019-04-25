package UDP;

import java.util.List;

public class Char {
    private List<Identifier> position;
    private int counter;
    private String siteId;
    private char value;

    public List<Identifier> getPosition() {
        return position;
    }

    public int getCounter() {
        return counter;
    }

    public String getSiteId() {
        return siteId;
    }

    public char getValue() {
        return value;
    }

    public Char(char value, int counter, String siteId, List<Identifier> position) {
        this.position = position;
        this.counter = counter;
        this.siteId = siteId;
        this.value = value;
    }

    public int compareTo(Char other) {
        List<Identifier> thisPosition = this.position;
        List<Identifier> otherPosition = other.position;

        int thisPosSize = thisPosition.size();
        int otherPosSize = otherPosition.size();

        int minPosSize = Math.min(thisPosSize, otherPosSize);
        for (int i = 0; i < minPosSize; i++) {
            Identifier thisIndex = thisPosition.get(i);
            Identifier otherIndex = otherPosition.get(i);
            if (thisIndex.compareTo(otherIndex) != 0) {
                return thisIndex.compareTo(otherIndex);
            }
        }

        if (thisPosSize < otherPosSize) {
            return -1;
        } else if (thisPosSize > otherPosSize) {
            return 1;
        } else {
            return 0;
        }
    }
}

