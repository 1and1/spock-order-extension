package net.oneandone.spock.orderextension

import org.spockframework.runtime.AbstractRunListener
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.model.ErrorInfo
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.SpecInfo

class OrderExtension extends AbstractAnnotationDrivenExtension<Order> {

    void visitSpecAnnotation(Order annotation, final SpecInfo spec) {
        sortFeatures(spec, annotation)
        includeFeaturesBeforeLastIncludedFeature(spec)
        skipFeaturesAfterFirstFailingFeature(spec)
    }

    private void sortFeatures(SpecInfo spec, Order order) {
        List<FeatureInfo> allowedFeatures = spec.allFeatures

        Graph graph = new Graph(filterSkippedFeatures(allowedFeatures, order)*.name.toSet())
        List<FeatureInfo> features = spec.features

        addEdgesForDefinedDependencies(features, graph)

        addLastFeatureAsPredecessorIfNothingDefined(graph, features)

        List<FeatureInfo> activeSuperFeatures = filterSkippedFeatures(spec.superSpec.allFeatures, order)
        addLastFeatureAsPredecessorIfNothingDefined(graph, activeSuperFeatures)

        List<String> sorted = graph.topologicalSort()

        int lastExecutionOrder = 0
        spec.allFeatures.forEach {
            if (order.skip().contains(it.name)) {
                it.skipped = true
                it.executionOrder = lastExecutionOrder
            } else {
                it.executionOrder = sorted.indexOf(it.name)
                lastExecutionOrder = it.executionOrder
            }
        }
    }

    private static List<FeatureInfo> filterSkippedFeatures(List<FeatureInfo> activeSuperFeatures, Order order) {
        if (order.skip()) {
            return activeSuperFeatures.findAll { !order.skip().contains(it.name) }
        }
        return activeSuperFeatures
    }

    private static void addLastFeatureAsPredecessorIfNothingDefined(graph, List<FeatureInfo> features) {
        String lastFeature = null
        features.forEach {
            if (lastFeature && !graph.containsTo(it.name)) {
                graph.addEdge(lastFeature, it.name)
            }
            lastFeature = it.name
        }
    }

    private static void addEdgesForDefinedDependencies(List<FeatureInfo> features, graph) {
        boolean waitingForBefore = false
        features.forEach {
            if (it.featureMethod.isAnnotationPresent(After)) {
                After after = it.featureMethod.getAnnotation(After)
                graph.addEdge(after.value(), it.name)
                waitingForBefore = true
            } else if (it.featureMethod.isAnnotationPresent(Between)) {
                if (waitingForBefore) {
                    throw new IllegalArgumentException("'${it.name}' is annotated with @Between, but @Before was expected. @After must be followed by @Before to indicate the next base feature to use.")
                }
                Between between = it.featureMethod.getAnnotation(Between)
                graph.addEdge(between.value()[0], it.name)
                graph.addEdge(it.name, between.value()[1])
            } else if (it.featureMethod.isAnnotationPresent(Before)) {
                Before before = it.featureMethod.getAnnotation(Before)
                graph.addEdge(it.name, before.value())
                waitingForBefore = false
            } else if (!waitingForBefore) {
                throw new IllegalArgumentException("Found not annotated feature '$it.name' in @Order spec. This is only between @After and @Before features.")
            }
        }
    }

    private static void includeFeaturesBeforeLastIncludedFeature(SpecInfo spec) {
        List<FeatureInfo> features = spec.getFeatures()
        boolean includeRemaining = false

        for (int i = features.size() - 1; i >= 0; i--) {
            FeatureInfo feature = features.get(i)
            if (includeRemaining) feature.setExcluded(false)
            else if (!feature.isExcluded()) includeRemaining = true
        }
    }

    private static void skipFeaturesAfterFirstFailingFeature(final SpecInfo spec) {
        spec.getBottomSpec().addListener(new AbstractRunListener() {
            @Override
            void error(ErrorInfo error) {
                // mark all subsequent features as skipped
                List<FeatureInfo> features = spec.allFeatures.sort { it.executionOrder }
                int indexOfFailedFeature = features.indexOf(error.getMethod().getFeature())
                for (int i = indexOfFailedFeature + 1; i < features.size(); i++) {
                    features[i].skipped = true
                }
            }
        })
    }
}
