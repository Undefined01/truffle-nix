package website.lihan.trufflenix.nodes.expressions.letexp;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;

import website.lihan.trufflenix.runtime.objects.FunctionObject;
import website.lihan.trufflenix.runtime.objects.LazyEvaluatedObject;

/// <summary>
/// Represents a variable binding that is initially assigned a placeholder value,
/// which will later be replaced with the actual value during evaluation.
/// </summary>
/// <remarks>
/// To handle code like {@code let x = { a = x; }; in x}, we need to evaluate the inner expression 
/// {@code { a = x; }} and bind it to the variable {@code x}. However, this requires {@code x} to be 
/// available for evaluation of {@code { a = x; }}.
///
/// To resolve this circular dependency, we introduce an indirection for the variable {@code x}. 
/// First, we assign a placeholder value to {@code x}, evaluate the inner expression, and then replace the 
/// placeholder with the final evaluated value.
///
/// For lambda functions, we wrap the expression with a {@link FunctionObject}, while for other 
/// values, we use a {@link LazyEvaluatedObject} to manage deferred evaluation.
/// </remarks>
public abstract class AbstractBindingNode extends Node {
    /// <summary>
    /// Initializes the variable binding with either a direct value or a placeholder.
    /// </summary>
    public abstract void executeInitializeBinding(VirtualFrame frame);

    /// <summary>
    /// Replaces the placeholder with the actual value after evaluating the bound expression.
    /// </summary>
    public abstract void executeFinalizeBinding(VirtualFrame frame);
}
