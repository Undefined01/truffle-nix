package website.lihan.trufflenix.parser;

import io.github.treesitter.jtreesitter.Node;
import io.github.treesitter.jtreesitter.TreeCursor;

import com.oracle.truffle.api.source.Source;

import io.github.treesitter.jtreesitter.Language;
import io.github.treesitter.jtreesitter.Parser;
import io.github.treesitter.jtreesitter.Point;
import io.github.treesitter.jtreesitter.Tree;
import website.lihan.treesitternix.TreeSitterNix;
import website.lihan.trufflenix.nodes.literals.FloatLiteralNode;
import website.lihan.trufflenix.nodes.literals.IntegerLiteralNode;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.operators.AddNodeGen;
import website.lihan.trufflenix.nodes.operators.UnaryMinusNodeGen;

public class NixParser {

    private static Parser parser;

    static {
        parser = new Parser(new Language(TreeSitterNix.language()));
    }

    private Tree tree;
    private String fileText;
    private TreeCursor cursor;

    private NixParser(Tree tree, String fileText) {
        this.tree = tree;
        this.fileText = fileText;
        this.cursor = tree.walk();
    }

    public static NixNode parse(Source source) {
        var fileText = source.getCharacters().toString();
        var tree = parser.parse(fileText).orElseThrow();
        var rootNode = tree.getRootNode();
        if (rootNode.hasError()) {
            throw new ParseError("Syntax error");
        }

        var analyzer = new NixParser(tree, fileText);
        return analyzer.analyze();
    }

    private NixNode analyze() {
        var node = cursor.getCurrentNode();
        String nodeKind = node.getType();

        switch (nodeKind) {
            case "source_code":
            case "parenthesized_expression": {
                CursorUtil.gotoFirstNamedChild(cursor);
                var result = analyze();
                cursor.gotoParent();
                return result;
            }

            case "integer_expression": {
                String nodeText = node.getText();
                return new IntegerLiteralNode(nodeText);
            }
            case "float_expression": {
                String nodeText = node.getText();
                return new FloatLiteralNode(nodeText);
            }
            // case "variable_expression":
            //     nodeText = node.getText();
            //     return new VariableNode(nodeText);
            // case "string_expression":
            //     return analyzeStringExpression();
            case "unary_expression":
                return analyzeUnaryExpression();
            case "binary_expression":
                return analyzeBinaryExpression();
            // case "apply_expression":
            //     return analyzeApplyExpression();
            // case "function_expression":
            //     return analyzeFunctionExpression();
            // case "let_expression":
            //     return analyzeLetExpression();
            // case "if_expression":
            //     return analyzeIfExpression();
            default: {
                Point start = node.getStartPoint();
                throw new ParseError("Unknown AST node " + nodeKind + " at " + start.row() + ":" + start.column());
            }
        }
    }

    private NixNode analyzeUnaryExpression() {
        Node node = cursor.getCurrentNode();
        assert node.getType().equals("unary_expression");
        assert node.getChildCount() == 2;

        cursor.gotoFirstChild();
        String operatorKind = cursor.getCurrentNode().getText();
        cursor.gotoNextSibling();
        NixNode operand = analyze();
        cursor.gotoParent();

        switch (operatorKind) {
            case "-":
                return UnaryMinusNodeGen.create(operand);
            default:
                throw new ParseError("Unknown operator " + operatorKind + " at " + node.getChild(0).get().getStartPoint());
        }
    }

    public NixNode analyzeBinaryExpression() {
        Node node = cursor.getCurrentNode();
        assert node.getType().equals("binary_expression");
        assert node.getChildCount() == 3;

        cursor.gotoFirstChild();
        NixNode left = analyze();
        cursor.gotoNextSibling();
        String operatorKind = cursor.getCurrentNode().getText();
        cursor.gotoNextSibling();
        NixNode right = analyze();
        cursor.gotoParent();

        switch (operatorKind) {
            case "+":
                return AddNodeGen.create(left, right);
            // case "-":
            //     return new SubtractionNode(left, right);
            // case "*":
            //     return new MultiplicationNode(left, right);
            // case "/":
            //     return new DivisionNode(left, right);
            // case "==":
            //     return new EqualityNode(left, right);
            default:
                throw new ParseError("Unknown operator " + operatorKind + " at " + node.getChild(1).get().getStartPoint());
        }
    }

