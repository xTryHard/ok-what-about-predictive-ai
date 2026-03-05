package org.theitdojo.predictive.tribuo;

import org.tribuo.DataSource;
import org.tribuo.MutableDataset;
import org.tribuo.classification.Label;
import org.tribuo.classification.LabelInfo;

public final class EdaService {

    public void printFullSourceProfile(DataSource<Label> source) {
        System.out.println("\n=== EDA: Full Data Source Profile ===");

        var fullDataset = new MutableDataset<>(source);
        LabelInfo info = (LabelInfo) fullDataset.getOutputInfo();

        long totalRecords = fullDataset.size();
        System.out.println("Total Records Found: " + totalRecords);

        for (Label label : info.getDomain()) {
            long count = info.getLabelCount(label);
            double pct = (count / (double) totalRecords) * 100;
            System.out.printf("  Label [%-5s] | Count: %-5d | Share: %.2f%%%n",
                    label.getLabel(), count, pct);
        }
    }
}
