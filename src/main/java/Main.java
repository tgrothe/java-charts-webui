import com.hellokaton.blade.Blade;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;
import javax.tools.JavaCompiler;

public class Main {
  private static final String CODE_TEXT =
      """
      public class MySupplierImpl implements MySupplier {
        public int numberOfSeries() {
          return 2; // Return the number of series to be generated
        }

        public String getTitle(int seriesIndex) {
          return "Series " + (seriesIndex + 1); // Return a title for each series
        }

        public double[][] generateSeries(int seriesIndex) {
          double[][] series = new double[10][2];
          double a = seriesIndex * 10; // Example coefficient based on series index
          for (int i = 0; i < series.length; i++) {
            double y = f(i, a);
            series[i][0] = i; // x value
            series[i][1] = y; // y value based on the function
          }
          return series;
        }

        private double f(double x, double a) {
          return x * x * a; // Example function
        }
      }
      """;
  private static final String EMPTY_IMG_SRC =
      "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNgYAAAAAMAASsJTYQAAAAASUVORK5CYII=";

  public static void main(String[] args) {
    Blade.create()
        .get(
            "/",
            ctx -> {
              ctx.attribute("codetext", CODE_TEXT);
              ctx.attribute("imgsrc", EMPTY_IMG_SRC);
              ctx.render("index.html");
            })
        .get(
            "/code/:code",
            ctx -> {
              try {
                String codeText = URLDecoder.decode(ctx.pathString("code"), StandardCharsets.UTF_8);
                ctx.attribute("codetext", codeText);
                ctx.attribute("imgsrc", MyPlotter.plot(compileSupplierCode(codeText)));
                ctx.render("index.html");
              } catch (Exception e) {
                e.printStackTrace();
                ctx.text("Error compiling code: " + e.getMessage());
              }
            })
        .listen(80)
        .start();
  }

  private static MySupplier compileSupplierCode(String codeText) throws Exception {
    // Save the code to a temporary file and compile it
    Path parentDir = Paths.get("temp");
    try (Stream<Path> paths = Files.walk(parentDir)) {
      paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }
    Files.createDirectories(parentDir);
    Path tempFile = Paths.get(parentDir.toString(), "MySupplierImpl.java");
    Files.writeString(tempFile, codeText);
    JavaCompiler jc = javax.tools.ToolProvider.getSystemJavaCompiler();
    jc.run(null, null, null, tempFile.toFile().getAbsolutePath());
    Path classFile = Objects.requireNonNull(tempFile.getParent()).resolve("MySupplierImpl.class");
    ClassLoader redefineClassLoader =
        new ClassLoader() {
          @Override
          public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (name.equals("MySupplierImpl")) {
              try {
                byte[] buf = Files.readAllBytes(classFile);
                int len = buf.length;
                return defineClass(name, buf, 0, len);
              } catch (IOException e) {
                throw new ClassNotFoundException("", e);
              }
            }
            return getParent().loadClass(name);
          }
        };
    return (MySupplier)
        Class.forName("MySupplierImpl", true, redefineClassLoader)
            .getDeclaredConstructors()[0]
            .newInstance();
  }
}