    // public NixNode analyzeStringExpression() {
    //     assert cursor.getCurrentNode().getType().equals("binary_expression");

    //     List<NixNode> parts = new ArrayList<>();
    //     for (Node child : cursor.namedChildren()) {
    //         String childNodeKind = child.getType();
    //         switch (childNodeKind) {
    //             case "string_fragment":
    //                 parts.add(new StringLiteralNode(child.getText()));
    //                 break;
    //             case "interpolation":
    //                 CursorUtil.gotoFirstNamedChild(cursor);
    //                 parts.add(new StringInterpolationNode(analyze()));
    //                 cursor.gotoParent();
    //                 break;
    //             case "escape_sequence":
    //                 parts.add(StringLiteralNode.fromEscapeSequence(child.getText()));
    //                 break;
    //             default:
    //                 throw new ParseError("Unknown AST node " + childNodeKind + " at " + child.startPoint());
    //         }
    //     }
    //     return new StringNode(parts);
    // }

    // public NixNode analyzeApplyExpression() {
    //     Node node = cursor.getCurrentNode();
    //     assert node.getType().equals("apply_expression");
    //     assert node.childCount() == 2;

    //     CursorUtil.gotoFirstNamedChild(cursor);
    //     NixNode function = analyze();
    //     CursorUtil.gotoNextNamedSibling(cursor);
    //     NixNode argument = analyze();
    //     cursor.gotoParent();

    //     return new ApplyNode(function, argument);
    // }

    // public NixNode analyzeFunctionExpression() {
    //     Node node = cursor.getCurrentNode();
    //     assert node.getType().equals("function_expression");
    //     assert node.namedChildCount() == 2 : "Expected 2 children for function expression, got " + node.childCount();

    //     CursorUtil.gotoFirstNamedChild(cursor);
    //     assert cursor.getCurrentNode().getType().equals("identifier");
    //     String parameter = cursor.getCurrentNode().getText();
    //     CursorUtil.gotoNextNamedSibling(cursor);
    //     NixNode body = analyze();
    //     cursor.gotoParent();

    //     return new LambdaNode(parameter, body);
    // }

    // public NixNode analyzeLetExpression() {
    //     Node node = cursor.getCurrentNode();
    //     assert node.getType().equals("let_expression");

    //     CursorUtil.gotoFirstNamedChild(cursor);
    //     assert cursor.getCurrentNode().getType().equals("binding_set");
    //     List<Pair<NixNode, NixNode>> bindings = new ArrayList<>();
    //     for (Node child : cursor.namedChildren()) {
    //         switch (child.getType()) {
    //             case "binding":
    //                 CursorUtil.gotoFirstNamedChild(cursor);
    //                 NixNode bindingName = analyzeAttrPath();
    //                 CursorUtil.gotoNextNamedSibling(cursor);
    //                 NixNode bindingValue = analyze();
    //                 cursor.gotoParent();
    //                 bindings.add(new Pair<>(bindingName, bindingValue));
    //                 break;
    //             default:
    //                 throw new ParseError("Unknown AST node " + child.getType() + " at " + child.startPoint());
    //         }
    //     }

    //     CursorUtil.gotoNextNamedSibling(cursor);
    //     NixNode body = analyze();
    //     assert !CursorUtil.gotoNextNamedSibling(cursor);
    //     cursor.gotoParent();

    //     return new LetNode(bindings, body);
    // }

    // public NixNode analyzeAttrPath() {
    //     Node node = cursor.getCurrentNode();
    //     assert node.getType().equals("attrpath");
    //     assert node.childCount() == 1;

    //     cursor.gotoFirstChild();
    //     assert cursor.getCurrentNode().getType().equals("identifier");
    //     String path = cursor.getCurrentNode().getText();
    //     cursor.gotoParent();

    //     return new StringLiteralNode(path);
    // }

    // public NixNode analyzeIfExpression() {
    //     Node node = cursor.getCurrentNode();
    //     assert node.getType().equals("if_expression");
    //     assert node.namedChildCount() == 3;

    //     CursorUtil.gotoFirstNamedChild(cursor);
    //     NixNode condition = analyze();
    //     CursorUtil.gotoNextNamedSibling(cursor);
    //     NixNode thenBranch = analyze();
    //     CursorUtil.gotoNextNamedSibling(cursor);
    //     NixNode elseBranch = analyze();
    //     cursor.gotoParent();

    //     return new IfNode(condition, thenBranch, elseBranch);
    // }
}