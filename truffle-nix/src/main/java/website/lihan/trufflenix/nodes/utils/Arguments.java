package website.lihan.trufflenix.nodes.utils;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.TruffleObject;
import website.lihan.trufflenix.runtime.objects.ListObject;

public final class Arguments {
  public static final int CAPTURED_VARIABLES_IDX = 0;
  public static final int ARGUMENTS_START_IDX = 1;

  public static Object[] pack(Object[] arguments, TruffleObject capturedVariables) {
    return pack(new SliceOfArray(arguments, 0, arguments.length), capturedVariables);
  }

  public static Object[] pack(SliceOfArray arguments, TruffleObject capturedVariables) {
    Object[] argumentsWithCapturedVariables = new Object[arguments.count() + 1];

    argumentsWithCapturedVariables[CAPTURED_VARIABLES_IDX] = capturedVariables;
    System.arraycopy(
        arguments.array(),
        arguments.start(),
        argumentsWithCapturedVariables,
        ARGUMENTS_START_IDX,
        arguments.count());
    return argumentsWithCapturedVariables;
  }

  public static Object getArgument(VirtualFrame frame, int index) {
    return frame.getArguments()[ARGUMENTS_START_IDX + index];
  }

  public static Object getCapturedVariable(VirtualFrame frame, int index) {
    var capturedVariables = (ListObject) frame.getArguments()[0];
    return capturedVariables.getArray()[index];
  }
}
