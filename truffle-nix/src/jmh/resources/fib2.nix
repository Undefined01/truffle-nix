let
  fib = f: n:
    if n < 2
      then n
      else f f (n - 1) + f f (n - 2);
in
  fib fib 5