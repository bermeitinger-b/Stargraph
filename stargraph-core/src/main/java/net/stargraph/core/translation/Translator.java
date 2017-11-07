package net.stargraph.core.translation;

        /*-
         * ==========================License-Start=============================
         * Stargraph
         * --------------------------------------------------------------------
         * Copyright (C) 2017 Lambda^3
         * --------------------------------------------------------------------
         * Permission is hereby granted, free of charge, to any person obtaining a copy
         * of this software and associated documentation files (the "Software"), to deal
         * in the Software without restriction, including without limitation the rights
         * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
         * copies of the Software, and to permit persons to whom the Software is
         * furnished to do so, subject to the following conditions:
         *
         * The above copyright notice and this permission notice shall be included in
         * all copies or substantial portions of the Software.
         *
         * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
         * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
         * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
         * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
         * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
         * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
         * THE SOFTWARE.
         * ==========================License-End===============================
         */


import net.stargraph.StarGraphException;
import net.stargraph.UnsupportedLanguageException;
import net.stargraph.query.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.List;

public abstract class Translator {

    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected Marker marker = MarkerFactory.getMarker("translator");

    protected abstract String doTranslate(String text, Language from, Language target);
    protected abstract List<Language> doGetPossibleTargetLanguages(Language sourceLanguage);
    protected abstract List<Language> doGetPossibleSourceLanguages(Language targetLanguage);

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

    public final List<Language> getPossibleTargetLanguages(Language sourceLanguage) {
        logger.trace(marker, "Getting possible target languages for source language '{}'", sourceLanguage);
        try {
            return doGetPossibleTargetLanguages(sourceLanguage);
        } catch (UnsupportedLanguageException e) {
            throw e;
        } catch (Exception e) {
            logger.error(marker, "Error caught during getting possible target languages for language '{}'", sourceLanguage);
            throw new StarGraphException(e);
        }
    }

    public final List<Language> getPossibleSourceLanguages(Language targetLanguage) {
        logger.trace(marker, "Getting possible target languages for source language '{}'", targetLanguage);
        try {
            return doGetPossibleSourceLanguages(targetLanguage);
        } catch (UnsupportedLanguageException e) {
            throw e;
        } catch (Exception e) {
            logger.error(marker, "Error caught during getting possible source languages for language '{}'", targetLanguage);
            throw new StarGraphException(e);
        }
    }
}
