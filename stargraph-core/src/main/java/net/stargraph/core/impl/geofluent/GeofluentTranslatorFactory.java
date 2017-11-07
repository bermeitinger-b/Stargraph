package net.stargraph.core.impl.geofluent;

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


import com.typesafe.config.Config;
import net.stargraph.core.translation.Translator;
import net.stargraph.core.translation.TranslatorFactory;

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
        return new GeofluentTranslatorBuilder()
                .withKey(key)
                .withSecret(secret)
                .build();
    }

}
