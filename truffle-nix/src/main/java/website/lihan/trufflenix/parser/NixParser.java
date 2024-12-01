package website.lihan.trufflenix.parser;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.source.Source;
import io.github.treesitter.jtreesitter.Language;
import io.github.treesitter.jtreesitter.Node;
import io.github.treesitter.jtreesitter.Parser;
import io.github.treesitter.jtreesitter.Point;
import io.github.treesitter.jtreesitter.Tree;
import io.github.treesitter.jtreesitter.TreeCursor;
import java.util.ArrayList;
import java.util.List;
import org.graalvm.collections.Pair;
import website.lihan.treesitternix.TreeSitterNix;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.expressions.GlobalVarReferenceNodeGen;
import website.lihan.trufflenix.nodes.expressions.LambdaApplicationNode;
import website.lihan.trufflenix.nodes.expressions.LambdaNode;
import website.lihan.trufflenix.nodes.expressions.LetExpressionNode;
import website.lihan.trufflenix.nodes.expressions.LocalVarReferenceNodeGen;
import website.lihan.trufflenix.nodes.expressions.StringExpressionNode;
import website.lihan.trufflenix.nodes.expressions.VariableBindingNode;
import website.lihan.trufflenix.nodes.expressions.VariableBindingNodeGen;
import website.lihan.trufflenix.nodes.literals.FloatLiteralNode;
import website.lihan.trufflenix.nodes.literals.IntegerLiteralNode;
import website.lihan.trufflenix.nodes.literals.StringLiteralNode;
import website.lihan.trufflenix.nodes.operators.AddNodeGen;
import website.lihan.trufflenix.nodes.operators.DivNodeGen;
import website.lihan.trufflenix.nodes.operators.MulNodeGen;
import website.lihan.trufflenix.nodes.operators.SubNodeGen;
import website.lihan.trufflenix.nodes.operators.UnaryMinusNodeGen;

public class NixParser {

  private static Parser parser;

  static {
    parser = new Parser(new Language(TreeSitterNix.language()));
  }

  private Tree tree;
  private String fileText;
  private TreeCursor cursor;

  private FrameDescriptor.Builder frameDescriptorBuilder = FrameDescriptor.newBuilder();
  private LocalScope localScope = null;
  private int variableId = 0;

  private NixParser(Tree tree, String fileText) {
    this.tree = tree;
    this.fileText = fileText;
    this.cursor = tree.walk();
  }

  public static Pair<NixNode, FrameDescriptor> parse(Source source) {
    var fileText = source.getCharacters().toString();
    var tree = parser.parse(fileText).orElseThrow();
    var rootNode = tree.getRootNode();
    if (rootNode.hasError()) {
      throw new ParseError("Syntax error");
    }

    var analyzer = new NixParser(tree, fileText);
    var nixNode = analyzer.analyze();
    if (analyzer.variableId == 0) {
      return Pair.create(nixNode, analyzer.frameDescriptorBuilder.build());
    }
    var frameDescriptor = analyzer.frameDescriptorBuilder.build();
    return Pair.create(nixNode, frameDescriptor);
  }

