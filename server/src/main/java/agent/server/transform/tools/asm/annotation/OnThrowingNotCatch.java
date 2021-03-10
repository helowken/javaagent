package agent.server.transform.tools.asm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static agent.invoke.proxy.ProxyArgsMask.MASK_NONE;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnThrowingNotCatch {
    int mask() default MASK_NONE;

    int argsHint() default -1;
}
