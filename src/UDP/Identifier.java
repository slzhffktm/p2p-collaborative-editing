package UDP;

public class Identifier {
    private int digit;
    private String siteId;

    public int getDigit() {
        return digit;
    }

    public String getSiteId() {
        return siteId;
    }

    public Identifier(int digit, String siteId) {
        this.digit = digit;
        this.siteId = siteId;
    }

    public int compareTo(Identifier otherId) {
        if (this.digit < otherId.digit) {
            return -1;
        } else if (this.digit > otherId.digit) {
            return 1;
        } else {
            return this.siteId.compareTo(otherId.siteId);
        }
    }
}