  private NixNode analyze() {
    var node = cursor.getCurrentNode();
    String nodeKind = node.getType();

    switch (nodeKind) {
      case "source_code":
      case "parenthesized_expression":
        {
          CursorUtil.gotoFirstNamedChild(cursor);
          var result = analyze();
          cursor.gotoParent();
          return result;
        }

      case "integer_expression":
        return new IntegerLiteralNode(node.getText());
      case "float_expression":
        return new FloatLiteralNode(node.getText());
      case "string_expression":
        return analyzeStringExpression();

      case "unary_expression":
        return analyzeUnaryExpression();
      case "binary_expression":
        return analyzeBinaryExpression();

      case "variable_expression":
      // TODO: select_expression like `a.b`
      case "select_expression":
        {
          Integer variableId = null;
          if (localScope != null) {
            variableId = localScope.getVariableId(node.getText());
          }
          if (variableId == null) {
            return GlobalVarReferenceNodeGen.create(node.getText());
          } else {
            return LocalVarReferenceNodeGen.create(variableId);
          }
        }

      case "apply_expression":
        return analyzeApplyExpression();

      case "let_expression":
        return analyzeLetExpression();

      case "function_expression":
        return analyzeFunctionExpression();

      // case "if_expression":
      //     return analyzeIfExpression();
      default:
        {
          Point start = node.getStartPoint();
          throw new ParseError(
              "Unknown AST node " + nodeKind + " at " + start.row() + ":" + start.column());
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
        throw new ParseError(
            "Unknown operator " + operatorKind + " at " + node.getChild(0).get().getStartPoint());
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
      case "-":
        return SubNodeGen.create(left, right);
      case "*":
        return MulNodeGen.create(left, right);
      case "/":
        return DivNodeGen.create(left, right);
      // case "==":
      //     return new EqualityNode(left, right);
      default:
        throw new ParseError(
            "Unknown operator " + operatorKind + " at " + node.getChild(1).get().getStartPoint());
    }
  }

  public NixNode analyzeStringExpression() {
    assert cursor.getCurrentNode().getType().equals("string_expression")
        : "Expected string_expression node, got " + cursor.getCurrentNode().getType();

    var parts = new ArrayList<NixNode>();
    for (Node child : CursorUtil.namedChildren(cursor)) {
      String childNodeKind = child.getType();
      switch (childNodeKind) {
        case "string_fragment":
          parts.add(StringLiteralNode.fromStringFracment(child.getText()));
          break;
        case "escape_sequence":
          parts.add(StringLiteralNode.fromEscapedSequence(child.getText()));
          break;
        case "interpolation":
          {
            var oldCursor = cursor;
            cursor = child.getNamedChild(0).get().walk();
            parts.add(analyze());
            cursor = oldCursor;
            break;
          }
        default:
          throw new ParseError(
              "Unknown AST node " + childNodeKind + " at " + child.getStartPoint());
      }
    }

    if (parts.size() == 1) {
      return parts.get(0);
    } else {
      return new StringExpressionNode(parts.toArray(new NixNode[0]));
    }
  }

  public NixNode analyzeApplyExpression() {
    Node node = cursor.getCurrentNode();
    assert node.getType().equals("apply_expression");
    assert node.getNamedChildCount() == 2;

    CursorUtil.gotoFirstNamedChild(cursor);
    NixNode function = analyze();
    CursorUtil.gotoNextNamedSibling(cursor);
    NixNode argument = analyze();
    cursor.gotoParent();

    return new LambdaApplicationNode(function, argument);
  }

  public NixNode analyzeLetExpression() {
    Node node = cursor.getCurrentNode();
    assert node.getType().equals("let_expression");

    var tmpCursor = cursor;
    var lastLocalScope = localScope;
    localScope = new LocalScope(lastLocalScope);

    CursorUtil.gotoFirstNamedChild(cursor);
    assert cursor.getCurrentNode().getType().equals("binding_set");
    List<VariableBindingNode> bindings = new ArrayList<>();
    var bindingAstChildren = CursorUtil.namedChildren(cursor);
    for (Node child : CursorUtil.namedChildren(cursor)) {
      switch (child.getType()) {
        case "binding":
          {
            cursor = child.walk();
            CursorUtil.gotoFirstNamedChild(cursor);
            // TODO: handle attrpath
            String bindingName = cursor.getCurrentNode().getText();
            CursorUtil.gotoNextNamedSibling(cursor);
            NixNode bindingValue = analyze();
            cursor.gotoParent();
            var slotId =
                frameDescriptorBuilder.addSlot(
                    FrameSlotKind.Illegal, bindingName + variableId, null);
            variableId++;
            localScope.newVariable(bindingName, slotId);
            bindings.add(VariableBindingNodeGen.create(bindingValue, slotId));
            break;
          }
        default:
          throw new ParseError(
              "Unknown AST node " + child.getType() + " at " + child.getStartPoint());
      }
    }

    cursor = tmpCursor;

    CursorUtil.gotoNextNamedSibling(cursor);
    NixNode body = analyze();
    assert !CursorUtil.gotoNextNamedSibling(cursor);
    cursor.gotoParent();

    localScope = lastLocalScope;

    return new LetExpressionNode(bindings.toArray(new VariableBindingNode[0]), body);
  }

  public NixNode analyzeFunctionExpression() {
    Node node = cursor.getCurrentNode();
    assert node.getType().equals("function_expression");
    assert node.getNamedChildCount() == 2
        : "Expected 2 children for function expression, got " + node.getNamedChildCount();

    var parentFrameBuilder = frameDescriptorBuilder;
    frameDescriptorBuilder = FrameDescriptor.newBuilder();

    var lastLocalScope = localScope;
    localScope = new LocalScope(null);

    CursorUtil.gotoFirstNamedChild(cursor);
    assert cursor.getCurrentNode().getType().equals("identifier");
    String parameterName = cursor.getCurrentNode().getText();
    var slotId = frameDescriptorBuilder.addSlot(FrameSlotKind.Illegal, parameterName, null);
    localScope.newVariable(parameterName, slotId);
    var parameterUnpackNode = new LambdaNode.ParameterUnpackNode(null, slotId);
    variableId++;
    CursorUtil.gotoNextNamedSibling(cursor);
    NixNode body = analyze();
    cursor.gotoParent();

    var frameDescriptor = frameDescriptorBuilder.build();
    frameDescriptorBuilder = parentFrameBuilder;
    localScope = lastLocalScope;

    return new LambdaNode(
        frameDescriptor, new LambdaNode.ParameterUnpackNode[] {parameterUnpackNode}, body);
  }

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
