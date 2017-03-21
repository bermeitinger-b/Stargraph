package net.stargraph.core;

/*-
 * ==========================License-Start=============================
 * stargraph-core
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

import com.typesafe.config.*;
import net.stargraph.ModelUtils;
import net.stargraph.StarGraphException;
import net.stargraph.core.impl.elastic.ElasticEntitySearcher;
import net.stargraph.core.impl.elastic.ElasticSearcher;
import net.stargraph.core.impl.hdt.HDTModelFactory;
import net.stargraph.core.index.Indexer;
import net.stargraph.core.index.IndexerFactory;
import net.stargraph.core.processors.Processors;
import net.stargraph.core.search.BaseSearcher;
import net.stargraph.core.search.EntitySearcher;
import net.stargraph.core.search.Searcher;
import net.stargraph.data.DataProvider;
import net.stargraph.data.DataProviderFactory;
import net.stargraph.data.processor.Holder;
import net.stargraph.data.processor.Processor;
import net.stargraph.data.processor.ProcessorChain;
import net.stargraph.model.BuiltInModel;
import net.stargraph.model.KBId;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The Stargraph database core implementation.
 */
public final class Stargraph {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private Marker marker = MarkerFactory.getMarker("core");
    private Config mainConfig;
    private ConcurrentMap<KBId, Indexer> indexers;
    private ConcurrentMap<KBId, Searcher> searchers;
    private IndexerFactory indexerFactory;
    private GraphModelFactory modelFactory;

    public Stargraph() {
        this(ConfigFactory.load().getConfig("stargraph"), true);
    }

    public Stargraph(Config cfg, boolean initialize) {
        this.mainConfig = Objects.requireNonNull(cfg);
        logger.trace(marker, "Configuration: {}", ModelUtils.toStr(mainConfig));
        logger.info(marker, "{}, {} ({})",
                Version.getCodeName(), Version.getBuildVersion(), Version.getBuildNumber());
        this.indexers = new ConcurrentHashMap<>();
        this.searchers = new ConcurrentHashMap<>();

        setIndexerFactory(createIndexerFactory());
        setModelFactory(new HDTModelFactory(this));

        if (initialize) {
            initialize();
        }
    }

    @SuppressWarnings("unchecked")
    public Class<Serializable> getModelClass(String modelName) {
        // This should change to support user's models.

        for (BuiltInModel entry : BuiltInModel.values()) {
            if (entry.modelId.equals(modelName)) {
                return entry.cls;
            }
        }

        throw new StarGraphException("No Class registered for model: '" + modelName + "'");
    }

    public EntitySearcher createEntitySearcher() {
        return new ElasticEntitySearcher(this);
    }

    public Model getModel(String dbId) {
        return modelFactory.getModel(dbId);
    }

    public Config getConfig() {
        return mainConfig;
    }

    public Config getKBConfig(String dbId) {
        return mainConfig.getConfig(String.format("kb.%s", dbId));
    }

    public Config getKBConfig(KBId kbId) {
        return mainConfig.getConfig(kbId.getKBPath());
    }

    public Config getTypeConfig(KBId kbId) {
        return mainConfig.getConfig(kbId.getTypePath());
    }

    public Set<KBId> getKBs() {
        return indexers.keySet();
    }

    public Indexer getIndexer(KBId kbId) {
        if (indexers.keySet().contains(kbId))
            return indexers.get(kbId);
        throw new StarGraphException("Indexer not found nor initialized: " + kbId);
    }

    public Searcher getSearcher(KBId kbId) {
        if (searchers.keySet().contains(kbId))
            return searchers.get(kbId);
        throw new StarGraphException("Indexer not found nor initialized: " + kbId);
    }

    public void setIndexerFactory(IndexerFactory indexerFactory) {
        this.indexerFactory = Objects.requireNonNull(indexerFactory);
    }

    public void setModelFactory(GraphModelFactory modelFactory) {
        this.modelFactory = Objects.requireNonNull(modelFactory);
    }

    public ProcessorChain createProcessorChain(KBId kbId) {
        List<? extends Config> processorsCfg = getProcessorsCfg(kbId);
        if (processorsCfg != null && processorsCfg.size() != 0) {
            List<Processor> processors = new ArrayList<>();
            processorsCfg.forEach(config -> processors.add(Processors.create(config)));
            ProcessorChain chain = new ProcessorChain(processors);
            logger.info(marker, "processors = {}", chain);
            return chain;
        }
        return null;
    }

    public DataProvider<? extends Holder> createDataProvider(KBId kbId) {
        DataProviderFactory factory;

        try {
            String className = getDataProviderCfg(kbId).getString("class");
            Class<?> providerClazz = Class.forName(className);
            Constructor[] constructors = providerClazz.getConstructors();

            if (BaseDataProviderFactory.class.isAssignableFrom(providerClazz)) {
                // It's our internal factory hence we inject the core dependency.
                factory = (DataProviderFactory) constructors[0].newInstance(this);
            } else {
                // This should be a user factory without constructor.
                // API user should rely on configuration or other means to initialize.
                // See TestDataProviderFactory as an example
                factory = (DataProviderFactory) providerClazz.newInstance();
            }

            DataProvider<? extends Holder> provider = factory.create(kbId);

            if (provider == null) {
                throw new IllegalStateException("DataProvider not created!");
            }

            logger.info(marker, "Creating {} data provider", kbId);
            return provider;
        } catch (Exception e) {
            throw new StarGraphException("Fail to initialize data provider: " + kbId, e);
        }
    }

    public final void initialize() {
        logger.info(marker, "Indexer factory : '{}'", indexerFactory.getClass());
        this.initializeKB();
    }

    public final void terminate() {
        //todo: shutdown procedure
    }

    private void initializeKB() {
        ConfigObject kbObj;
        try {
            kbObj = this.mainConfig.getObject("kb");
        } catch (ConfigException e) {
            throw new StarGraphException("No KB configured.", e);
        }

        for (Map.Entry<String, ConfigValue> kbEntry : kbObj.entrySet()) {
            ConfigObject typeObj = this.mainConfig.getObject(String.format("kb.%s.model", kbEntry.getKey()));
            for (Map.Entry<String, ConfigValue> typeEntry : typeObj.entrySet()) {
                KBId kbId = KBId.of(kbEntry.getKey(), typeEntry.getKey());
                Indexer indexer = this.indexerFactory.create(kbId, this);
                indexer.start();
                indexers.put(kbId, indexer);
                BaseSearcher searcher = new ElasticSearcher(kbId, this);
                searcher.start();
                searchers.put(kbId, searcher);
            }
        }

        if (searchers.isEmpty()) {
            logger.warn(marker, "No KBs configured.");
        }
    }

    private List<? extends Config> getProcessorsCfg(KBId kbId) {
        String path = String.format("%s.processors", kbId.getTypePath());
        if (mainConfig.hasPath(path)) {
            return mainConfig.getConfigList(path);
        }
        return null;
    }

    private Config getDataProviderCfg(KBId kbId) {
        String path = String.format("%s.provider", kbId.getTypePath());
        return mainConfig.getConfig(path);
    }


    private IndexerFactory createIndexerFactory() {
        try {
            String className = getConfig().getString("indexer.factory.class");
            Class<?> providerClazz = Class.forName(className);
            Constructor<?> constructor = providerClazz.getConstructors()[0];
            return (IndexerFactory) constructor.newInstance();
        } catch (Exception e) {
            throw new StarGraphException("Can't initialize indexers.", e);
        }
    }
}