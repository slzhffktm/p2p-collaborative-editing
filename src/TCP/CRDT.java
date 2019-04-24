package TCP;

import java.util.ArrayList;

public class CRDT {
    private int siteId;
    private ArrayList<Char> struct;

    public CRDT(int siteId) {
        this.siteId = siteId;
        this.struct = new ArrayList<>();
    }

    public char localInsert(Char value, int index) {
        Char c;
        c = this.generateChar(value, index);
        this.struct.add(index, c);
    }

    public Char generateChar(Char val, int index) {
        ArrayList<Identifier> posBefore = (this.struct.get(index - 1).getPosition());
        ArrayList<Identifier> posAfter = (this.struct.get(index - 1).getPosition());
        ArrayList<Identifier> newPos = (this.generatePosBetween(posBefore, posAfter);

        return new Char(val, , , newPos)
    }

    private ArrayList<Identifier> generatePosBetween(ArrayList<Identifier> posBefore, ArrayList<Identifier> posAfter) {
        Identifier id1 = posBefore.get(0);
        Identifier id2 = posAfter.get(0);

        if (id1.getDigit() - id2.getDigit() > 1) {

        }
    }
}