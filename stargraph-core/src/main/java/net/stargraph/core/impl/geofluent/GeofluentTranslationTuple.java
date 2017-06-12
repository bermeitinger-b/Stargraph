package net.stargraph.core.impl.geofluent;

import java.util.Objects;

/**
 *
 */
public class GeofluentTranslationTuple {
    private final String from;
    private final String target;

    GeofluentTranslationTuple(String from, String target) {
        this.from = Objects.requireNonNull(from);
        this.target = Objects.requireNonNull(target);
    }

    public String getFrom() {
        return from;
    }

    String getTarget() {
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeofluentTranslationTuple that = (GeofluentTranslationTuple) o;
        return Objects.equals(getFrom(), that.getFrom()) &&
                Objects.equals(getTarget(), that.getTarget());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFrom(), getTarget());
    }
}
