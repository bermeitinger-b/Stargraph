package net.stargraph.core.impl.geofluent;

import net.stargraph.core.translation.TranslatorBuilder;

/**
 *
 */
public class GeofluentTranslatorBuilder extends TranslatorBuilder {

    private String key;
    private String secret;

    GeofluentTranslatorBuilder() {
    }

    GeofluentTranslatorBuilder withKey(String key) {
        this.key = key;
        return this;
    }

    GeofluentTranslatorBuilder withSecret(String secret) {
        this.secret = secret;
        return this;
    }

    public GeofluentTranslator build() {
        return new GeofluentTranslator(this);
    }

    String getKey() {
        return key;
    }

    String getSecret() {
        return secret;
    }

}
