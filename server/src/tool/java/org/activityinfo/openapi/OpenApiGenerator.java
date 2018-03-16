package org.activityinfo.openapi;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.jaxrs2.Reader;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import org.activityinfo.server.endpoint.rest.FormResource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class OpenApiGenerator {

  public static void main(String[] args) throws IOException {
    SwaggerConfiguration config = new SwaggerConfiguration();
    config.setReadAllResources(false);

    Reader reader = new Reader(config);
    OpenAPI openApi = reader.read(FormResource.class);

    File outputFile = new File(args[0]);

    try(FileWriter writer = new FileWriter(outputFile)) {
      Json.pretty().writeValue(writer, openApi);
    }
  }
}
