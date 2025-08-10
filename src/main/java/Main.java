import com.hellokaton.blade.Blade;

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
        .post(
            "/",
            ctx -> {
              String codeText = ctx.bodyToString();
              ctx.attribute("codetext", codeText);
              ctx.attribute("imgsrc", EMPTY_IMG_SRC);
              ctx.render("index.html");
            })
        .listen(80)
        .start();
  }
}
