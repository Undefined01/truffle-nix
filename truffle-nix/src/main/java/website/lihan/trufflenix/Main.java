package website.lihan.trufflenix;

import java.util.Scanner;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

public class Main {
  public static void main(String[] args) {
    var context = Context.create();

    Value value;
    if (args.length > 0) {
      value = context.eval("nix", args[0]);
    } else {
      // From standard input
      var program = new StringBuilder();
      var scanner = new Scanner(System.in);
      while (scanner.hasNextLine()) {
        program.append(scanner.nextLine());
      }
      scanner.close();
      value = context.eval("nix", program.toString());
    }
    System.out.println(value.asString());
  }
}
