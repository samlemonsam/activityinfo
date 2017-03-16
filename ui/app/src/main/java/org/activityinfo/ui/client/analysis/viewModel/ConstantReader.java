package org.activityinfo.ui.client.analysis.viewModel;

public class ConstantReader implements DimensionReader {

    private final String value;

    public ConstantReader(String value) {
        this.value = value;
    }

    @Override
    public String read(int row) {
        return value;
    }
}
