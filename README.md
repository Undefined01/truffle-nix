# Truffle Nix

Truffle Nix is a [GraalVM](http://graalvm.org/) implementation of the [Nix programming language](https://nix.dev/manual/nix/2.18/language/).
It is work in progress and not yet feature complete.
You can find the current status of the implementation in the [features](#supported-features) section.

## Building, Testing, and Running

To build the project, you need to have GraalVM 23 installed.

```bash
$ ./gradlew :truffle-nix:installDist
```

The build will create a distribution in `truffle-nix/build/install/truffle-nix`.

This project is still in development, and the native libraries are not bundled in the distribution.
To run the project, you need to set the `LD_LIBRARY_PATH` to a directory containing the native libraries.
Or install the native libraries to the system library path.

```bash
$ export LD_LIBRARY_PATH="$(pwd)/tree-sitter-nix/src/main/resources"
```

You can run the project with the following command:

```bash
$ truffle-nix/build/install/truffle-nix [<--options>] [<nix-file>]
```

The options will be passed to the Truffle language launcher and can be used to set the optimization level or other options.
The nix file is the path to the nix file that should be evaluated. If no file is provided, it will read from the standard input.

## Performance

```bash
$ ./gradlew :truffle-nix:test
$ ./gradlew :truffle-nix:jmh
```

| Program | Simple Language | GraalJS | Java | Truffle Nix |
|---------|-----------------|---------|------|-------------|
| `fibonacci` | 38 us | 43 us | 30 us | 64 us |
| `fibonacci_closure` | / | 159 us | / | 46 us |
| `quicksort` | / | 79 us | 40 | 74 us |


## Supported Features

#### Primitive Types

Nix has serveral primitive types, Any valid nix expression will be evaluated to one of these types.
- [x] `int` (**64-bit** signed integer)
- [x] `float` (**64-bit** floating point number)
- [x] `boolean`
- [x] `string`
- [x] `lambda` (a function that takes one argument and returns any nix type)
- [x] `list` (a list of nix types, the elements can be of different types)
- [x] `attrset` (a set of key-value pairs, the keys are strings and the values can be any nix type)
- [ ] `path`
- [ ] `null`


Note that `true` and `false` are not keywords but just variables in Nix that are bound to the boolean values. You can access them in `builtins.true` and `builtins.false`.

#### Operators

- [x] string concatenation: `"hello" + "world"`
- [x] arithmetic operators for integers and floats: `1 + 2`, `3. - 4.`, `5 * 6.`, `7 / 8`
    - When an integer and a float are used together, the integer will be promoted to a float.
    - Integer division like `7 / 8` will be rounded towards zero
- [x] comparison operators for integers and floats: `1 < 2`, `3 <= 4`, `5 > 6`, `7 >= 8`, `9 == 10`, `11 != 12`
- [x] comparison operators for strings: `"a" < "b"`, `"c" <= "d"`, `"e" > "f"`, `"g" >= "h"`, `"i" == "j"`, `"k" != "l"`
    - Strings are compared lexicographically
- [x] boolean negation: `!true`
- [x] boolean operators: `true && false`, `true || false`
    - The `&&` and `||` operators are short-circuiting, meaning that the second operand is only evaluated if necessary
- [x] list concatenation: `[1 2] ++ [3 4]`
- [x] attribute selection: `attrs.key`
- [ ] attribute existence check: `attrs ? key`
- [ ] attribute selection with default: `attrs.key or "default"`
- [ ] attribute set extension: `attrs // { key = value; }`

#### String

- [x] basic string: `"hello"`
    - basic string can cross multiple lines, and all whitespace characters are preserved.
    - All line breaks (CR/LF/CRLF) in the string are normalized to `\n`. But the line break produced by the escape sequence are left as is, e.g. `"hello\r\nworld"` will be evaluated to the string `hello\r\nworld` with CR and LF characters.
- [x] string escaping: `"\"hello\"\n"` (evaluates to the string `"hello"` with a newline character at the end)
    - String escaping only supports limited escape sequences: `\"`, `\r`, `\n`, `\t`. Other characters following a backslash are treated as is, e.g. `"\a"` will be treated as the string `a`.
- [x] string interpolation: `"hello ${"world ${ "!" }"}"`
- [ ] multi-line string: `''hello''`
    - Multi-line strings are strings that remove the common indentation from all lines.
        For example, the following indented string contains 2 leading spaces on the quote line and 4 leading spaces on the world line:
        ```nix
          ''
            hello
              world
          ''
        ```
        It will be evaluated to the string `"hello\n  world\n"` with 4 leading spaces removed.

For more information, see the test cases in `StringTest.java`.

#### Expressions

- [x] let expression: `let x = 1; in x + 2` (evaluates to 3)
    - [ ] The bindings in the `let` expression are evaluated simultaneously, which means the bindings can reference each other.

        ```nix
        let
            a = c * b;
            b = 1;
            c = b + 1;
        in
            a # evaluates to 2
        ```
        ```nix
        let
            a = { x = b; };
            b = { y = a; };
        in
            a.x.y.x # evaluates to { y = { x = { y = ... }; }; }
        ```
- [x] function application: `builtins.typeOf 1` (evaluates to string `int`)
    - [x] partially applied function

        Some builtin functions like `builtins.elemAt` take multiple arguments.
        But you can only apply one argument at a time and get a new function that takes the remaining arguments.

        ```nix
        let
            fib = builtins.elemAt [0 1 1 2 3 5 8 13 21 34];
        in
            (fib 5) + (fib 6)   # evaluates to 8 + 13 = 21
        ```
- [x] lambda expression: `x: x + 1`
    - Every lambda expression takes exactly one argument.
    - [x] closure: Lambda can capture the variables from the scope where it is created, and the captured variables are available as long as the lambda.
        ```nix
        let
            x = 1;
            f = y: x + y;
        in
            let
                x = 2;
            in
                f 1 # evaluates to 2, not 3
        ```
    - [x] curried lambda / partial application: Lambda can be partially applied by providing fewer arguments than the lambda expects. Since nix only supports lambdas with one argument, lambdas with multiple arguments are simulated by returning a closure that captures the arguments. Therefore, all lambdas are curried by default.
        ```nix
        let
            add = x: y: x + y;
            add1 = add 1;
            add2 = add 2;
        in
            (add1 1) + (add2 1) # evaluates to 2 + 3 = 5
        ```
    - [x] self-reference: Lambdas can reference themselves in the `let` expression.
        ```nix
        let
            fib = n:
                if n < 2
                    then n
                    else fib (n - 1) + fib (n - 2);
        in
            fib 10 # evaluates to 55
        ```
    - [x] parameter unpacking: `{ x, y }: x + y`

        The argument must be an attribute set with the keys `x` and `y`. `x` and `y` are added to the scope of the lambda's body.

    - [x] parameter unpacking with default values: `{ x, y ? 2 }: x + y`

        The argument must be an attribute set with the key `x` and an optional key `y`. `x` and `y` are added to the scope of the lambda's body and `y` defaults to 2 if not provided.

    - [x] parameter unpacking with rest argument: `{ x, ... }: x`.

        The argument must be an attribute set with the key `x` and may have additional keys. Only `x` is added to the scope of the lambda's body.

    - [x] parameter unpacking with whole attribute set: `{ x, ... } @ args: assert args.x == x` and `args @ { x, ... }: args.x == x`

        The argument must be an attribute set with the key `x` and may have additional keys. The whole attribute set named `args` and `x` are added to the scope of the lambda's body.

- [x] conditional expression: `if true then 1 else 2` (evaluates to 1)
- [x] with expression: `with { x = 1; }; x + 2` (evaluates to 3)
    - Inner with expressions will shadow the outer with expressions.
        ```nix
        with { x = 1; };
        with { x = 2; };
        x # evaluates to 2
        ```
    - With expression will not shadow the local variables defined in the `let` expression.
        ```nix
        let
            x = 1;
        in
            with { x = 2; };
            x # evaluates to 1
        ```
- [ ] recursive attribute set: `rec { x = 1; y = x + 1; }` (evaluates to `{ x = 1; y = 2; }`)
- [ ] attribute set constructor:
    - [ ] multi-level path: `{ x.y = 1; }.x.y`
    - [ ] auto-merging: `{ x.y = 1; x.z = 2; }.x`
    - [ ] interpolation: `let a = "x"; in { ${a} = 2 }.x`

#### Language Features

- [ ] lazy evaluation: The evaluation of an expression is delayed until the value is needed.
- [x] tail call optimization: Tail calls are optimized to avoid stack overflow.

    ```nix
    let
        sum = n:
            if n == 0
                then 0
                else n + sum (n - 1);
    in
        sum 100000 # stack overflow without tail call optimization
    ```
    ```nix
    let
        sum = n: acc:
            if n == 0
                then acc
                else sum (n - 1) (n + acc);
    in
        sum 100000 0 # evaluates to 5000050000
    ```

#### Builtins

We use `=>` to indicate the function signature. `int => int` means a function that takes an integer and returns an integer. The `=>` is right-associative, so `int => int => int` means a function that takes an integer (`int`) and returns a function that takes an integer and returns an integer (`int => int`).

The uppercase letters `A`, `B`, and `C` are used as placeholders for any nix type. They are not a specific type, but only indicate the dataflow.
For example, `list A => A` means a function that takes a list and returns some value from the list. `list A => list B => B` means a function that takes two lists and returns some value from the second list.

Miscellaneous builtins:
- [x] true: `boolean`
- [x] false: `boolean`
- [x] typeOf: `A => string`

    Returns the type of the argument as a string.

- [ ] compareVersions: `string => string => int`

    Compares two versions and returns -1 if the first version is smaller, 0 if they are equal, and 1 if the first version is greater.

- [ ] match *regex* *str*: `string => string => list string`

    Matches a string against a extended POSIX regular expression and returns the regex groups.
    For example, `builtins.match "([0-9]+) ([a-z]+)" "123 abc"` evaluates to `[ "123 abc" "123" "abc" ]`.

File system builtins:
- [ ] fetchClosure: `attrset => path`

    Fetches a closure from the given URL and returns the path to the closure.

- [ ] fetchGit: `attrset => path`

    Fetches a git repository from the given URL and returns the path to the repository.

- [ ] fetchTarball: `attrset => path`

    Fetches a tarball from the given URL and returns the path to the extracted tarball.

- [ ] fetchurl: `string => path`

    Fetches a file from the given URL and returns the path to the file.

- [ ] fromJSON: `string => A`

    Parses a JSON string and returns the corresponding nix value.

- [ ] fromTOML: `string => A`

    Parses a TOML string and returns the corresponding nix value.

- [ ] toJSON: `A => string`

    Converts a nix value to a JSON string.

- [ ] toTOML: `A => string`
    
    Converts a nix value to a TOML string.

- [ ] toXML: `A => string`

    Converts a nix value to an XML string.

- [ ] readFile: `path => string`

    Reads the content of a file and returns it as a string.

Operators:
- [ ] add: `number => number`
- [ ] sub: `number => number`
- [ ] mul: `number => number`
- [ ] div: `number => number`
- [ ] mod: `int => int`
- [ ] neg: `number => number`
- [ ] bitAnd: `int => int`
- [ ] bitOr: `int => int`
- [ ] bitXor: `int => int`

Debugging builtins:
- [x] assert: `A => B => B`

    Evaluates the first argument, aborts if it is false, otherwise returns the second argument.

- [ ] abort: `string => !`

    Aborts evaluation with an error message

- [ ] trace: `A => B => B`

    Evaluates and prints the first argument, then returns the second argument.


Math builtins:
- [ ] ceil: `float => int`

    Returns the smallest integer greater than or equal to the argument. For example, `builtins.ceil 1.1` evaluates to `2`, and `builtins.ceil -1.1` evaluates to `-1`.

- [ ] floor: `float => int`

    Returns the largest integer less than or equal to the argument. For example, `builtins.floor 1.1` evaluates to `1`, and `builtins.floor -1.1` evaluates to `-2`.

- [ ] round: `float => int`

    Returns the nearest integer to the argument. For example, `builtins.round 1.1` evaluates to `1`, and `builtins.round -1.1` evaluates to `-1`.

List related builtins:
- [x] length: `list A => int`

    Returns the number of elements in the list.

- [x] elemAt: `list A => int => A`

    Returns the element at the given index.

- [x] head: `list A => A`

    Returns the first element of the list.

- [x] tail: `list A => list A`

    Returns the list without the first element.
    
- [x] filter: `(A => boolean) => list A => list A`

    Returns a new list with all elements for which the predicate is true.

- [ ] map: `(A => B) => list A => list B`

    Returns a new list with the result of applying the function to each element.

- [ ] all: `(A => boolean) => list A => boolean`

    Returns true if the predicate is true for all elements in the list.

- [ ] any: `(A => boolean) => list A => boolean`

    Returns true if the predicate is true for any element in the list.

- [ ] foldl' *accumulator* *initialValue* *list*: `(B => A => B) => B => list A => B`

    Applies the function to each element in the list from left to right and accumulates the result.

- [ ] elem: `A => list A => boolean`

    Returns true if the element is in the list.

- [ ] concatLists: `list list A => list A`

    Concatenates a list of lists into a single list.

- [ ] concatMap *f* *list*: `(A => list B) => list A => list B`

    Maps the function over the list and concatenates the results. Equivalent to `concatLists (map f list)`.

- [ ] groupBy: `(A => string) => list A => attrset A`

    Groups the elements of the list by the result of the function.


Attribute set related builtins:
- [ ] attrNames: `attrset A => list string`

    Returns a list of all keys in the attribute set. The order of the keys is alphabetically sorted. For example, `builtins.attrNames { y = 1; x = "foo"; }` evaluates to `[ "x" "y" ]`.

- [ ] attrValues: `attrset A => list A`

    Returns a list of all values in the attribute set. The order of the values is the same as the order of the keys returned by `builtins.attrNames`. For example, `builtins.attrValues { y = 1; x = "foo"; }` evaluates to `[ "foo" 1 ]`.

- [ ] intersectAttrs: `attrset A => attrset B => attrset B`

    Returns a new attribute set containing only the attributes that are present in both input attribute sets. The values are taken from the second attribute set. For example, `builtins.intersectAttrs { x = 1; y = 2; } { y = 3; z = 4; }` evaluates to `{ y = 3; }`.

- [ ] catAttrs *name* *list*: `string => list attrset A => list A`

    Collects all attributes with the *name* from the list of attribute sets. Equivalent to `map (x: x.name) (filter (x: x ? name) list)`.

- [ ] listToAttrs: `list attrset A => attrset A`

    Construct a set from a list of `name` and `value` attribute sets. First occurrence of a name takes precedence in case of duplicates.
    For example, `builtins.listToAttrs [ { name = "x"; value = 1; } { name = "y"; value = 2; } { name = "x"; value = 3; } ]` evaluates to `{ x = 1; y = 2; }`.

- [ ] removeAttrs: `attrset A => list string => attrset A`

    Returns a new attribute set with the specified attributes removed. For example, `builtins.removeAttrs { x = 1; y = 2; z = 3; } [ "a" "x" "z" ]` evaluates to `{ y = 2; }`.

Lambda related builtins:
- [ ] functionArgs: `lambda => attrset`

    Returns a set containing the names of the formal arguments expected by the lambda. The keys are the argument names, and the values are a boolean indicating whether the argument has a default value.
    For example, `builtins.functionArgs ({ x, y ? 123}: ...)` evaluates to `{ x = false; y = true; }`.

    "Formal argument" here refers to the attributes pattern-matched by the function. Plain lambdas are not included, e.g. `functionArgs (x: ...)` evaluates to `{}`.