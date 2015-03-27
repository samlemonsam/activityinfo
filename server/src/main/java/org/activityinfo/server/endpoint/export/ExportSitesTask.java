package org.activityinfo.server.endpoint.export;

import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.activityinfo.legacy.shared.auth.AuthenticatedUser;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.FilterUrlSerializer;
import org.activityinfo.server.authentication.ServerSideAuthProvider;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.util.monitoring.Timed;

import javax.inject.Provider;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;

@Singleton
public class ExportSitesTask extends HttpServlet {

    public static final String END_POINT = "/tasks/export";

    public static final String EXPORT_BUCKET_NAME = "activityinfo-generated";

    private Provider<DispatcherSync> dispatcher;
    private ServerSideAuthProvider authProvider;
    
    @Inject
    public ExportSitesTask(Provider<DispatcherSync> dispatcher, ServerSideAuthProvider authProvider) {
        this.dispatcher = dispatcher;
        this.authProvider = authProvider;
    }

    @Override
    @Timed(name = "export", kind = "sites")
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // authenticate this task
        authProvider.set(new AuthenticatedUser("",
                Integer.parseInt(req.getParameter("userId")),
                req.getParameter("userEmail")));


        // create the workbook
        Filter filter = FilterUrlSerializer.fromQueryParameter(req.getParameter("filter"));
        SiteExporter export = new SiteExporter(dispatcher.get()).buildExcelWorkbook(filter);

        // Save to GCS
        GcsService gcs = GcsServiceFactory.createGcsService();
        GcsFileOptions fileOptions = new GcsFileOptions.Builder()
                .mimeType("application/vnd.ms-excel")
                .contentDisposition("attachment; filename=" + req.getParameter("filename"))
                .build();
        GcsFilename fileName = new GcsFilename(EXPORT_BUCKET_NAME,
                req.getParameter("exportId"));

        try(OutputStream outputStream = Channels.newOutputStream(gcs.createOrReplace(fileName, fileOptions))) {
            export.getBook().write(outputStream);
        }
    }
}
