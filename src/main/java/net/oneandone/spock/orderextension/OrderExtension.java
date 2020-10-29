package net.oneandone.spock.orderextension;

import org.spockframework.runtime.AbstractRunListener;
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.model.ErrorInfo;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.NodeInfo;
import org.spockframework.runtime.model.SpecInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class OrderExtension extends AbstractAnnotationDrivenExtension<Order> {

    @Override
    public void visitSpecAnnotation(Order annotation, final SpecInfo spec) {
        sortFeatures(spec, annotation);
        includeFeaturesBeforeLastIncludedFeature(spec);
        skipFeaturesAfterFirstFailingFeature(spec);
    }

    private void sortFeatures(SpecInfo spec, Order order) {
        List<FeatureInfo> allowedFeatures = spec.getAllFeatures();

        Graph graph = new Graph(filterSkippedFeatures(allowedFeatures, order)
                .stream().map(NodeInfo::getName).collect(Collectors.toSet()));

        List<FeatureInfo> features = spec.getFeatures();

        addEdgesForDefinedDependencies(graph, features);

        addLastFeatureAsPredecessorIfNothingDefined(graph, features);

        List<FeatureInfo> activeSuperFeatures = filterSkippedFeatures(spec.getSuperSpec().getAllFeatures(), order);
        addLastFeatureAsPredecessorIfNothingDefined(graph, activeSuperFeatures);

        List<String> sorted = graph.topologicalSort();

        int lastExecutionOrder = 0;
        List<String> skip = Arrays.asList(order.skip());
        for (FeatureInfo featureInfo : spec.getAllFeatures()) {
            if (skip.contains(featureInfo.getName())) {
                featureInfo.setSkipped(true);
                featureInfo.setExecutionOrder(lastExecutionOrder);
            } else {
                featureInfo.setExecutionOrder(sorted.indexOf(featureInfo.getName()));
                lastExecutionOrder = featureInfo.getExecutionOrder();
            }
        }
    }

    private static List<FeatureInfo> filterSkippedFeatures(List<FeatureInfo> activeSuperFeatures, Order order) {
        if (order.skip().length > 0) {
            List<String> skip = Arrays.asList(order.skip());
            return activeSuperFeatures.stream().filter(it -> !skip.contains(it.getName())).collect(Collectors.toList());        }
        return activeSuperFeatures;
    }

    private static void addLastFeatureAsPredecessorIfNothingDefined(Graph graph, List<FeatureInfo> features) {
        String lastFeature = null;
        for (FeatureInfo feature : features) {
            if (lastFeature != null && !graph.containsTo(feature.getName())) {
                graph.addEdge(lastFeature, feature.getName());
            }
            lastFeature = feature.getName();
        }
    }

    private static void addEdgesForDefinedDependencies(Graph graph, List<FeatureInfo> features) {
        boolean waitingForBefore = false;
        for (FeatureInfo feature : features) {
            boolean orderAnnotationFound = false;
            if (feature.getFeatureMethod().isAnnotationPresent(After.class)) {
                After after = feature.getFeatureMethod().getAnnotation(After.class);
                graph.addEdge(after.value(), feature.getName());
                waitingForBefore = true;
                orderAnnotationFound = true;
            }
            if (feature.getFeatureMethod().isAnnotationPresent(Before.class)) {
                Before before = feature.getFeatureMethod().getAnnotation(Before.class);
                graph.addEdge(feature.getName(), before.value());
                waitingForBefore = false;
                orderAnnotationFound = true;
            }
            if (!orderAnnotationFound && !waitingForBefore) {
                throw new IllegalArgumentException("Found not annotated feature '" + feature.getName() + "' in @Order spec."
                        + " This is only between @After and @Before features allowed.");
            }
        }
    }

    private static void includeFeaturesBeforeLastIncludedFeature(SpecInfo spec) {
        List<FeatureInfo> features = spec.getFeatures();
        boolean includeRemaining = false;

        for (int i = features.size() - 1; i >= 0; i--) {
            FeatureInfo feature = features.get(i);
            if (includeRemaining) feature.setExcluded(false);
            else if (!feature.isExcluded()) includeRemaining = true;
        }
    }

    private static void skipFeaturesAfterFirstFailingFeature(final SpecInfo spec) {
        spec.getBottomSpec().addListener(new AbstractRunListener() {
            @Override
            public void error(ErrorInfo error) {
                // mark all subsequent features as skipped
                List<FeatureInfo> features = new ArrayList<>(spec.getAllFeatures());
                features.sort(Comparator.comparingInt(FeatureInfo::getExecutionOrder));

                int indexOfFailedFeature = features.indexOf(error.getMethod().getFeature());
                for (int i = indexOfFailedFeature + 1; i < features.size(); i++) {
                    features.get(i).setSkipped(true);
                }
            }
        });
    }
}
