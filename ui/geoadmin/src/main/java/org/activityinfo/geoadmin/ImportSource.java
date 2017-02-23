package org.activityinfo.geoadmin;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import org.activityinfo.geoadmin.model.Country;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.channels.FileChannel.MapMode;
import java.security.MessageDigest;
import java.util.List;
import java.util.logging.Logger;

/**
 * A set of features from a file to be imported into ActivityInfo's
 * administrative reference database.
 * 
 */
public class ImportSource {

    private static final Logger LOGGER = Logger.getLogger(ImportSource.class.getName());

    private static final int IN_MEMORY_LIMIT_BYTES = 1024 * 1024 * 5;

    private List<PropertyDescriptor> attributes;

    private SimpleFeatureSource featureSource;
    private MathTransform transform;
    private File file;
    private boolean mbrOnly;

    private List<ImportFeature> features = Lists.newArrayList();

    private String hash;

    public ImportSource(File shapefile) throws Exception {
        this.file = shapefile;
        this.mbrOnly = this.file.length() > IN_MEMORY_LIMIT_BYTES;


        ShapefileDataStore ds = new ShapefileDataStore(shapefile.toURI().toURL());

        featureSource = ds.getFeatureSource();

        transform = createTransform();
        loadFeatures();
        calculateHash();
    }

    public boolean isMbrOnly() {
        return mbrOnly;
    }

    /**
     * Loads the feature's attributes and the envelope for the geometry into
     * memory.
     */
    private void loadFeatures() throws IOException {

        attributes = getNonGeometryAttributes();
        SimpleFeatureCollection features = featureSource.getFeatures();
        SimpleFeatureIterator it = features.features();
        while (it.hasNext()) {
            SimpleFeature feature = (SimpleFeature) it.next();
            if(hasGeometry(feature)) {
              ImportFeature importFeature = new ImportFeature(
                  attributes,
                  toAttributeArray(feature),
                  calcWgs84Geometry(feature));

              this.features.add(importFeature);
            } else {
              System.err.println("No geometry: " + attributes);
            }
        }
    }

    public boolean hasGeometry(SimpleFeature feature) {
        Geometry geometry = (Geometry) feature.getDefaultGeometryProperty().getValue();
        return geometry != null;
    }

    /**
     * Calculates the geographic envelope of the feature in the WGS 84
     * Geographic Reference system.
     */
    private Geometry calcWgs84Geometry(SimpleFeature feature) {
        try {
            Geometry geometry = (Geometry) feature.getDefaultGeometryProperty().getValue();
            Geometry geometryInWgs84 = JTS.transform(geometry, transform);
            if(mbrOnly) {
                geometryInWgs84 = geometryInWgs84.getEnvelope();
            }
            return geometryInWgs84;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads the attribute vaules in an array.
     * 
     * @param feature
     * @return
     */
    private Object[] toAttributeArray(SimpleFeature feature) {
        Object[] attribs = new Object[attributes.size()];
        for (int i = 0; i != attribs.length; ++i) {
            attribs[i] = feature.getAttribute(attributes.get(i).getName());
        }
        return attribs;
    }

    /**
     * Finds all the non-geographic attributes in the source.
     */
    private List<PropertyDescriptor> getNonGeometryAttributes() {
        attributes = Lists.newArrayList();
        for (PropertyDescriptor descriptor : featureSource.getSchema().getDescriptors()) {
            if (!(descriptor.getType() instanceof GeometryType)) {
                attributes.add(descriptor);
            }
        }
        return attributes;
    }

    public List<PropertyDescriptor> getAttributes() {
        return attributes;
    }

    public int getFeatureCount() {
        return features.size();
    }

    private MathTransform createTransform() throws Exception {
        GeometryDescriptor geometryType = featureSource.getSchema().getGeometryDescriptor();
        CoordinateReferenceSystem sourceCrs = geometryType.getCoordinateReferenceSystem();
        if(sourceCrs == null) {
            // if it's not WGS84, we'll soon find out as we check the geometry against the
            // country bounds
            sourceCrs = DefaultGeographicCRS.WGS84;
        }

        CoordinateReferenceSystem geoCRS = DefaultGeographicCRS.WGS84;
        boolean lenient = true; // allow for some error due to different datums
        return CRS.findMathTransform(sourceCrs, geoCRS, lenient);
    }

    public int getAttributeCount() {
        return attributes.size();
    }

    public String[] getAttributeNames() {
        String[] names = new String[attributes.size()];
        for (int i = 0; i != names.length; ++i) {
            names[i] = attributes.get(i).getName().getLocalPart();
        }
        return names;
    }

    public FeatureSource getFeatureSource() {
        return featureSource;
    }

    /**
     * Checks to see whether all geometry at least intersects the country's
     * geographic bounds. This is a good check to ensure that we have correctly
     * understood the source's CRS.
     * 
     * @param country
     * @return
     */
    public boolean validateGeometry(Country country) {
        Envelope countryEnvelope = countryBounds(country);
        for (ImportFeature feature : features) {
            if (!countryEnvelope.intersects(feature.getEnvelope())) {
                System.out.println(feature.toString() + " has envelope " + feature.getEnvelope());
                return false;
            }
        }
        return true;
    }

    private Envelope countryBounds(Country country) {
        if (country.getBounds() == null) {
            return new Envelope(-180, 180, -90, 90);
        } else {
            return GeoUtils.toEnvelope(country.getBounds());
        }
    }

    public File getFile() {
        return file;
    }

    public String getMetadata() throws IOException {
        File metadataFile = getFile(".shp.xml");
        if (metadataFile.exists()) {
            return Files.toString(metadataFile, Charsets.UTF_8);
        } else {
            return null;
        }
    }

    public List<ImportFeature> getFeatures() {
        return features;
    }

    public String getMd5Hash() {
        return hash;
    }

    private void calculateHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            updateHash(digest, ".shp");
            updateHash(digest, ".shx");
            updateHash(digest, ".shp.xml");
            updateHash(digest, ".dbf");
            updateHash(digest, ".sbn");
            updateHash(digest, ".prj");

            this.hash = new BigInteger(1, digest.digest()).toString(16);
        } catch (Exception e) {
            throw new RuntimeException("Exception generating hash");
        }
    }

    private void updateHash(MessageDigest digest, String extension) throws IOException {
        File file = getFile(extension);
        if (file.exists()) {
            digest.update(Files.map(file, MapMode.READ_ONLY));
        }
    }

    private File getFile(String extension) throws AssertionError {
        String absPath = file.getAbsolutePath();
        if (!absPath.endsWith(".shp")) {
            throw new AssertionError();
        }
        File file = new File(absPath.substring(0, absPath.length() - 4) + extension);
        return file;
    }

}
