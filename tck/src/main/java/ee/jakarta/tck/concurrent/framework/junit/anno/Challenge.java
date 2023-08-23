package ee.jakarta.tck.concurrent.framework.junit.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Disabled;

/**
 * Challenge metadata to track where challenges from GitHub came from,
 * what version was affected by the challenge,
 * and if the issue has been fixed.
 *
 * Aggregates the @Disabled annotation from Junit
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Disabled
public @interface Challenge {
    
    /**
     * REQUIRED: Link to the challenge on GitHub
     */
    String link();
    
    /**
     * REQUIRED: The version of the TCK where the challenge was found.
     */
    String version();
    
    /**
     * OPTIONAL: Link to the pull request on GitHub that fixes the challenge
     */
    String fix() default "";
    
    /**
     * OPTIONAL: The version of the TCK where the test can be re-enabled
     */
    String reintroduce() default "";
    
    /**
     * OPTIONAL: The reason the TCK was disabled
     */
    String reason() default "";


}
