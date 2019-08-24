package com.mergebase.push0ver;

import javax.annotation.Nullable;

/**
 * A "Version" object that implements Comparable in a way that tends to match what humans expect
 * (e.g., "1.2" &lt; "1.2.3alpha99" &lt; "1.2.3beta3" &lt; "1.2.3rc1" &lt; "1.2.3" &lt; "1.2.11" ).
 * <p>
 * Developed with an empirical sample of ~20,000 distinct version numbers extracted from Maven-Central and Debian.
 */
public class Version implements Comparable<Version> {
    private final String version;
    private final String[] split;

    public Version(final String version) {
        String v = version != null ? version.trim() : "";

        // Special logic for versions that start with 'v' e.g., 'v1.2.3'
        if (v.length() > 1) {
            char c = v.charAt(0);
            char d = v.charAt(1);
            if (c == 'v' || c == 'V') {
                if (d >= '0' && d <= '9') {
                    v = v.substring(1);
                }
            }
        }

        this.version = version.trim();
        String[] v1 = v.trim().split("\\.");
        String[] v2 = new String[v1.length + 1];
        System.arraycopy(v1, 0, v2, 0, v1.length);
        v2[v2.length - 1] = ""; // very important: append empty-string to version split.
        this.split = v2;
    }

    public String toString() {
        return version;
    }

    String[] getSplit() {
        return split;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Version && this.compareTo((Version) obj) == 0;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public boolean isSnapshot() {
        return this.version.toUpperCase().contains("-SNAPSHOT");
    }

    public int compareTo(@Nullable Version v) {
        return VersionComparators.COMPARE_VERSIONS.compare(this, v);
    }
}
