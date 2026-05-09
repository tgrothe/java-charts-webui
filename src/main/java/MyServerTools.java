import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.text.StringSubstitutor;

public class MyServerTools {
  private static final HashMap<String, String> templates = new HashMap<>();

  public static String getTemplate(String path, String... args) throws IOException {
    if (path == null || path.isBlank() || args == null) {
      throw new IllegalArgumentException("Path and args cannot be null or empty.");
    }
    if (!templates.containsKey(path)) {
      try (InputStream templateStream = MyCompiler.class.getResourceAsStream(path)) {
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
}
