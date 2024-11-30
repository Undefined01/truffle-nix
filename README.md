# Truffle Nix

Truffle Nix is a [GraalVM](http://graalvm.org/) implementation of the [Nix programming language](https://nix.dev/manual/nix/2.18/language/).
It is work in progress and not yet feature complete.
You can find the current status of the implementation in the [features](#Supported%20Features) section.

## Building

To build the project, you need to have GraalVM installed.

```bash
$ ./gradlew build
$ ./gradlew test
```

## Supported Features

#### Primitive Types

Nix has serveral primitive types, Any valid nix expression will evaluate to one of these types:
- [x] `int` (64-bit signed integer)
- [x] `float` (64-bit floating point number)
- [ ] `boolean`
    - Note that `true` and `false` are not keywords in Nix, but are just variables that are bound to the boolean values. You can access them in `builtins.true` and `builtins.false`.
- [x] `string`
- [ ] `lambda` (a function that takes one argument and returns any nix type)
- [ ] `attrset` (a set of key-value pairs, the keys are strings and the values can be any nix type)
- [ ] `list` (a list of nix types, the elements can be of different types)
- [ ] `path`
- [ ] `null`

#### Operators

- [x] string concatenation: `"hello" + "world"`
- [x] arithmetic operators for integers and floats: `1 + 2`, `3. - 4.`, `5 * 6.`, `7 / 8`
    - Integer division like `7 / 8` will be rounded towards zero
- [ ] comparison operators for integers and floats: `1 < 2`, `3 <= 4`, `5 > 6`, `7 >= 8`, `9 == 10`, `11 != 12`
- [ ] comparison operators for strings: `"a" < "b"`, `"c" <= "d"`, `"e" > "f"`, `"g" >= "h"`, `"i" == "j"`, `"k" != "l"`
    - Strings are compared lexicographically
- [ ] boolean negation: `!true`
- [ ] boolean operators: `true && false`, `true || false`
    - The `&&` and `||` operators are short-circuiting, meaning that the second operand is only evaluated if necessary
- [ ] attribute selection: `attrs.key`
- [ ] attribute selection with default: `attrs.key or "default"`
- [ ] attribute set extension: `attrs // { key = value; }`
- [ ] list concatenation: `[1 2] ++ [3 4]`

#### String

- [x] basic string: `"hello"`
    - basic string can cross multiple lines, and all whitespace characters are preserved. All line breaks (CR/LF/CRLF) in the string are normalized to `\n`. But the line break produced by the escape sequence are left as is, e.g. `"hello\r\nworld"` will be evaluated to the string `hello\r\nworld` with CR and LF characters.
- [x] string escaping: `"\"hello\"\n"` (evaluates to the string `hello` with a newline character)
    - String escaping only supports limited escape sequences, like `\"`, `\r`, `\n`, `\t`. Other characters following a backslash are treated as is, e.g. `"\a"` will be treated as the string `a`.
- [x] string interpolation: `"hello ${"world ${ "!" }"}"`
- [ ] multi-line string: `''hello''`
    - Multi-line strings are strings that remove the common indentation from all lines.
        For example, the following indented string:
        ```nix
          ''
            hello
              world
          ''
        ```
        will be evaluated to the string `"hello\n  world\n"`.
        For more information, see the test cases in `StringTest.java`.

#### Expressions

- [x] lambda application: `builtins.typeOf 1` (evaluates to string `int`)
- [ ] lambda expression: `x: x + 1`
    - Every lambda expression takes exactly one argument.
    - [ ] curried lambda: `x: y: x + y`
    - [ ] parameter unpacking: `{ x, y }: x + y`
        The argument must be an attribute set with the keys `x` and `y`. `x` and `y` are added to the scope of the lambda's body.
    - [ ] parameter unpacking with default values: `{ x, y ? 2 }: x + y`
        The argument must be an attribute set with the key `x` and an optional key `y`. `x` and `y` are added to the scope of the lambda's body and `y` defaults to 2 if not provided.
    - [ ] parameter unpacking with rest argument: `{ x, ... }: x`.
        The argument must be an attribute set with the key `x` and may have additional keys. Only `x` is added to the scope of the lambda's body.
    - [ ] parameter unpacking with whole attribute set: `{ x, ... } @ args: assert args.x == x` and `args @ { x, ... }: args.x == x`
        The argument must be an attribute set with the key `x` and may have additional keys. The whole attribute set named `args` and `x` are added to the scope of the lambda's body.
- [ ] conditional expression: `if true then 1 else 2` (evaluates to 1)
- [ ] let expression: `let x = 1; in x + 2` (evaluates to 3)
- [ ] with expression: `with { x = 1; }; x + 2` (evaluates to 3)
- [ ] recursive attribute set: `rec { x = 1; y = x + 1; }` (evaluates to `{ x = 1; y = 2; }`)
- [ ] abort expression: `abort "error message"` (aborts evaluation with an error message)

#### builtins

- [x] typeOf
- [x] assert