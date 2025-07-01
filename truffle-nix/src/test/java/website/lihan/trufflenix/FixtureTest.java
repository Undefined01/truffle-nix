package website.lihan.trufflenix;

import org.graalvm.polyglot.Value;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class FixtureTest extends TruffleTestBase {
  @Test
  public void testOverride() {
    Value result;
    result = this.context.eval("nix",
        """
        let
            makeOverridable =
                f: originalArgs:
                let
                    originalResult = f originalArgs;
                in
                    originalResult // {
                        override = newArgs:
                            f (originalArgs // newArgs);
                    }
            f = { a, b }: { a = a; b = b; c = a + b; };
            res1 = f { a = 1; b = 2; };
            res2 = makeOverridable f { a = 1; b = 2; };
            res3 = res2.override { a = 3; }
        in
            {
                inherit res1 res2 res3;
            }
        """);
    assertEquals(result.getMember("res1").getMember("a").asLong(), 1);
    assertEquals(result.getMember("res1").getMember("b").asLong(), 2);
    assertEquals(result.getMember("res1").getMember("c").asLong(), 3);
    assertEquals(result.getMember("res2").getMember("a").asLong(), 1);
    assertEquals(result.getMember("res2").getMember("b").asLong(), 2);
    assertEquals(result.getMember("res2").getMember("c").asLong(), 3);
    assertTrue(result.getMember("res2").hasMember("override"));
    assertEquals(result.getMember("res3").getMember("a").asLong(), 3);
    assertEquals(result.getMember("res3").getMember("b").asLong(), 2);
    assertEquals(result.getMember("res3").getMember("c").asLong(), 5);
    assertTrue(!result.getMember("res3").hasMember("override"));

    result = this.context.eval("nix",
        """
        let
            makeOverridable =
                f: originalArgs:
                let
                    originalResult = f originalArgs;
                in
                    originalResult // {
                        override = newArgs:
                            makeOverridable f (originalArgs // newArgs);
                    };
            f = { a, b, ... }: { a = a; b = b; c = a + b; };
            res1 = f { a = 1; b = 2; };
            res2 = makeOverridable f { a = 1; b = 2; };
            res3 = res2.override { a = 3; };
            res4 = res3.override { b = 4; };
            res5 = res4.override { c = 5; };
        in
            {
                inherit res1 res2 res3 res4 res5;
            }
        """);
    assertEquals(result.getMember("res1").getMember("a").asLong(), 1);
    assertEquals(result.getMember("res1").getMember("b").asLong(), 2);
    assertEquals(result.getMember("res1").getMember("c").asLong(), 3);
    assertEquals(result.getMember("res2").getMember("a").asLong(), 1);
    assertEquals(result.getMember("res2").getMember("b").asLong(), 2);
    assertEquals(result.getMember("res2").getMember("c").asLong(), 3);
    assertTrue(result.getMember("res2").hasMember("override"));
    assertEquals(result.getMember("res3").getMember("a").asLong(), 3);
    assertEquals(result.getMember("res3").getMember("b").asLong(), 2);
    assertEquals(result.getMember("res3").getMember("c").asLong(), 5);
    assertTrue(result.getMember("res3").hasMember("override"));
    assertEquals(result.getMember("res4").getMember("a").asLong(), 3);
    assertEquals(result.getMember("res4").getMember("b").asLong(), 4);
    assertEquals(result.getMember("res4").getMember("c").asLong(), 7);
    assertTrue(result.getMember("res4").hasMember("override"));
    assertEquals(result.getMember("res5").getMember("a").asLong(), 3);
    assertEquals(result.getMember("res5").getMember("b").asLong(), 4);
    assertEquals(result.getMember("res5").getMember("c").asLong(), 7);
    assertTrue(result.getMember("res5").hasMember("override"));
  }

  @Test
  public void testFixture() {
    Value result;
    result = this.context.eval("nix",
        """
        let
            pkgs = self: {
                a = 1;
                b = 2;
                c = self.a + self.b;
            };
            res = pkgs res;
        in
            res
        """);
    assertTrue(result.hasMembers());
    assertEquals(result.getMember("a").asLong(), 1);
    assertEquals(result.getMember("b").asLong(), 2);
    assertEquals(result.getMember("c").asLong(), 3);

    result = this.context.eval("nix",
        """
        let
            pkgs = self: { a = 1; b = 2; c = self.a + self.b; };
            res1 = pkgs (res1 // { a = 3; });
        in
            res1
        """);
    assertTrue(result.hasMembers());
    assertEquals(result.getMember("a").asLong(), 1);
    assertEquals(result.getMember("b").asLong(), 2);
    assertEquals(result.getMember("c").asLong(), 5);

    result = this.context.eval("nix",
        """
        let
            pkgs = self: { a = 1; b = 2; c = self.a + self.b; };
            res1 = (pkgs res1) // { a = 3; };
        in
            res1
        """);
    assertTrue(result.hasMembers());
    assertEquals(result.getMember("a").asLong(), 3);
    assertEquals(result.getMember("b").asLong(), 2);
    assertEquals(result.getMember("c").asLong(), 5);

    result = this.context.eval("nix",
        """
        let
            pkgs = self: { a = 1; b = 2; c = self.a + self.b; };
            res1 = (pkgs res1) // { c = 7; };
        in
            res1
        """);
    assertTrue(result.hasMembers());
    assertEquals(result.getMember("a").asLong(), 3);
    assertEquals(result.getMember("b").asLong(), 2);
    assertEquals(result.getMember("c").asLong(), 7);
  }

  @Test
  public void testOverlay() {
    Value result;
    result = this.context.eval("nix",
        """
        let
          makeOverridable =
            f: overrideSet:
            let
              result = (f result) // overrideSet;
            in
            result // {
              override = newOverrideSet:
                makeOverridable f (overrideSet // newOverrideSet);
            };
          pkgs = self: { a = 1; b = 2; c = self.a + self.b; };
          res1 = makeOverridable pkgs {};
          res2 = res1.override { a = 3; };
          res3 = res2.override { b = 4; };
          res4 = res3.override { b = 5; };
          res5 = res4.override { b = 6; };
          res6 = res5.override { c = 5; };
          res7 = res6.override { d = 7; };
        in
        { inherit res1 res2 res3 res4 res5 res6 res7; }
        """);
    assertTrue(result.hasMembers());
    assertEquals(result.getMember("res1").getMember("a").asLong(), 1);
    assertEquals(result.getMember("res1").getMember("b").asLong(), 2);
    assertEquals(result.getMember("res1").getMember("c").asLong(), 3);
    assertTrue(result.getMember("res1").hasMember("override"));
    assertEquals(result.getMember("res2").getMember("a").asLong(), 3);
    assertEquals(result.getMember("res2").getMember("b").asLong(), 2);
    assertEquals(result.getMember("res2").getMember("c").asLong(), 5);
    assertTrue(result.getMember("res2").hasMember("override"));
    assertEquals(result.getMember("res3").getMember("a").asLong(), 3);
    assertEquals(result.getMember("res3").getMember("b").asLong(), 4);
    assertEquals(result.getMember("res3").getMember("c").asLong(), 7);
    assertTrue(result.getMember("res3").hasMember("override"));
    assertEquals(result.getMember("res4").getMember("a").asLong(), 3);
    assertEquals(result.getMember("res4").getMember("b").asLong(), 5);
    assertEquals(result.getMember("res4").getMember("c").asLong(), 8);
    assertTrue(result.getMember("res4").hasMember("override"));
    assertEquals(result.getMember("res5").getMember("a").asLong(), 3);
    assertEquals(result.getMember("res5").getMember("b").asLong(), 6);
    assertEquals(result.getMember("res5").getMember("c").asLong(), 9);
    assertTrue(result.getMember("res5").hasMember("override"));
    assertEquals(result.getMember("res6").getMember("a").asLong(), 3);
    assertEquals(result.getMember("res6").getMember("b").asLong(), 6);
    assertEquals(result.getMember("res6").getMember("c").asLong(), 5);
    assertTrue(result.getMember("res6").hasMember("override"));
    assertEquals(result.getMember("res7").getMember("a").asLong(), 3);
    assertEquals(result.getMember("res7").getMember("b").asLong(), 6);
    assertEquals(result.getMember("res7").getMember("c").asLong(), 5);
    assertEquals(result.getMember("res7").getMember("d").asLong(), 7);
    assertTrue(result.getMember("res7").hasMember("override"));

    result = this.context.eval("nix",
        """
        let
          makeOverridable =
            f:
            let
              result = f result;
            in
              result // {
                override = overrideSet:
                  makeOverridable (final: (f final) // overrideSet);
              };
          pkgs = self: { a = 1; b = 2; c = self.a + self.b; };
          res1 = makeOverridable pkgs;
          res2 = res1.override { a = 3; };
          res3 = res2.override { b = 4; };
          res4 = res3.override { b = 5; };
          res5 = res4.override { b = 6; };
          res6 = res5.override { c = 5; };
          res7 = res6.override { d = 7; };
        in
        { inherit res1 res2 res3 res4 res5 res6 res7; }
        """);
    assertTrue(result.hasMembers());
    assertEquals(result.getMember("res1").getMember("a").asLong(), 1);
    assertEquals(result.getMember("res1").getMember("b").asLong(), 2);
    assertEquals(result.getMember("res1").getMember("c").asLong(), 3);
    assertTrue(result.getMember("res1").hasMember("override"));
    assertEquals(result.getMember("res2").getMember("a").asLong(), 3);
    assertEquals(result.getMember("res2").getMember("b").asLong(), 2);
    assertEquals(result.getMember("res2").getMember("c").asLong(), 5);
    assertTrue(result.getMember("res2").hasMember("override"));
    assertEquals(result.getMember("res3").getMember("a").asLong(), 3);
    assertEquals(result.getMember("res3").getMember("b").asLong(), 4);
    assertEquals(result.getMember("res3").getMember("c").asLong(), 7);
    assertTrue(result.getMember("res3").hasMember("override"));
    assertEquals(result.getMember("res4").getMember("a").asLong(), 3);
    assertEquals(result.getMember("res4").getMember("b").asLong(), 5);
    assertEquals(result.getMember("res4").getMember("c").asLong(), 8);
    assertTrue(result.getMember("res4").hasMember("override"));
    assertEquals(result.getMember("res5").getMember("a").asLong(), 3);
    assertEquals(result.getMember("res5").getMember("b").asLong(), 6);
    assertEquals(result.getMember("res5").getMember("c").asLong(), 9);
    assertTrue(result.getMember("res5").hasMember("override"));
    assertEquals(result.getMember("res6").getMember("a").asLong(), 3);
    assertEquals(result.getMember("res6").getMember("b").asLong(), 6);
    assertEquals(result.getMember("res6").getMember("c").asLong(), 5);
    assertTrue(result.getMember("res6").hasMember("override"));
    assertEquals(result.getMember("res7").getMember("a").asLong(), 3);
    assertEquals(result.getMember("res7").getMember("b").asLong(), 6);
    assertEquals(result.getMember("res7").getMember("c").asLong(), 5);
    assertEquals(result.getMember("res7").getMember("d").asLong(), 7);
    assertTrue(result.getMember("res7").hasMember("override"));


    result = this.context.eval("nix",
        """
        let
          makeOverridable =
            f:
            let
              result = f result;
            in
              result // {
                override = overrideF:
                  makeOverridable (final: (f final) // (overrideF final));
              };
          pkgs = final: { a = 1; b = 2; c = final.a + final.b; };
          res1 = makeOverridable pkgs;
          res2 = res1.override (final: { a = 3; });
          res3 = res2.override (final: { b = 4; });
          res4 = res3.override (final: { c = final.a; });
          res5 = res3.override (final: { c = final.b; });
          res6 = res5.override (final: { d = final.a + final.b + final.c; });
          res7 = res6.override (final: { a = 1; b = 2; });
        in
        { inherit res1 res2 res3 res4 res5 res6 res7; }
        """);
    assertTrue(result.hasMembers());
    assertEquals(result.getMember("res1").getMember("a").asLong(), 1);
    assertEquals(result.getMember("res1").getMember("b").asLong(), 2);
    assertEquals(result.getMember("res1").getMember("c").asLong(), 3);
    assertTrue(result.getMember("res1").hasMember("override"));
    assertEquals(result.getMember("res2").getMember("a").asLong(), 3);
    assertEquals(result.getMember("res2").getMember("b").asLong(), 2);
    assertEquals(result.getMember("res2").getMember("c").asLong(), 5);
    assertTrue(result.getMember("res2").hasMember("override"));
    assertEquals(result.getMember("res3").getMember("a").asLong(), 3);
    assertEquals(result.getMember("res3").getMember("b").asLong(), 4);
    assertEquals(result.getMember("res3").getMember("c").asLong(), 7);
    assertTrue(result.getMember("res3").hasMember("override"));
    assertEquals(result.getMember("res4").getMember("a").asLong(), 3);
    assertEquals(result.getMember("res4").getMember("b").asLong(), 4);
    assertEquals(result.getMember("res4").getMember("c").asLong(), 3);
    assertTrue(result.getMember("res4").hasMember("override"));
    assertEquals(result.getMember("res5").getMember("a").asLong(), 3);
    assertEquals(result.getMember("res5").getMember("b").asLong(), 4);
    assertEquals(result.getMember("res5").getMember("c").asLong(), 4);
    assertTrue(result.getMember("res5").hasMember("override"));
    assertEquals(result.getMember("res6").getMember("a").asLong(), 3);
    assertEquals(result.getMember("res6").getMember("b").asLong(), 4);
    assertEquals(result.getMember("res6").getMember("c").asLong(), 4);
    assertEquals(result.getMember("res6").getMember("d").asLong(), 11);
    assertTrue(result.getMember("res6").hasMember("override"));
    assertEquals(result.getMember("res7").getMember("a").asLong(), 1);
    assertEquals(result.getMember("res7").getMember("b").asLong(), 2);
    assertEquals(result.getMember("res7").getMember("c").asLong(), 2);
    assertEquals(result.getMember("res7").getMember("d").asLong(), 5);
    assertTrue(result.getMember("res7").hasMember("override"));

    result = this.context.eval("nix",
        """
        let
          makeOverridable =
            f:
            let
              final = f final;
            in
            final // {
              override = overrideF:
                makeOverridable (final:
                  let
                    prev = f final;
                    newFinal = prev // overrideF final prev;
                  in
                  newFinal
                );
            };
          pkgs = final: { a = 1; b = 2; c = final.a + final.b; };
          res1 = makeOverridable pkgs;
          res2 = res1.override (final: prev: { a = 3; });
          res3 = res2.override (final: prev: { a = 4; d = prev.a; e = prev.c; });
          res4 = res3.override (final: prev: { a = 5; });
          res5 = res4.override (final: prev: { a = 6; b = prev.a; });
        in
        { inherit res1 res2 res3 res4 res5; }
        """);
    assertEquals(result.getMember("res1").getMember("a").asLong(), 1);
    assertEquals(result.getMember("res1").getMember("b").asLong(), 2);
    assertEquals(result.getMember("res1").getMember("c").asLong(), 3);
    assertTrue(result.getMember("res1").hasMember("override"));
    assertEquals(result.getMember("res2").getMember("a").asLong(), 3);
    assertEquals(result.getMember("res2").getMember("b").asLong(), 2);
    assertEquals(result.getMember("res2").getMember("c").asLong(), 5);
    assertTrue(result.getMember("res2").hasMember("override"));
    assertEquals(result.getMember("res3").getMember("a").asLong(), 3);
    assertEquals(result.getMember("res3").getMember("b").asLong(), 2);
    assertEquals(result.getMember("res3").getMember("c").asLong(), 5);
    assertEquals(result.getMember("res3").getMember("d").asLong(), 3);
    assertEquals(result.getMember("res3").getMember("e").asLong(), 5);
    assertTrue(result.getMember("res3").hasMember("override"));
    assertEquals(result.getMember("res4").getMember("a").asLong(), 5);
    assertEquals(result.getMember("res4").getMember("b").asLong(), 2);
    assertEquals(result.getMember("res4").getMember("c").asLong(), 7);
    assertEquals(result.getMember("res4").getMember("d").asLong(), 3);
    assertEquals(result.getMember("res4").getMember("e").asLong(), 7);
    assertTrue(result.getMember("res4").hasMember("override"));
    assertEquals(result.getMember("res5").getMember("a").asLong(), 6);
    assertEquals(result.getMember("res5").getMember("b").asLong(), 5);
    assertEquals(result.getMember("res5").getMember("c").asLong(), 11);
    assertEquals(result.getMember("res5").getMember("d").asLong(), 3);
    assertEquals(result.getMember("res5").getMember("e").asLong(), 11);
    assertTrue(result.getMember("res5").hasMember("override"));
  }
}
