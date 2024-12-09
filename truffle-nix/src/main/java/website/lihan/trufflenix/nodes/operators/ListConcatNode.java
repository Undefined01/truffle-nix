package website.lihan.trufflenix.nodes.operators;

import com.oracle.truffle.api.dsl.Specialization;
import website.lihan.trufflenix.NixLanguage;
import website.lihan.trufflenix.runtime.ListObject;

public abstract class ListConcatNode extends BinaryOpNode {
  private final NixLanguage nixLanguage;

  public ListConcatNode() {
    this.nixLanguage = NixLanguage.get(this);
  }

  @Specialization
  public ListObject doList(ListObject left, ListObject right) {
    var newList = new Object[(int) (left.getArraySize() + right.getArraySize())];
    System.arraycopy(left.getArray(), 0, newList, 0, (int) left.getArraySize());
    System.arraycopy(
        right.getArray(), 0, newList, (int) left.getArraySize(), (int) right.getArraySize());
    return new ListObject(newList);
  }
}
