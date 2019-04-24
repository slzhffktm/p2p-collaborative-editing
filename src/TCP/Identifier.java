package TCP;

public class Identifier {
    private int digit;
    private int siteId;

    public int getDigit() {
        return digit;
    }

    public void setDigit(int digit) {
        this.digit = digit;
    }

    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    public Identifier(int digit, int siteId) {
        this.digit = digit;
        this.siteId = siteId;
    }

    public int compareTo(Identifier otherId) {
        if (this.digit < otherId.digit) {
            return -1;
        } else if (this.digit > otherId.digit) {
            return 1;
        } else {
            if (this.siteId < otherId.siteId) {
                return -1;
            } else if (this.siteId > otherId.siteId) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
