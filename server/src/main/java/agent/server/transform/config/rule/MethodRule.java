package agent.server.transform.config.rule;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MethodRule {
    String method();

    String[] argTypes() default "";

    Position position();

    enum Position {
        BEFORE, AFTER
    }
}
