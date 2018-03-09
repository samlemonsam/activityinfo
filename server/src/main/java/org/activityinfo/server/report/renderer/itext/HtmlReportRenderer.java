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
package org.activityinfo.server.report.renderer.itext;

import com.google.code.appengine.awt.Color;
import com.google.code.appengine.awt.Graphics2D;
import com.google.code.appengine.awt.color.ColorSpace;
import com.google.code.appengine.awt.image.BufferedImage;
import com.google.code.appengine.imageio.ImageIO;
import com.google.inject.Inject;
import com.lowagie.text.*;
import com.lowagie.text.html.HtmlWriter;
import org.activityinfo.legacy.shared.reports.model.ReportElement;
import org.activityinfo.server.generated.GeneratedResource;
import org.activityinfo.server.generated.StorageProvider;
import org.activityinfo.server.geo.AdminGeometryProvider;
import org.activityinfo.server.report.generator.MapIconPath;
import org.activityinfo.server.report.renderer.image.ImageCreator;
import org.activityinfo.server.report.renderer.image.ItextGraphic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * iText ReportRenderer targeting HTML output
 */
public class HtmlReportRenderer extends ItextReportRenderer {

    private final StorageProvider imageStorageProvider;

    @Inject
    public HtmlReportRenderer(AdminGeometryProvider geometryProvider,
                              @MapIconPath String mapIconPath,
                              StorageProvider imageStorageProvider) {
        super(geometryProvider, mapIconPath);
        this.imageStorageProvider = imageStorageProvider;
    }

    @Override
    protected DocWriter createWriter(Document document, OutputStream os) throws DocumentException {
        return HtmlWriter.getInstance(document, os);
    }

    @Override
    public String getMimeType() {
        return "text/html";
    }

    @Override
    public String getFileSuffix() {
        return ".html";
    }

    public void render(ReportElement element, final Writer writer) throws IOException {
        // Nasty hack to correct image layout in HTML
        // HtmlWriter always writes with 
        
        // The HtmlWriter encodes everything as ISO-8859-1
        final Charset charset = Charset.forName("ISO-8859-1");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        render(element, baos);
        String html = new String(baos.toByteArray(), charset);
        html = html.replaceAll("<img align=\"Left\"", "<img ");
        writer.write(html);
    }

    @Override
    protected void renderFooter(Document document) {
        // no footer for HTML
    }

    @Override
    protected ImageCreator getImageCreator() {
        return new HtmlImageCreator();
    }

    private class HtmlImageCreator implements ImageCreator {
        @Override
        public HtmlImage create(int width, int height) {
            BufferedImage image = new BufferedImage(width, height, ColorSpace.TYPE_RGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setPaint(Color.WHITE);
            g2d.fillRect(0, 0, width, height);

            return new HtmlImage(image, g2d);
        }

        @Override
        public ItextGraphic createMap(int width, int height) {
            return create(width, height);
        }
    }

    public static class MyImage extends Image {
        private int width;
        private int height;

        public MyImage(URL url, int width, int height) {
            super(url);
            this.width = width;
            this.height = height;
        }

        public MyImage(com.lowagie.text.Image im) {
            super(im);
        }

        @Override
        public int type() {
            return Element.IMGTEMPLATE;
        }

        @Override
        public float getScaledWidth() {
            return width;
        }

        @Override
        public float getScaledHeight() {
            return height;
        }
        
        
    }

    private class HtmlImage implements ItextGraphic {
        private final BufferedImage image;
        private final Graphics2D g2d;

        public HtmlImage(BufferedImage image, Graphics2D g2d) {
            super();
            this.image = image;
            this.g2d = g2d;
        }

        @Override
        public Graphics2D getGraphics() {
            return g2d;
        }

        @Override
        public Image toItextImage() throws BadElementException {
            try {
                GeneratedResource storage = imageStorageProvider.create("image/png", "activityinfo.png");
                try(OutputStream output = storage.openOutputStream()) {
                    ImageIO.write(image, "PNG", output);
                }
                return new MyImage(new URL(storage.getDownloadUri()), image.getWidth(), image.getHeight());
                
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void addImage(String imageUrl, int x, int y, int width, int height) {
            BufferedImage img;
            try {
                img = ImageIO.read(new URL(imageUrl));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            g2d.drawImage(img, x, y, null);
        }
    }
}
