package org.theitdojo.predictive.tribuo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.oracle.labs.mlrg.olcut.config.json.JsonProvenanceModule;
import org.tribuo.Dataset;
import org.tribuo.Model;
import org.tribuo.Trainer;
import org.tribuo.classification.Label;

import java.io.File;
import java.util.Map;

public final class ProvenanceExporter {

    private final ObjectMapper mapper;

    public ProvenanceExporter() {
        this.mapper = new ObjectMapper()
                .registerModule(new JsonProvenanceModule())
                .registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void exportAll(Map<String, Trainer<Label>> trainers, Dataset<Label> trainSet, File outputDir) {
        System.out.println("\n=== Exporting Model Provenance ===");

        if (!outputDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            outputDir.mkdirs();
        }

        trainers.forEach((name, trainer) -> {
            try {
                Model<Label> model = trainer.train(trainSet);
                String fileName = name.replace(" ", "_").toLowerCase() + "_provenance.json";

                File outputFile = new File(outputDir, fileName);
                mapper.writeValue(outputFile, model.getProvenance());

                System.out.println("Exported: " + outputFile.getPath());
            } catch (Exception e) {
                System.err.println("Failed: " + name + " -> " + e.getMessage());
            }
        });
    }
}
