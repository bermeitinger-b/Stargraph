package net.stargraph.core.impl.geofluent;

import com.typesafe.config.Config;
import io.redlink.ssix.geofluent.GeoFluentClient;
import net.stargraph.StarGraphException;
import net.stargraph.UnsupportedLanguageException;
import net.stargraph.core.Translator;
import net.stargraph.query.Language;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
public class GeofluentTranslator extends Translator {

    private static Map<Language[], String[]> possibleTranslations = new HashMap<>();

    static {
        possibleTranslations.put(new Language[]{Language.CA, Language.EN}, new String[]{"ca-es", "en-xn"});
        possibleTranslations.put(new Language[]{Language.DA, Language.EN}, new String[]{"da-dk", "en-xn"});
        possibleTranslations.put(new Language[]{Language.DE, Language.EN}, new String[]{"de-de", "en-xn"});
        possibleTranslations.put(new Language[]{Language.EN, Language.DE}, new String[]{"en-xn", "de-de"});
        possibleTranslations.put(new Language[]{Language.EN, Language.ES}, new String[]{"en-xn", "es-xn"});
        possibleTranslations.put(new Language[]{Language.EN, Language.FI}, new String[]{"en", "fi-fi"});
        possibleTranslations.put(new Language[]{Language.EN, Language.FR}, new String[]{"en-xn", "fr-xn"});
        possibleTranslations.put(new Language[]{Language.EN, Language.IT}, new String[]{"en-xn", "it-it"});
        possibleTranslations.put(new Language[]{Language.EN, Language.NL}, new String[]{"en", "nl-be"});
        possibleTranslations.put(new Language[]{Language.ES, Language.EN}, new String[]{"es-xn", "en-xn"});
        possibleTranslations.put(new Language[]{Language.ET, Language.EN}, new String[]{"et-ee", "en-xn"});
        possibleTranslations.put(new Language[]{Language.FI, Language.EN}, new String[]{"fi-fi", "en"});
        possibleTranslations.put(new Language[]{Language.FR, Language.EN}, new String[]{"fr-xn", "en-xn"});
        possibleTranslations.put(new Language[]{Language.GL, Language.EN}, new String[]{"gl", "en-xn"});
        possibleTranslations.put(new Language[]{Language.HU, Language.EN}, new String[]{"hu-hu", "en-xn"});
        possibleTranslations.put(new Language[]{Language.IS, Language.EN}, new String[]{"is-is", "en-xn"});
        possibleTranslations.put(new Language[]{Language.IT, Language.EN}, new String[]{"it-it", "en-xn"});
        possibleTranslations.put(new Language[]{Language.LT, Language.EN}, new String[]{"lt-lt", "en-xn"});
        possibleTranslations.put(new Language[]{Language.NB, Language.EN}, new String[]{"nb-no", "en-xn"});
        possibleTranslations.put(new Language[]{Language.NL, Language.EN}, new String[]{"nl-nl", "en-xn"});
        possibleTranslations.put(new Language[]{Language.PL, Language.EN}, new String[]{"pl-pl", "en-xn"});
        possibleTranslations.put(new Language[]{Language.PT, Language.EN}, new String[]{"pt-pt", "en-xn"});
        possibleTranslations.put(new Language[]{Language.RO, Language.EN}, new String[]{"ro-ro", "en-xn"});
        possibleTranslations.put(new Language[]{Language.SK, Language.EN}, new String[]{"sk-sk", "en-xn"});
        possibleTranslations.put(new Language[]{Language.SL, Language.EN}, new String[]{"sl-si", "en-xn"});
        possibleTranslations.put(new Language[]{Language.SV, Language.EN}, new String[]{"sv-se", "en-xn"});
        possibleTranslations.put(new Language[]{Language.UK, Language.EN}, new String[]{"uk-ua", "en-xn"});
    }

    private GeoFluentClient geoFluentClient;

    public GeofluentTranslator(Config config, Language targetLanguage) {
        this.targetLanguage = Objects.requireNonNull(targetLanguage);
        this.geoFluentClient = new GeoFluentClient(
                config.getString("translation.geofluent.client"),
                config.getString("translation.geofluent.secret")
        );
        this.geoFluentClient.init();
        try {
            logger.debug(marker, "GeofluentTranslator initialized, supported languages: {}", this.geoFluentClient.languages());
        } catch (Exception e) {
            logger.error(marker, "Could not initialize GeofluentTranslator service", e);
            throw new StarGraphException(e);
        }
    }

    @Override
    protected String doTranslate(String text, Language from, Language target) {
        String[] fromToTarget = possibleTranslations.get(new Language[]{from, target});

        if (fromToTarget == null) {
            throw new UnsupportedLanguageException(target);
        }

        String translated;
        try {
            translated = this.geoFluentClient.translate(text, fromToTarget[0], fromToTarget[1]);
        } catch (IOException e) {
            logger.error(marker, "IOException when translating '{}' from {} ({}) to {} ({})",
                    text, from, fromToTarget[0], target, fromToTarget[1]);
            throw new StarGraphException(e);
        } catch (URISyntaxException e) {
            logger.error(marker, "Could not send translation request to service.", e);
            throw new StarGraphException(e);
        }

        return translated;
    }
}
