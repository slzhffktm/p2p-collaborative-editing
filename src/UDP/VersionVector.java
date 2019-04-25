package UDP;

import java.util.ArrayList;

/**
 * vector/list of versions of sites in the distributed system
 * keeps track of the latest operation received from each site (i.e. version)
 * prevents duplicate operations
 */
public class VersionVector {
    private ArrayList<Version> versions;
    private Version localVersion;

    public ArrayList<Version> getVersions() {
        return versions;
    }

    public Version getLocalVersion() {
        return localVersion;
    }

    public VersionVector(String siteId) {
        this.versions = new ArrayList<>();
        this.localVersion = new Version(siteId);
        this.versions.add(this.localVersion);
    }

    public void increment() {
        this.localVersion.setCounter(localVersion.getCounter() + 1);
    }

    /**
     * updates vector with new version received from another site
     * if vector doesn't contain version, it's created and added to vector
     * create exceptions if need be.
     */
    public void update(Version incomingVersion) {
        Version existingVersion = null;

        for (Version v : versions) {
            if (v.getSiteId().equals(incomingVersion.getSiteId())) {
                existingVersion = v;
                break;
            }
        }

        if (existingVersion == null) {
            Version newVersion = new Version(incomingVersion.getSiteId());
            newVersion.update(incomingVersion);
            this.versions.add(newVersion);
        } else {
            existingVersion.update(incomingVersion);
        }
    }

    public boolean hasBeenApplied(Version incomingVersion) {
        Version localIncomingVersion = getVersionFromVector(incomingVersion);

        if (localIncomingVersion == null) {
            return false;
        }

        boolean isIncomingLower = incomingVersion.getCounter() <= localIncomingVersion.getCounter();
        boolean isInExceptions = localIncomingVersion.getExceptions().contains(incomingVersion.getCounter());

        return isIncomingLower && !isInExceptions;
    }

    public Version getVersionFromVector(Version incomingVersion) {
        for (Version v : versions) {
            if (v.getSiteId().equals(incomingVersion.getSiteId())) {
                return v;
            }
        }
        return null;
    }
}