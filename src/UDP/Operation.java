package UDP;

public class Operation {
    private Char c;
    private char type;

    public Operation(Char c, char type) {
        this.c = c;
        this.type = type;
    }

    public Char getC() {
        return c;
    }

    public void setC(Char c) {
        this.c = c;
    }

    public char getType() {
        return type;
    }

    public void setType(char type) {
        this.type = type;
    }
}
