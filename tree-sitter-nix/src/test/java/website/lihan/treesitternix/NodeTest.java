package website.lihan.treesitternix;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import io.github.treesitter.jtreesitter.Language;
import io.github.treesitter.jtreesitter.Parser;
import io.github.treesitter.jtreesitter.Node;
import io.github.treesitter.jtreesitter.Tree;

class NodeTest {
    private static Tree tree;
    private static Node node;

    @BeforeAll
    static void beforeAll() {
        var language = new Language(TreeSitterNix.language());
        try (var parser = new Parser(language)) {
            tree = parser.parse("{ a = 1 + 2; }").orElseThrow();
            node = tree.getRootNode();
        }
    }

    @AfterAll
    static void afterAll() {
        tree.close();
    }

    @Test
    void getTree() {
        assertSame(tree, node.getTree());
    }

    @Test
    void getId() {
        assertNotEquals(0L, node.getId());
    }

    @Test
    void getType() {
        assertEquals("source_code", node.getType());
    }

    @Test
    void getGrammarType() {
        assertEquals("source_code", node.getGrammarType());
    }

    @Test
    void isNamed() {
        assertTrue(node.isNamed());
    }

    @Test
    void isExtra() {
        assertFalse(node.isExtra());
    }

    @Test
    void isError() {
        assertFalse(node.isError());
    }

    @Test
    void isMissing() {
        assertFalse(node.isMissing());
    }

    @Test
    void hasChanges() {
        assertFalse(node.hasChanges());
    }

    @Test
    void hasError() {
        assertFalse(node.hasError());
    }

    @Test
    void getStartByte() {
        assertEquals(0, node.getStartByte());
    }

    @Test
    void getEndByte() {
        assertEquals(14, node.getEndByte());
    }
}