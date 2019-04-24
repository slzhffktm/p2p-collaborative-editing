package TCP;

import java.util.ArrayList;

public class Char {
    private ArrayList<Identifier> position;
    private int counter;
    private int siteId;
    private char value;

    public ArrayList<Identifier> getPosition() {
        return position;
    }

    public void setPosition(ArrayList<Identifier> position) {
        this.position = position;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    public char getValue() {
        return value;
    }

    public void setValue(char value) {
        this.value = value;
    }

    public Char(char value, int counter, int siteId, ArrayList<Identifier> position) {
        this.position = position;
        this.counter = counter;
        this.siteId = siteId;
        this.value = value;
    }

    public int compareTo(Char otherChar) {
        int comp;
        Identifier id1, id2;
        ArrayList<Identifier> pos1 = (ArrayList<Identifier>) this.position.clone();
        ArrayList<Identifier> pos2 = (ArrayList<Identifier>) otherChar.position.clone();

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
