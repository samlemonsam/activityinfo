/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.model.query;

import com.google.gwt.core.shared.GwtIncompatible;
import org.activityinfo.model.util.HeapsortColumn;

import java.io.*;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Simple Array of String values
 */
public class StringArrayColumnView implements ColumnView, Serializable {

    private static final long serialVersionUID = 1L;

    private static final int FORMAT_UNCOMPRESSED = 0;
    private static final int FORMAT_COMPRESSED = 1;

    private String[] values;

    protected StringArrayColumnView() {
    }

    public StringArrayColumnView(String[] values) {
        this.values = values;
    }

    public StringArrayColumnView(List<String> values) {
        this.values = values.toArray(new String[values.size()]);
    }

    @Override
    public ColumnType getType() {
        return ColumnType.STRING;
    }

    @Override
    public int numRows() {
        return values.length;
    }

    @Override
    public Object get(int row) {
        return values[row];
    }

    @Override
    public double getDouble(int row) {
        return Double.NaN;
    }

    @Override
    public String getString(int row) {
        return values[row];
    }

    @Override
    public int getBoolean(int row) {
        return NA;
    }

    @Override
    public boolean isMissing(int row) {
        return values[row] == null;
    }

    @Override
    public ColumnView select(int[] selectedRows) {
        String[] filteredValues = new String[selectedRows.length];
        for (int i = 0; i < filteredValues.length; i++) {
            int selectedRow = selectedRows[i];
            if(selectedRow != -1) {
                filteredValues[i] = this.values[selectedRow];
            }
        }
        return new StringArrayColumnView(filteredValues);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < Math.min(values.length, 5); i++) {
            if(i > 0) {
                sb.append(", ");
            }
            if(values[i] == null) {
                sb.append("NULL");
            } else {
                sb.append("'");
                sb.append(values[i]);
                sb.append("'");
            }
        }
        if(values.length > 5) {
            sb.append("... length=").append(values.length);
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public int[] order(int[] sortVector, SortModel.Dir direction, int[] range) {
        int numRows = values.length;
        if (range == null || range.length == numRows) {
            HeapsortColumn.heapsortString(values, sortVector, numRows, direction == SortModel.Dir.ASC);
        } else {
            HeapsortColumn.heapsortString(values, sortVector, range.length, range, direction == SortModel.Dir.ASC);
        }
        return sortVector;
    }

    @GwtIncompatible
    private void writeObject(ObjectOutputStream out) throws IOException {
        writeCompressed(out);
    }

    @GwtIncompatible
    private void writeCompressed(ObjectOutputStream out) throws IOException {
        DataOutputStream daos = new DataOutputStream(out);
        daos.writeInt(FORMAT_COMPRESSED);

        GZIPOutputStream gzout = new GZIPOutputStream(out);
        DataOutputStream gzDataOut = new DataOutputStream(gzout);
        writeArray(gzDataOut);
        gzout.finish();
    }

    @GwtIncompatible
    private void writeArray(DataOutputStream daos) throws IOException {
        daos.writeInt(values.length);
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            if(value == null) {
                daos.writeUTF("");
            } else {
                daos.writeUTF(value);
            }
        }
    }

    @GwtIncompatible
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        DataInputStream dis = new DataInputStream(in);
        int format = dis.readInt();
        if(format == FORMAT_COMPRESSED) {
            readCompressed(dis);
        }
    }

    @GwtIncompatible
    private void readCompressed(DataInputStream in) throws IOException {

        GZIPInputStream gzin = new GZIPInputStream(in);
        DataInputStream gzDataIn = new DataInputStream(gzin);

        readArray(gzDataIn);
    }

    @GwtIncompatible
    private void readArray(DataInputStream input) throws IOException {
        int count = input.readInt();
        this.values = new String[count];

        for (int i = 0; i < count; i++) {
            String s = input.readUTF();
            if(!s.isEmpty()) {
                values[i] = s;
            }
        }
    }
}
