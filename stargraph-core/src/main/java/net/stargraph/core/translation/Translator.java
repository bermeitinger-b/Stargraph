package net.stargraph.core.translation;

import net.stargraph.StarGraphException;
import net.stargraph.UnsupportedLanguageException;
import net.stargraph.query.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 *
 */
public abstract class Translator {

    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected Marker marker = MarkerFactory.getMarker("translator");

    protected abstract String doTranslate(String text, Language from, Language target);

    public final String translate(String text, Language from, Language target) {
        logger.debug(marker, "Translating '{}' from {} to {}", text, from, target);
        try {
            return doTranslate(text, from, target);
        } catch (UnsupportedLanguageException e) {
            throw e;
        } catch (Exception e) {
            logger.error(marker, "Error caught during translating the text '{}' from {} to {}", text, from, target);
            throw new StarGraphException(e);
        }
    }
}
