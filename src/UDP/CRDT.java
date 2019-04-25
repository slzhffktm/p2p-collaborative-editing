package UDP;

import java.util.ArrayList;
import java.util.List;

public class CRDT {
    private VersionVector vector;
    private int base;
    private int boundary;
    private Peer2Peer controller;
    private String siteId;
    private ArrayList<Char> struct;

    public CRDT(String siteId, Peer2Peer controller) {
        this.siteId = siteId;
        this.struct = new ArrayList<>();
        this.vector = controller.getVector();
        this.base = 32;
        this.boundary = 10;
        this.controller = controller;
    }

    public Char localInsert(char value, int index) {
        this.vector.increment();
        Char c;
        c = this.generateChar(value, index);
        this.struct.add(index, c);
        return c;
    }

    public void remoteInsert(Char c) {
        int index = this.findInsertIndex(c);
        System.out.println("find insert index: " + index);
        this.struct.add(index, c);
//        this.controller.insertToTextEditor(c.getValue(), index);
    }

    public Char localDelete(int index) {
        this.controller.getVector().increment();
        Char c = this.struct.get(index - 1);

        this.struct.remove(index - 1);
        return c;
    }

    public void remoteDelete(Char c) {
        int index = findPosition(c);

        if (index == -1) {
            return;
        }
        this.struct.remove(index);
//        this.controller.deleteToTextEditor(index);
    }

    public int findInsertIndex(Char c) {
        int left = 0;
        int right = this.struct.size() - 1;
        int mid, compareNum;

        if (this.struct.size() == 0 || c.compareTo(this.struct.get(left)) < 1) {
            return left;
        } else if (c.compareTo(this.struct.get(right)) > 0) {
            return this.struct.size();
        }

        while ((left + 1) < right) {
            mid = (int) Math.floor(left + (right - left) / 2);
            compareNum = c.compareTo(this.struct.get(mid));

            if (compareNum == 0) {
                return mid;
            } else if (compareNum > 0) {
                left = mid;
            } else {
                right = mid;
            }
        }

        return (c.compareTo(this.struct.get(left)) == 0 ? left : right);
    }

    public int findIndexByPosition(Char c) {
        int left = 0;
        int right = this.struct.size() - 1;
        int mid, compareNum;

        if (this.struct.size() == 0) {
            throw new Error("Character doesn't exist in CRDT");
//            return 0;
        }

        while (left + 1 < right) {
            mid = (int) Math.floor(left + (right - left) / 2);
            compareNum = c.compareTo(this.struct.get(mid));

            if (compareNum == 0) {
                return mid;
            } else if (compareNum > 0) {
                left = mid;
            } else {
                right = mid;
            }
        }

        if (c.compareTo(this.struct.get(left)) == 0) {
            return left;
        } else if (c.compareTo(this.struct.get(right)) == 0) {
            return right;
        } else {
            throw new Error("Character doesn't exist in CRDT");
//            return 0;
        }
    }

    public int findPosition(Char c) {
        for (int i = 0; i < this.struct.size(); i++) {
            if (c.compareTo(this.struct.get(i)) == 0) {
                return i;
            }
        }
        return -1;
    }

    public Char generateChar(char val, int index) {
        ArrayList<Identifier> posBefore;

        if (((index - 1) >= 0) && ((index - 1) < this.struct.size())) {
            posBefore = this.struct.get(index - 1).getPosition();
        } else {
            posBefore = new ArrayList<Identifier>();
        }

        ArrayList<Identifier> posAfter;

        if (((index) >= 0) && ((index) < this.struct.size())) {
            posAfter = this.struct.get(index).getPosition();
        } else {
            posAfter = new ArrayList<Identifier>();
        }

        ArrayList<Identifier> newPos = new ArrayList<Identifier>();
        this.generatePosBetween(posBefore, posAfter, newPos, 0);
        int localCounter = this.vector.getLocalVersion().getCounter();

        return new Char(val, this.vector.getLocalVersion().getCounter(), this.siteId, newPos);
    }

    private char retrieveStrategy(int level) {
        return (Math.round(Math.random()) == 1 ? '+' : '-');
    }

    public int generateIdBetween(int min, int max, char boundaryStrategy) {
        if ((max - min) < this.boundary) {
            min = min + 1;
        } else {
            if (boundaryStrategy == '-') {
                min = max - this.boundary;
            } else {
                min = min + 1;
                max = min + this.boundary;
            }
        }
        return ((int) Math.floor(Math.random() * (max - min)) + min);
    }

