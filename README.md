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
- [ ] `string`
- [ ] `path`
- [ ] `null`
- [ ] `attrset` (a set of key-value pairs, the keys are strings and the values can be any nix type)
- [ ] `list` (a list of nix types, the elements can be of different types)
- [ ] `lambda` (a function that takes one argument and returns any nix type)

#### Operators

- [ ] string concatenation: `"hello" + "world"`
- [ ] string interpolation: `"hello ${"world"} ${1 + 2}"`
- [ ] arithmetic operators for integers and floats: `1 + 2`, `3. - 4.`, `5 * 6.`, `7 / 8`
    - integer division like `7 / 8` will be rounded towards zero
- [ ] comparison operators for integers and floats: `1 < 2`, `3 <= 4`, `5 > 6`, `7 >= 8`, `9 == 10`, `11 != 12`
- [ ] comparison operators for strings: `"a" < "b"`, `"c" <= "d"`, `"e" > "f"`, `"g" >= "h"`, `"i" == "j"`, `"k" != "l"`
    - strings are compared lexicographically
- [ ] boolean negation: `!true`
- [ ] boolean operators: `true && false`, `true || false`
    - the `&&` and `||` operators are short-circuiting, meaning that the second operand is only evaluated if necessary
- [ ] attribute selection: `attrs.key`
- [ ] attribute selection with default: `attrs.key or "default"`
- [ ] attribute set extension: `attrs // { key = value; }`
- [ ] list concatenation: `[1 2] ++ [3 4]`

#### Expressions

- [ ] conditional expression: `if true then 1 else 2` (evaluates to 1)
- [ ] assert expression: `assert true; 1` (evaluates to 1)
- [ ] let expression: `let x = 1; in x + 2` (evaluates to 3)
- [ ] with expression: `with { x = 1; }; x + 2` (evaluates to 3)
- [ ] lambda expression: `x: x + 1` (evaluates to a function that adds 1 to its argument)
- [ ] function application: `(x: x + 1) 2` (evaluates to 3)
- [ ] recursive attribute set: `rec { x = 1; y = x + 1; }` (evaluates to `{ x = 1; y = 2; }`)
- [ ] abort expression: `abort "error message"` (aborts evaluation with an error message)
