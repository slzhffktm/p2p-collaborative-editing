package UDP;

import java.util.ArrayList;
import java.util.List;

public class Char {
    private List<Identifier> position;
    private int counter;
    private String siteId;
    private char value;

    public List<Identifier> getPosition() {
        return position;
    }

    public void setPosition(List<Identifier> position) {
        this.position = position;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public char getValue() {
        return value;
    }

    public void setValue(char value) {
        this.value = value;
    }

    public Char(char value, int counter, String siteId, List<Identifier> position) {
        this.position = position;
        this.counter = counter;
        this.siteId = siteId;
        this.value = value;
    }

    public int compareTo(Char otherChar) {
        int comp;
        Identifier id1, id2;
        List<Identifier> pos1 = this.position;
        List<Identifier> pos2 = otherChar.position;

        for (int i = 1; i < Math.min(pos1.size(), pos2.size()); i++) {
            id1 = pos1.get(i);
            id2 = pos2.get(i);
            comp = id1.compareTo(id2);

            if (comp != 0) {
                return comp;
            }
        }

        if (pos1.size() < pos2.size()) {
            return -1;
        } else if (pos1.size() > pos2.size()) {
            return 1;
        } else {
            return 0;
        }
    }
}