    public void generatePosBetween(List<Identifier> posBefore,
                                   List<Identifier> posAfter,
                                   List<Identifier> newPos,
                                   int level) {

        int base = (int) Math.pow(2, level) * this.base;
        char boundaryStrategy = this.retrieveStrategy(level);

        Identifier idBefore;
        if (posBefore.size() > 0) {
            idBefore = posBefore.get(0);
        } else {
            idBefore = new Identifier(0, this.siteId);
        }
        Identifier idAfter;
        if (posAfter.size() > 0) {
            idAfter = posAfter.get(0);
        } else {
            idAfter = new Identifier(base, this.siteId);
        }

//        System.out.println("[generatePosBetween] idBefore = " + idBefore.getDigit() + ", idAfter = " + idAfter.getDigit());

        if ((idAfter.getDigit() - idBefore.getDigit()) > 1) {
            int newDigit = this.generateIdBetween(idBefore.getDigit(),
                    idAfter.getDigit(),
                    boundaryStrategy);
//            System.out.println("[generatePosBetween] newDigit = " + newDigit);
            newPos.add(new Identifier(newDigit, this.siteId));
        } else if ((idAfter.getDigit() - idBefore.getDigit()) == 1) {
            newPos.add(idBefore);
            if (posBefore.size() > 0) {
                posBefore = posBefore.subList(1, posBefore.size());
            }
            this.generatePosBetween(posBefore,
                    new ArrayList<Identifier>(),
                    newPos,
                    level + 1);
        } else if (idBefore.getDigit() == idAfter.getDigit()) {
            int comSiteId = idBefore.getSiteId().compareTo(idAfter.getSiteId());
            if (comSiteId < 0) {
                newPos.add(idBefore);
                if (posBefore.size() > 0) {
                    posBefore = posBefore.subList(1, posBefore.size());
                }
                this.generatePosBetween(posBefore,
                        new ArrayList<Identifier>(),
                        newPos,
                        level + 1);
            } else if (comSiteId == 0) {
                newPos.add(idBefore);
                if (posBefore.size() > 0) {
                    posBefore = posBefore.subList(1, posBefore.size());
                }
                if (posAfter.size() > 0) {
                    posAfter = posAfter.subList(1, posAfter.size());
                }
                this.generatePosBetween(posBefore,
                        posAfter,
                        newPos,
                        level + 1);
            } else {
                throw new Error("u no gud at coding");
            }
        }
    }
//
//    private void generatePosBetween(ArrayList<Identifier> posBefore, ArrayList<Identifier> posAfter, ArrayList<Identifier> newPos, int level) {
//        Identifier id1 = (posBefore.size() > 0 ? posBefore.get(0) : new Identifier(0, this.siteId));
//        Identifier id2 = (posAfter.size() > 0 ? posAfter.get(0) : new Identifier(0, this.siteId));
//
//        int base = (int) Math.pow(2, level) * this.base;
//        char boundaryStrategy = this.retrieveStrategy(level);
//
//        if (id1.getDigit() - id2.getDigit() > 1) {
//            int newDigit = this.generateIdBetween(id1.getDigit(), id2.getDigit(), boundaryStrategy);
//            newPos.add(new Identifier(newDigit, this.siteId));
//        } else if (id2.getDigit() - id1.getDigit() == 1) {
//            newPos.add(id1);
//            if (posBefore.size() > 0) {
//                List sublist = posBefore.subList(1, posBefore.size());
//                posBefore = new ArrayList<Identifier>(sublist);
//            }
//            this.generatePosBetween(posBefore, new ArrayList<Identifier>(), newPos, level + 1);
//        } else if (id1.getDigit() == id2.getDigit()) {
//            int compare = id1.getSiteId().compareTo(id2.getSiteId());
//            if (compare < 0) {
//                newPos.add(id1);
//                if (posBefore.size() > 0) {
//                    List sublist = posBefore.subList(1, posBefore.size());
//                    posBefore = new ArrayList<Identifier>(sublist);
//                }
//                this.generatePosBetween(posBefore, new ArrayList<Identifier>(), newPos, level + 1);
//            } else if (compare == 0) {
//                newPos.add(id1);
//                if (posBefore.size() > 0) {
//                    List sublist = posBefore.subList(1, posBefore.size());
//                    posBefore = new ArrayList<Identifier>(sublist);
//                }
//                if (posAfter.size() > 0) {
//                    List sublist = posAfter.subList(1, posAfter.size());
//                    posAfter = new ArrayList<Identifier>(sublist);
//                }
//                this.generatePosBetween(posBefore, posAfter, newPos, level + 1);
//            } else {
//                throw new Error("RIP");
//            }
//        }
//    }

    public void printString() {
        for (Char c : this.struct) {
            System.out.print(c.getValue());
        }
        System.out.println("");
    }

    public String getString() {
        String string = "";

        for (Char c : this.struct) {
            string += c.getValue();
        }

        return string;
    }
}