package website.lihan.trufflenix.nodes.utils;

import com.oracle.truffle.api.source.SourceSection;
import java.util.List;
import website.lihan.trufflenix.nodes.NixNode;
import website.lihan.trufflenix.nodes.expressions.functions.FunctionApplicationNodeGen;
import website.lihan.trufflenix.nodes.expressions.functions.TailCallThrowerNodeGen;

public class Functions {
  public static NixNode create(
      SourceSection source, NixNode functionNode, List<NixNode> argumentNodes) {
    return create(source, functionNode, argumentNodes.toArray(NixNode[]::new));
  }

  public static NixNode createTailCall(NixNode functionNode, List<NixNode> argumentNodes) {
    return createTailCall(functionNode, argumentNodes.toArray(NixNode[]::new));
  }

  public static NixNode create(
      SourceSection source, NixNode functionNode, NixNode[] argumentNodes) {
    return FunctionApplicationNodeGen.create(
        functionNode, new ExecuteValuesNode(argumentNodes), source);
  }

  public static NixNode createTailCall(NixNode functionNode, NixNode[] argumentNodes) {
    return TailCallThrowerNodeGen.create(functionNode, new ExecuteValuesNode(argumentNodes));
  }
}
