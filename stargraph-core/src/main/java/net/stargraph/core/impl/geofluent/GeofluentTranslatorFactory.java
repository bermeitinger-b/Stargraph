package net.stargraph.core.impl.geofluent;

import com.typesafe.config.Config;
import net.stargraph.core.translation.Translator;
import net.stargraph.core.translation.TranslatorFactory;

/**
 *
 */
public class GeofluentTranslatorFactory extends TranslatorFactory {

    private final String key;
    private final String secret;

    public GeofluentTranslatorFactory(Config config) {
        super(config);
        key = this.config.getString("translator.geofluent.key");
        secret = this.config.getString("translator.geofluent.secret");
    }

    @Override
    public Translator create() {
        return (new GeofluentTranslatorBuilder()
                .withKey(key)
                .withSecret(secret)
                .build());
    }

}
