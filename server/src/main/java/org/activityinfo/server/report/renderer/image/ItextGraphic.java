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
package org.activityinfo.server.report.renderer.image;

import com.google.code.appengine.awt.Graphics2D;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Image;

public interface ItextGraphic {

    /**
     * Adds an image to the graphic. This is often preferable to using
     * getGraphics().drawImage() because for PDF and RTF reports, we can embed
     * the image bytes directly in the output without have to transcode the
     * image.
     * <p/>
     * <p/>
     * IMPORTANT: unlike the iText coordinate system, the coordinate system here
     * has its origin in the UPPER-LEFT hand corner. This is to keep it
     * compatible with the Graphics2D coordinate system.
     *
     * @param imageUrl the URL of the image
     * @param x        the left-hand position of the image
     * @param y        the top position of the image
     * @param width    the image's physical width in pixels
     * @param height   the image's physical height in pixels
     */
    void addImage(String imageUrl, int x, int y, int width, int height);

    Graphics2D getGraphics();

    /**
     * @return an iText image.
     * @throws BadElementException
     */
    Image toItextImage() throws BadElementException;

}
