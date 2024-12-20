package website.lihan.trufflenix.nodes.utils;

import java.util.List;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.expressions.functions.FunctionApplicationNodeGen;
import website.lihan.trufflenix.nodes.expressions.functions.TailCallThrowerNodeGen;

public class Functions {
  public static NixNode create(NixNode functionNode, List<NixNode> argumentNodes) {
    return create(functionNode, argumentNodes.toArray(NixNode[]::new));
  }

  public static NixNode createTailCall(NixNode functionNode, List<NixNode> argumentNodes) {
    return createTailCall(functionNode, argumentNodes.toArray(NixNode[]::new));
  }

  public static NixNode create(NixNode functionNode, NixNode[] argumentNodes) {
    return FunctionApplicationNodeGen.create(functionNode, new ExecuteValuesNode(argumentNodes));
  }

  public static NixNode createTailCall(NixNode functionNode, NixNode[] argumentNodes) {
    return TailCallThrowerNodeGen.create(functionNode, new ExecuteValuesNode(argumentNodes));
  }
}
