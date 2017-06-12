package net.stargraph.core.translation;

import com.typesafe.config.Config;

import java.util.Objects;

/**
 *
 */
public abstract class TranslatorFactory {
    protected Config config;

    public TranslatorFactory(Config config) {
        this.config = Objects.requireNonNull(config);
    }

    public abstract Translator create();
}
