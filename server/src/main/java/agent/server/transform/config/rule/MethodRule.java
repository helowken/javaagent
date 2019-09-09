package agent.server.transform.config.rule;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MethodRule {
    String method();

    Position position();

    String filter() default "";

    int maxLevel() default 10;

    enum Position {
        BEFORE,
        AFTER,
        WRAP,

        BEFORE_MC,
        AFTER_MC,
        WRAP_MC
    }
}
