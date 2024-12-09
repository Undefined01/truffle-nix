package website.lihan.trufflenix.integrationtest;

public class FibonacciTest {
  public static final String PROGRAM_NIX =
  """
  let
    fib = n:
      if n < 2
        then n
        else fib (n - 1) + fib (n - 2);
  in
    fib
  """;

  public static final String PROGRAM_NIX2 =
  """
  let
    fib = f: n:
      if n < 2
        then n
        else f f (n - 1) + f f (n - 2);
  in
    fib fib
  """;

  public static final String PROGRAM_NIX3 =
  """
  let
    fib = arg:
      if arg.n < 2
        then arg.n
        else arg.f { f = arg.f; n = arg.n - 1; } + arg.f { f = arg.f; n = arg.n - 2; };
  in
    fib { f = fib; n = 20; }
  """;

  public static final String FIB_TAIL =
  """
  let
    fib_with_tail_recursion = n:
      let
        fib_tail = n: a: b:
          if n == 0
            then a
            else fib_tail (n - 1) b (a + b);
      in
        fib_tail n 0 1;
  in
  """;
}
