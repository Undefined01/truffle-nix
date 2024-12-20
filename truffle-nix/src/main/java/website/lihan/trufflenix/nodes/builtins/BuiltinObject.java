package website.lihan.trufflenix.nodes.builtins;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.staticobject.DefaultStaticProperty;
import com.oracle.truffle.api.staticobject.StaticProperty;
import com.oracle.truffle.api.staticobject.StaticShape;
import org.graalvm.collections.EconomicMap;
import website.lihan.trufflenix.NixLanguage;
import website.lihan.trufflenix.nodes.NixRootNode;
import website.lihan.trufflenix.runtime.objects.FunctionObject;
import website.lihan.trufflenix.runtime.objects.NullObject;
import website.lihan.trufflenix.runtime.objects.TruffleMemberNamesObject;

@ExportLibrary(InteropLibrary.class)
public final class BuiltinObject implements TruffleObject {
  private Object targetObject;
  private final EconomicMap<String, StaticProperty> properties = EconomicMap.create();
  private final String[] propertyNames;

  public BuiltinObject(NixLanguage language) {
    StaticShape.Builder shapeBuilder = StaticShape.newBuilder(language);

    addProperty(shapeBuilder, "true", Boolean.class);
    addProperty(shapeBuilder, "false", Boolean.class);
    addProperty(shapeBuilder, "null", NullObject.class);
    addProperty(shapeBuilder, "typeOf");

    addProperty(shapeBuilder, "length");
    addProperty(shapeBuilder, "elemAt");
    addProperty(shapeBuilder, "head");
    addProperty(shapeBuilder, "tail");
    addProperty(shapeBuilder, "filter");
    addProperty(shapeBuilder, "map");
    addProperty(shapeBuilder, "genList");

    targetObject = shapeBuilder.build().getFactory().create();

    initProperty(language, "true", true);
    initProperty(language, "false", false);
    initProperty(language, "null", NullObject.INSTANCE);
    initMethodProperty(language, "typeOf", TypeOfNodeGen.create());

    initMethodProperty(language, "length", LengthNodeGen.create());
    initMethodProperty(language, "elemAt", ElemAtNodeGen.create());
    initMethodProperty(language, "head", HeadNodeGen.create());
    initMethodProperty(language, "tail", TailNodeGen.create());
    initMethodProperty(language, "filter", FilterNodeGen.create());
    initMethodProperty(language, "map", MapNodeGen.create());
    initMethodProperty(language, "genList", GenListNodeGen.create());

    propertyNames = new String[properties.size()];
    var i = 0;
    for (var prop : properties.getKeys()) {
      propertyNames[i++] = prop;
    }
  }

  public static BuiltinObject create(NixLanguage language) {
    return new BuiltinObject(language);
  }

  private void addProperty(StaticShape.Builder shapeBuilder, String name) {
    addProperty(shapeBuilder, name, FunctionObject.class);
  }

  private void addProperty(StaticShape.Builder shapeBuilder, String name, Class<?> type) {
    var prop = new DefaultStaticProperty(name);
    properties.put(prop.getId(), prop);
    shapeBuilder.property(prop, type, true);
  }

  private void initProperty(NixLanguage language, String name, Object value) {
    var prop = properties.get(name);
    prop.setObject(targetObject, value);
  }

  private void initMethodProperty(
      NixLanguage language, String name, BuiltinFunctionNode functionBody) {
    var rootNode = new NixRootNode(language, functionBody);
    var function = new FunctionObject(rootNode.getCallTarget(), functionBody.getArgumentCount());
    var prop = properties.get(name);
    prop.setObject(targetObject, function);
  }

  @ExportMessage
  boolean hasMembers() {
    return true;
  }

  @ExportMessage
  boolean isMemberReadable(String member) {
    return properties.containsKey(member);
  }

  @ExportMessage
  Object readMember(String member) throws UnknownIdentifierException {
    var prop = properties.get(member);
    if (prop == null) {
      throw UnknownIdentifierException.create(member);
    }
    return prop.getObject(targetObject);
  }

  @ExportMessage
  Object getMembers(boolean includeInternal) {
    return new TruffleMemberNamesObject(propertyNames);
  }
}
