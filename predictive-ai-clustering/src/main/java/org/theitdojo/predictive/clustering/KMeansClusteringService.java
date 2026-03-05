package org.theitdojo.predictive.clustering;

import org.theitdojo.predictive.core.Customer;
import org.tribuo.Example;
import org.tribuo.Feature;
import org.tribuo.Model;
import org.tribuo.MutableDataset;
import org.tribuo.clustering.ClusterID;
import org.tribuo.clustering.ClusteringFactory;
import org.tribuo.clustering.kmeans.KMeansTrainer;
import org.tribuo.datasource.ListDataSource;
import org.tribuo.impl.ArrayExample;
import org.tribuo.math.distance.L2Distance;
import org.tribuo.provenance.SimpleDataSourceProvenance;

import java.time.OffsetDateTime;
import java.util.*;

public final class KMeansClusteringService {

    public record ClusteredPoint(double[] vector, int clusterId) {}

    public record Result(
            List<ClusteredPoint> points,
            Map<Integer, double[]> centroids
    ) {}

    private final ClusteringFactory factory = new ClusteringFactory();
    private final Random rnd;
    private final long seed;

    public KMeansClusteringService(long seed) {
        this.seed = seed;
        this.rnd = new Random(seed);
    }

    public Result cluster(List<Customer> customers, int k, int maxPoints) {
        if (k <= 1) throw new IllegalArgumentException("k must be >= 2");
        if (customers.isEmpty()) throw new IllegalArgumentException("No customers provided");

        List<Customer> sample = reservoirSample(customers, maxPoints);
        List<Example<ClusterID>> examples = new ArrayList<>(sample.size());

        for (Customer c : sample) {
            double[] v = CustomerVectorizer.vectorize(c);
            examples.add(toExample(v));
        }

        var provenance = new SimpleDataSourceProvenance(
                "Customer Segmentation",
                OffsetDateTime.now(),
                factory
        );

        var source = new ListDataSource<>(examples, factory, provenance);
        var dataset = new MutableDataset<>(source);

        KMeansTrainer trainer = new KMeansTrainer(
                k,
                100,
                new L2Distance(),
                Runtime.getRuntime().availableProcessors(),
                seed
        );

        Model<ClusterID> model = trainer.train(dataset);

        // Assign clusters
        List<ClusteredPoint> clustered = new ArrayList<>(sample.size());
        Map<Integer, List<double[]>> byCluster = new HashMap<>();

        for (Customer c : sample) {
            double[] v = CustomerVectorizer.vectorize(c);
            int cid = model.predict(toExample(v)).getOutput().getID();
            clustered.add(new ClusteredPoint(v, cid));
            byCluster.computeIfAbsent(cid, _k -> new ArrayList<>()).add(v);
        }

        // Compute centroids manually (mean vector per cluster)
        Map<Integer, double[]> centroids = new HashMap<>();
        for (var e : byCluster.entrySet()) {
            int cid = e.getKey();
            List<double[]> vectors = e.getValue();
            int dim = vectors.getFirst().length;
            double[] mean = new double[dim];

            for (double[] v : vectors) {
                for (int i = 0; i < dim; i++) mean[i] += v[i];
            }
            for (int i = 0; i < dim; i++) mean[i] /= vectors.size();

            centroids.put(cid, mean);
        }

        return new Result(clustered, centroids);
    }

    private static Example<ClusterID> toExample(double[] v) {
        List<Feature> feats = new ArrayList<>(v.length);
        var names = ClusteringFeatures.values();
        for (int i = 0; i < v.length; i++) {
            feats.add(new Feature(names[i].name(), v[i]));
        }
        return new ArrayExample<>(ClusteringFactory.UNASSIGNED_CLUSTER_ID, feats);
    }

    private List<Customer> reservoirSample(List<Customer> customers, int max) {
        if (max <= 0 || customers.size() <= max) return customers;
        List<Customer> reservoir = new ArrayList<>(max);
        for (int i = 0; i < customers.size(); i++) {
            Customer c = customers.get(i);
            if (i < max) {
                reservoir.add(c);
            } else {
                int j = rnd.nextInt(i + 1);
                if (j < max) reservoir.set(j, c);
            }
        }
        return reservoir;
    }
}
