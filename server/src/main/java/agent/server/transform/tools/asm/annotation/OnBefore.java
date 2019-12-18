package agent.server.transform.tools.asm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static agent.server.transform.tools.asm.ProxyArgsMask.MASK_NONE;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnBefore {
    int mask() default MASK_NONE;

    String otherArgsFunc() default "";

}
