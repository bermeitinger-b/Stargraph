package net.stargraph.core.translation;

import net.stargraph.query.Language;

import java.util.Objects;

/**
 *
 */
public class TranslatableTuple {
    private final Language from;
    private final Language target;

    public TranslatableTuple(Language from, Language target) {
        this.from = Objects.requireNonNull(from);
        this.target = Objects.requireNonNull(target);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranslatableTuple that = (TranslatableTuple) o;
        return from == that.from &&
                target == that.target;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, target);
    }
}
