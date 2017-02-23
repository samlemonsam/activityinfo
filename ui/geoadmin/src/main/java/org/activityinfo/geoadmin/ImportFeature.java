package org.activityinfo.geoadmin;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import org.opengis.feature.type.PropertyDescriptor;

import java.util.List;
import java.util.Objects;

public class ImportFeature {
    private ImportSource source;
    private Object[] attributeValues;
    private Envelope envelope;
    private List<PropertyDescriptor> attributes;
    private Geometry geometry;


    public ImportFeature(List<PropertyDescriptor> attributes,
        Object[] attributeValues,
        Geometry geometry) {
        this.attributes = attributes;
        this.attributeValues = attributeValues;
        this.geometry = geometry;
        this.envelope = geometry.getEnvelopeInternal();
    }

    public Envelope getEnvelope() {
        return envelope;
    }

    public String getAttributeStringValue(PropertyDescriptor descriptor) {
        return getAttributeStringValue(attributes.indexOf(descriptor));
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public Object[] getAttributeValues() {
        return attributeValues;
    }

    public Object getAttributeValue(int attributeIndex) {
        return attributeValues[attributeIndex];
    }

    public String getAttributeStringValue(int attributeIndex) {
        if (attributeValues[attributeIndex] == null) {
            return null;
        } else {
            return attributeValues[attributeIndex].toString();
        }
    }

    public double similarity(String name) {
        double nameSimilarity = 0;
        for (int attributeIndex = 0; attributeIndex != attributeValues.length; ++attributeIndex) {
            Object value = attributeValues[attributeIndex];
            if (value != null) {
                nameSimilarity = Math.max(nameSimilarity, PlaceNames.similarity(name, value.toString()));
            }
        }
        return nameSimilarity;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i != attributeValues.length; ++i) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(attributeValues[i]);
        }
        return sb.toString();
    }

  public double matchCode(String code) {
    for (int attributeIndex = 0; attributeIndex != attributeValues.length; ++attributeIndex) {
      Object value = attributeValues[attributeIndex];
      if (Objects.equals(value, code)) {
        return 1;
      }
    }
    return 0;
  }


}
