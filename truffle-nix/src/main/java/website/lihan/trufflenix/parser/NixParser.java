package website.lihan.trufflenix.parser;

import com.oracle.truffle.api.frame.FrameDescriptor;
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
import website.lihan.trufflenix.nodes.expressions.IfExpressionNode;
import website.lihan.trufflenix.nodes.expressions.PropertyReferenceNodeGen;
import website.lihan.trufflenix.nodes.expressions.StringExpressionNode;
import website.lihan.trufflenix.nodes.expressions.functions.FunctionApplicationNode;
import website.lihan.trufflenix.nodes.expressions.functions.FunctionApplicationNodeGen;
import website.lihan.trufflenix.nodes.expressions.functions.LambdaNode;
import website.lihan.trufflenix.nodes.expressions.letexp.AbstractBindingNode;
import website.lihan.trufflenix.nodes.expressions.letexp.LambdaBindingNode;
import website.lihan.trufflenix.nodes.expressions.letexp.LetExpressionNode;
import website.lihan.trufflenix.nodes.expressions.letexp.VariableBindingNodeGen;
import website.lihan.trufflenix.nodes.literals.AttrsetLiteralNodeGen;
import website.lihan.trufflenix.nodes.literals.FloatLiteralNode;
import website.lihan.trufflenix.nodes.literals.IntegerLiteralNode;
import website.lihan.trufflenix.nodes.literals.ListLiteralNode;
import website.lihan.trufflenix.nodes.literals.StringLiteralNode;
import website.lihan.trufflenix.nodes.operators.AddNodeGen;
import website.lihan.trufflenix.nodes.operators.CompEqNodeGen;
import website.lihan.trufflenix.nodes.operators.CompGeNodeGen;
import website.lihan.trufflenix.nodes.operators.CompGtNodeGen;
import website.lihan.trufflenix.nodes.operators.CompLeNodeGen;
import website.lihan.trufflenix.nodes.operators.CompLtNodeGen;
import website.lihan.trufflenix.nodes.operators.CompNeNodeGen;
import website.lihan.trufflenix.nodes.operators.DivNodeGen;
import website.lihan.trufflenix.nodes.operators.ListConcatNodeGen;
import website.lihan.trufflenix.nodes.operators.LogicAndNodeGen;
import website.lihan.trufflenix.nodes.operators.LogicOrNodeGen;
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

  private LocalScope localScope = LocalScope.createRootScope();

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
    var frameDescriptor = analyzer.localScope.buildFrame();
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
        {
          var slotId = localScope.getSlotId(node.getText());
          if (slotId.isEmpty()) {
            return GlobalVarReferenceNodeGen.create(node.getText());
          } else {
            return slotId.get().createReadNode();
          }
        }

      case "select_expression":
        return analyzeSelectExpression();

      case "apply_expression":
        return analyzeApplyExpression();

      case "let_expression":
        return analyzeLetExpression();

      case "function_expression":
        return analyzeFunctionExpression();

      case "if_expression":
        return analyzeIfExpression();

      case "list_expression":
        return analyzeListExpression();

      case "attrset_expression":
        return analyzeAttrSetExpression();

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

      case "==":
        return CompEqNodeGen.create(left, right);
      case "!=":
        return CompNeNodeGen.create(left, right);
      case ">":
        return CompGtNodeGen.create(left, right);
      case ">=":
        return CompGeNodeGen.create(left, right);
      case "<":
        return CompLtNodeGen.create(left, right);
      case "<=":
        return CompLeNodeGen.create(left, right);

      case "&&":
        return LogicAndNodeGen.create(left, right);
      case "||":
        return LogicOrNodeGen.create(left, right);

      case "++":
        return ListConcatNodeGen.create(left, right);
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

    var arguments = new ArrayList<NixNode>();
    
    int depth = 0;
    while (cursor.getCurrentNode().getType().equals("apply_expression")) {
      CursorUtil.ensureGotoFirstNamedChild(cursor);
      depth+=1;
    }
    NixNode function = analyze();
    for (int i = 0; i < depth; i++) {
      CursorUtil.gotoNextNamedSibling(cursor);
      arguments.add(analyze());
      cursor.gotoParent();
    }

    return FunctionApplicationNode.create(function, arguments);
  }

  public NixNode analyzeLetExpression() {
    Node node = cursor.getCurrentNode();
    assert node.getType().equals("let_expression");

    var tmpCursor = cursor;
    var parentScope = localScope;
    localScope = parentScope.newLocalScope();

    CursorUtil.gotoFirstNamedChild(cursor);
    assert cursor.getCurrentNode().getType().equals("binding_set");
    List<AbstractBindingNode> bindings = new ArrayList<>();
    var bindingAstChildren = CursorUtil.namedChildren(cursor);
    for (Node child : CursorUtil.namedChildren(cursor)) {
      switch (child.getType()) {
        case "binding":
          {
            cursor = child.walk();
            CursorUtil.gotoFirstNamedChild(cursor);
            // TODO: handle attrpath
            String bindingName = cursor.getCurrentNode().getText();
            var slotId = localScope.newVariable(bindingName);

            CursorUtil.gotoNextNamedSibling(cursor);
            NixNode bindingValue = analyze();
            cursor.gotoParent();
            if (bindingValue instanceof LambdaNode lambda) {
              bindings.add(LambdaBindingNode.create(lambda, slotId));
            } else {
              bindings.add(VariableBindingNodeGen.create(bindingValue, slotId));
            }
            break;
          }
        case "inherit":
        case "inherit_from":
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

    localScope = parentScope;
    return new LetExpressionNode(bindings.toArray(new AbstractBindingNode[0]), body);
  }

  public NixNode analyzeFunctionExpression() {
    Node node = cursor.getCurrentNode();
    assert node.getType().equals("function_expression");
    assert node.getNamedChildCount() == 2
        : "Expected 2 children for function expression, got " + node.getNamedChildCount();

    var parentScope = localScope;
    localScope = parentScope.newFrame();

    var slotInitNodes = new ArrayList<LambdaNode.SlotInitNode>();
    int argumentCount = 0;
    while (cursor.getCurrentNode().getType().equals("function_expression")) {
      CursorUtil.gotoFirstNamedChild(cursor);
      analyzeFunctionParameterUnpacking(argumentCount, slotInitNodes);
      CursorUtil.gotoNextNamedSibling(cursor);
      argumentCount += 1;
    }
    NixNode body = analyze();
    for (int i = 0; i < argumentCount; i++) {
      cursor.gotoParent();
    }

    var frameDescriptor = localScope.buildFrame();
    var slotIdInParentFrameOfCapturedVariable =
        new VariableSlot[localScope.frame.capturedVariables.size()];
    for (var i = 0; i < localScope.frame.capturedVariables.size(); i++) {
      var capturedVariable = localScope.frame.capturedVariables.get(i);
      var parentSlotId = capturedVariable.getLeft();
      var newSlotId = capturedVariable.getRight();
      slotIdInParentFrameOfCapturedVariable[i] = parentSlotId;
    }

    localScope = parentScope;

    return new LambdaNode(
        frameDescriptor,
        slotIdInParentFrameOfCapturedVariable,
        slotInitNodes.toArray(new LambdaNode.SlotInitNode[0]),
        argumentCount,
        body);
  }

  private void analyzeFunctionParameterUnpacking(int parameterIdx, List<LambdaNode.SlotInitNode> slotInitNodes) {
    assert cursor.getCurrentNode().getType().equals("identifier");
    String parameterName = cursor.getCurrentNode().getText();
    localScope.newArgument(parameterName, parameterIdx);
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

  public NixNode analyzeIfExpression() {
    Node node = cursor.getCurrentNode();
    assert node.getType().equals("if_expression");
    assert node.getNamedChildCount() == 3;

    CursorUtil.gotoFirstNamedChild(cursor);
    NixNode condition = analyze();
    CursorUtil.gotoNextNamedSibling(cursor);
    NixNode thenBranch = analyze();
    CursorUtil.gotoNextNamedSibling(cursor);
    NixNode elseBranch = analyze();
    cursor.gotoParent();

    return new IfExpressionNode(condition, thenBranch, elseBranch);
  }

  public NixNode analyzeListExpression() {
    Node node = cursor.getCurrentNode();
    assert node.getType().equals("list_expression");

    var elements = new ArrayList<NixNode>();
    var tmpCursor = cursor;
    for (Node child : CursorUtil.namedChildren(cursor)) {
      cursor = child.walk();
      elements.add(analyze());
    }
    cursor = tmpCursor;

    return new ListLiteralNode(elements);
  }

  public NixNode analyzeAttrSetExpression() {
    Node node = cursor.getCurrentNode();
    assert node.getType().equals("attrset_expression");
    CursorUtil.gotoFirstNamedChild(cursor);
    assert cursor.getCurrentNode().getType().equals("binding_set");

    var tmpCursor = cursor;
    var elements = new ArrayList<Pair<String, NixNode>>();
    for (Node child : CursorUtil.namedChildren(cursor)) {
      switch (child.getType()) {
        case "binding":
          {
            cursor = child.walk();
            CursorUtil.gotoFirstNamedChild(cursor);
            String key = cursor.getCurrentNode().getText();
            CursorUtil.gotoNextNamedSibling(cursor);
            NixNode value = analyze();
            elements.add(Pair.create(key, value));
            break;
          }
        case "inherit":
        case "inherit_from":
        default:
          throw new ParseError(
              "Unknown AST node " + child.getType() + " at " + child.getStartPoint());
      }
    }

    cursor = tmpCursor;
    cursor.gotoParent();
    return AttrsetLiteralNodeGen.create(elements);
  }

  private NixNode analyzeSelectExpression() {
    Node node = cursor.getCurrentNode();
    assert node.getType().equals("select_expression");
    assert node.getNamedChildCount() == 2;

    CursorUtil.ensureGotoFirstNamedChild(cursor);
    NixNode target = analyze();
    CursorUtil.ensureGotoNextNamedSibling(cursor);
    assert cursor.getCurrentNode().getType().equals("attrpath")
        : "Expected attrpath node, got " + cursor.getCurrentNode().getType();

    NixNode propertyReferenceNode = target;
    for (Node child : CursorUtil.namedChildren(cursor)) {
      assert child.getType().equals("identifier");
      propertyReferenceNode =
          PropertyReferenceNodeGen.create(propertyReferenceNode, child.getText());
    }

    cursor.gotoParent();
    return propertyReferenceNode;
  }
}
