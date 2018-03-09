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

import com.google.inject.Inject;
import org.activityinfo.legacy.shared.reports.model.MapReportElement;
import org.activityinfo.legacy.shared.reports.model.PivotChartReportElement;
import org.activityinfo.legacy.shared.reports.model.ReportElement;
import org.activityinfo.server.report.renderer.ChartRendererJC;
import org.activityinfo.server.report.renderer.Renderer;

import java.io.IOException;
import java.io.OutputStream;

/*
 * @author Alex Bertram
 */
public class ImageReportRenderer implements Renderer {

    private final ImageMapRenderer mapRenderer;
    private final ChartRendererJC chartRenderer;

    @Inject
    public ImageReportRenderer(ImageMapRenderer renderer, ChartRendererJC chartRendererJC) {
        this.mapRenderer = renderer;
        this.chartRenderer = chartRendererJC;
    }

    @Override
    public void render(ReportElement element, OutputStream os) throws IOException {
        // TODO: support for other types?

        if (element instanceof MapReportElement) {
            mapRenderer.render((MapReportElement) element, os);
        } else if (element instanceof PivotChartReportElement) {
            chartRenderer.render((PivotChartReportElement) element, os);
        }

    }

    @Override
    public String getMimeType() {
        return "image/png";
    }

    @Override
    public String getFileSuffix() {
        return ".png";
    }
}
