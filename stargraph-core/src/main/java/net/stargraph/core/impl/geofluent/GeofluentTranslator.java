package net.stargraph.core.impl.geofluent;

import io.redlink.ssix.geofluent.GeoFluentClient;
import net.stargraph.StarGraphException;
import net.stargraph.UnsupportedLanguageException;
import net.stargraph.core.translation.TranslatableTuple;
import net.stargraph.core.translation.Translator;
import net.stargraph.query.Language;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class GeofluentTranslator extends Translator {

    private static Map<TranslatableTuple, GeofluentTranslationTuple> possibleTranslations = new HashMap<>();

    static {
        possibleTranslations.put(new TranslatableTuple(Language.CA, Language.EN), new GeofluentTranslationTuple("ca-es", "en-xn"));
        possibleTranslations.put(new TranslatableTuple(Language.DA, Language.EN), new GeofluentTranslationTuple("da-dk", "en-xn"));
        possibleTranslations.put(new TranslatableTuple(Language.DE, Language.EN), new GeofluentTranslationTuple("de-de", "en-xn"));
        possibleTranslations.put(new TranslatableTuple(Language.EN, Language.DE), new GeofluentTranslationTuple("en-xn", "de-de"));
        possibleTranslations.put(new TranslatableTuple(Language.EN, Language.ES), new GeofluentTranslationTuple("en-xn", "es-xn"));
        possibleTranslations.put(new TranslatableTuple(Language.EN, Language.FI), new GeofluentTranslationTuple("en", "fi-fi"));
        possibleTranslations.put(new TranslatableTuple(Language.EN, Language.FR), new GeofluentTranslationTuple("en-xn", "fr-xn"));
        possibleTranslations.put(new TranslatableTuple(Language.EN, Language.IT), new GeofluentTranslationTuple("en-xn", "it-it"));
        possibleTranslations.put(new TranslatableTuple(Language.EN, Language.NL), new GeofluentTranslationTuple("en", "nl-be"));
        possibleTranslations.put(new TranslatableTuple(Language.ES, Language.EN), new GeofluentTranslationTuple("es-xn", "en-xn"));
        possibleTranslations.put(new TranslatableTuple(Language.ET, Language.EN), new GeofluentTranslationTuple("et-ee", "en-xn"));
        possibleTranslations.put(new TranslatableTuple(Language.FI, Language.EN), new GeofluentTranslationTuple("fi-fi", "en"));
        possibleTranslations.put(new TranslatableTuple(Language.FR, Language.EN), new GeofluentTranslationTuple("fr-xn", "en-xn"));
        possibleTranslations.put(new TranslatableTuple(Language.GL, Language.EN), new GeofluentTranslationTuple("gl", "en-xn"));
        possibleTranslations.put(new TranslatableTuple(Language.HU, Language.EN), new GeofluentTranslationTuple("hu-hu", "en-xn"));
        possibleTranslations.put(new TranslatableTuple(Language.IS, Language.EN), new GeofluentTranslationTuple("is-is", "en-xn"));
        possibleTranslations.put(new TranslatableTuple(Language.IT, Language.EN), new GeofluentTranslationTuple("it-it", "en-xn"));
        possibleTranslations.put(new TranslatableTuple(Language.LT, Language.EN), new GeofluentTranslationTuple("lt-lt", "en-xn"));
        possibleTranslations.put(new TranslatableTuple(Language.NB, Language.EN), new GeofluentTranslationTuple("nb-no", "en-xn"));
        possibleTranslations.put(new TranslatableTuple(Language.NL, Language.EN), new GeofluentTranslationTuple("nl-nl", "en-xn"));
        possibleTranslations.put(new TranslatableTuple(Language.PL, Language.EN), new GeofluentTranslationTuple("pl-pl", "en-xn"));
        possibleTranslations.put(new TranslatableTuple(Language.PT, Language.EN), new GeofluentTranslationTuple("pt-pt", "en-xn"));
        possibleTranslations.put(new TranslatableTuple(Language.RO, Language.EN), new GeofluentTranslationTuple("ro-ro", "en-xn"));
        possibleTranslations.put(new TranslatableTuple(Language.SK, Language.EN), new GeofluentTranslationTuple("sk-sk", "en-xn"));
        possibleTranslations.put(new TranslatableTuple(Language.SL, Language.EN), new GeofluentTranslationTuple("sl-si", "en-xn"));
        possibleTranslations.put(new TranslatableTuple(Language.SV, Language.EN), new GeofluentTranslationTuple("sv-se", "en-xn"));
        possibleTranslations.put(new TranslatableTuple(Language.UK, Language.EN), new GeofluentTranslationTuple("uk-ua", "en-xn"));
    }

    private GeoFluentClient geoFluentClient;

    GeofluentTranslator(GeofluentTranslatorBuilder builder) {
        this.geoFluentClient = new GeoFluentClient(builder.getKey(), builder.getSecret());
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
        GeofluentTranslationTuple fromToTarget = possibleTranslations.get(new TranslatableTuple(from, target));

        if (fromToTarget == null) {
            throw new UnsupportedLanguageException(target);
        }

        String translated;
        try {
            translated = this.geoFluentClient.translate(text, fromToTarget.getFrom(), fromToTarget.getTarget());
        } catch (IOException e) {
            logger.error(marker, "IOException when translating '{}' from {} ({}) to {} ({})",
                    text, from, fromToTarget.getFrom(), target, fromToTarget.getTarget());
            throw new StarGraphException(e);
        } catch (URISyntaxException e) {
            logger.error(marker, "Could not send translation request to service.", e);
            throw new StarGraphException(e);
        }

        return translated;
    }

    @Override
    protected List<Language> doGetPossibleTargetLanguages(Language sourceLanguage) {
        return possibleTranslations
                .keySet()
                .parallelStream()
                .filter(f -> f.getFrom().equals(sourceLanguage))
                .map(TranslatableTuple::getTarget)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    protected List<Language> doGetPossibleSourceLanguages(Language targetLanguage) {
        return possibleTranslations
                .keySet()
                .parallelStream()
                .filter(f -> f.getTarget().equals(targetLanguage))
                .map(TranslatableTuple::getFrom)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
