package net.stargraph.core.query;

import com.typesafe.config.Config;
import net.stargraph.StarGraphException;
import net.stargraph.core.query.annotator.AnnotatorFactory;
import net.stargraph.core.translation.Translator;
import net.stargraph.core.translation.TranslatorFactory;

import java.lang.reflect.Constructor;

/**
 *
 */
public class TranslationFeature {

    public static Translator create(Config config) {
        try {
            String className = config.getString("translator.factory.class");
            Class<?> providerClazz = Class.forName(className);
            Constructor<?> constructor = providerClazz.getConstructors()[0];
            TranslatorFactory factory = (TranslatorFactory) constructor.newInstance(config);
            return factory.create();
        } catch (Exception e) {
            throw new StarGraphException("Can't initialize translator.", e);
        }

    }

}
