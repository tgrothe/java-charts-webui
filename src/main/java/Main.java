import com.hellokaton.blade.Blade;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class Main {
  private static final String CODE_TEXT =
      """
      Test
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
              String codeText = URLDecoder.decode(ctx.pathString("code"), StandardCharsets.UTF_8);
              ctx.attribute("codetext", codeText);
              ctx.attribute("imgsrc", EMPTY_IMG_SRC);
              ctx.render("index.html");
            })
        .listen(80)
        .start();
  }
}
