import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyHttpServer {
  public record MyHttpHandler(String path, HttpHandler handler) {}

  public enum ContentType {
    HTML("text/html"),
    JSON("application/json"),
    TEXT("text/plain");

    ContentType(String type) {}
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(MyHttpServer.class);

  private static final HashMap<String, String> templates = new HashMap<>();

  public static void start(MyHttpHandler... handlers) throws IOException {
    if (handlers == null || handlers.length == 0) {
      throw new IllegalArgumentException("At least one handler must be provided.");
    }
    System.setProperty("java.net.preferIPv4Stack", "true");
    HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);
    LOGGER.info("MyHttpServer initialized.");
    for (MyHttpHandler handler : handlers) {
      if (handler == null || handler.path() == null || handler.path().isBlank()) {
        throw new IllegalArgumentException("Handler path cannot be null or empty.");
      }
      server.createContext(
          handler.path(),
          exchange -> {
            LOGGER.info(
                "Received request from client: {} for URL: {}",
                getClientIpAddress(exchange),
                exchange.getRequestURI());
            try {
              handler.handler().handle(exchange);
            } catch (Exception e) {
              LOGGER.warn("Exception in handler for path {}: {}", handler.path(), e.getMessage());
              sendCustomResponse(
                  exchange,
                  500,
                  ContentType.TEXT,
                  null,
                  "An error occurred while processing your request.",
                  e.getMessage());
            }
            LOGGER.info(
                "Request processing completed for client: {} with status code: {}",
                getClientIpAddress(exchange),
                exchange.getResponseCode());
          });
      LOGGER.info("Handler added for path: {}", handler.path());
    }
    server.setExecutor(null); // creates a default executor
    server.start();
    LOGGER.info("Server started on port 80.");
  }

  public static String getClientIpAddress(HttpExchange request) {
    String xForwardedForHeader = request.getRequestHeaders().getFirst("X-Forwarded-For");
    if (xForwardedForHeader == null) {
      return request.getRemoteAddress().getAddress().getHostAddress();
    }
    // As of https://en.wikipedia.org/wiki/X-Forwarded-For
    // The general format of the field is: X-Forwarded-For: client, proxy1, proxy2 ...
    // we only want the client
    return new StringTokenizer(xForwardedForHeader, ",").nextToken().trim();
  }

  public static String getPathPart(HttpExchange ex, int index) {
    String[] parts = ex.getRequestURI().getRawPath().split("/");
    if (index < 0 || index >= parts.length) {
      return null;
    }
    return URLDecoder.decode(parts[index], StandardCharsets.UTF_8);
  }

  private static String getTemplate(String path, String... args) throws IOException {
    if (!templates.containsKey(path)) {
      try (InputStream templateStream = Main.class.getResourceAsStream(path)) {
        assert templateStream != null;
        templates.put(path, new String(templateStream.readAllBytes(), StandardCharsets.UTF_8));
      }
    }
    return StringSubstitutor.replace(
        templates.get(path),
        IntStream.range(0, args.length)
            .boxed()
            .collect(
                Collectors.toMap(
                    i -> "" + i, // key is the index as a string
                    i -> args[i] // value is the corresponding argument
                    )),
        "{{",
        "}}");
  }

  public static void sendCustomResponse(
      HttpExchange exchange,
      int statusCode,
      ContentType contentType,
      String templatePath,
      String... templateValues) {
    try {
      String responseBody =
          templatePath != null
              ? getTemplate(templatePath, templateValues)
              : Arrays.toString(templateValues);
      exchange.getResponseHeaders().add("Content-Type", contentType + "; charset=utf-8");
      exchange.sendResponseHeaders(statusCode, responseBody.length());
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(responseBody.getBytes(StandardCharsets.UTF_8));
      }
    } catch (IOException e) {
      LOGGER.error("Failed to send custom response: {}", e.getMessage());
      throw new UncheckedIOException(e);
    }
  }
}
