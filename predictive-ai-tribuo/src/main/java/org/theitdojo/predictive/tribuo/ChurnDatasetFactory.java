package org.theitdojo.predictive.tribuo;

import org.theitdojo.predictive.core.Feature;
import org.theitdojo.predictive.core.ResourceFiles;
import org.tribuo.DataSource;
import org.tribuo.Dataset;
import org.tribuo.MutableDataset;
import org.tribuo.classification.Label;
import org.tribuo.classification.LabelFactory;
import org.tribuo.data.columnar.FieldProcessor;
import org.tribuo.data.columnar.RowProcessor;
import org.tribuo.data.columnar.processors.field.DoubleFieldProcessor;
import org.tribuo.data.columnar.processors.field.IdentityProcessor;
import org.tribuo.data.columnar.processors.response.FieldResponseProcessor;
import org.tribuo.data.csv.CSVDataSource;
import org.tribuo.evaluation.TrainTestSplitter;

import java.nio.file.Path;
import java.util.HashMap;

public final class ChurnDatasetFactory {

    private final String csvClasspath;
    private final long splitSeed;
    private final double trainFraction;

    public ChurnDatasetFactory(String csvClasspath, double trainFraction, long splitSeed) {
        this.csvClasspath = csvClasspath;
        this.trainFraction = trainFraction;
        this.splitSeed = splitSeed;
    }

    public DataSource<Label> buildDataSource() throws Exception {
        var labelFactory = new LabelFactory();
        var fieldProcessors = new HashMap<String, FieldProcessor>();

        for (Feature f : Feature.values()) {
            if (f.type() == Feature.Type.NUMERIC) {
                fieldProcessors.put(f.key(), new DoubleFieldProcessor(f.key(), true));
            } else {
                fieldProcessors.put(f.key(), new IdentityProcessor(f.key()));
            }
        }

        var responseProcessor = new FieldResponseProcessor<>("Churn", "No", labelFactory);
        var rowProcessor = new RowProcessor<>(responseProcessor, fieldProcessors);

        Path csvPath = ResourceFiles.copyToTempFile(csvClasspath, ".csv");

        return new CSVDataSource<>(csvPath, rowProcessor, true);
    }

    public Split split() throws Exception {
        DataSource<Label> source = buildDataSource();

        var splitter = new TrainTestSplitter<>(source, trainFraction, splitSeed);
        var trainSet = new MutableDataset<>(splitter.getTrain());
        var testSet = new MutableDataset<>(splitter.getTest());

        // Weight positive class for recall.
        trainSet.forEach(ex -> {
            if (ex.getOutput().getLabel().equalsIgnoreCase("Yes")) {
                ex.setWeight(4.0f);
            }
        });

        return new Split(source, trainSet, testSet);
    }

    public record Split(DataSource<Label> source, Dataset<Label> train, Dataset<Label> test) {}
}
