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
package org.activityinfo.server.report.renderer;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.activityinfo.legacy.shared.command.RenderElement;
import org.activityinfo.server.report.renderer.excel.ExcelMapDataExporter;
import org.activityinfo.server.report.renderer.excel.ExcelReportRenderer;
import org.activityinfo.server.report.renderer.image.ImageReportRenderer;
import org.activityinfo.server.report.renderer.itext.HtmlReportRenderer;
import org.activityinfo.server.report.renderer.itext.PdfReportRenderer;
import org.activityinfo.server.report.renderer.itext.RtfReportRenderer;

/**
 * Creates a {@code Renderer} for a given file format
 */
public class RendererFactory {

    private final Injector injector;

    @Inject
    public RendererFactory(Injector injector) {
        this.injector = injector;
    }

    public Renderer get(RenderElement.Format format) {

        switch (format) {
            case Word:
                return injector.getInstance(RtfReportRenderer.class);
            case Excel:
                return injector.getInstance(ExcelReportRenderer.class);
            case PDF:
                return injector.getInstance(PdfReportRenderer.class);
            case PNG:
                return injector.getInstance(ImageReportRenderer.class);
            case Excel_Data:
                return injector.getInstance(ExcelMapDataExporter.class);
            case HTML:
                return injector.getInstance(HtmlReportRenderer.class);
        }

        throw new IllegalArgumentException();
    }
}
