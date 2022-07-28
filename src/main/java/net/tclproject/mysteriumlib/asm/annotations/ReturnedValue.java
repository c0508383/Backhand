package net.tclproject.mysteriumlib.asm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Intercepts the value that has originally been returned and passes it into the fix method.
 * In other words, passes the last value in the stack.
 * Can only use if injectOnExit() == true and the target method doesn't return void.
 */
@Target(ElementType.PARAMETER)
public @interface ReturnedValue {}
