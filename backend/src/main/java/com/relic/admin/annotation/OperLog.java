package com.relic.admin.annotation;

import com.relic.admin.common.Constants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation marking a method for automatic operation-log recording.
 *
 * <p>Apply this to controller or service methods that perform auditable
 * operations. The {@link com.relic.admin.aop.OperationLogAspect} intercepts
 * annotated methods via {@code @Around} advice, captures request parameters,
 * return value (or exception), execution time and operator context, then
 * persists a {@link com.relic.admin.entity.SysLog} record asynchronously.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * &#64;OperLog(operationType = Constants.OP_CREATE, operationTarget = "artifact")
 * public Result&lt;Void&gt; createArtifact(&#64;RequestBody ArtifactDTO dto) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperLog {

    /**
     * Log type. Defaults to {@link Constants#LOG_TYPE_OPERATION}.
     *
     * @return one of OPERATION, SYSTEM, SECURITY
     */
    String logType() default Constants.LOG_TYPE_OPERATION;

    /**
     * Operation type, e.g. CREATE, UPDATE, DELETE, LOGIN.
     *
     * @return the operation type string
     */
    String operationType() default "";

    /**
     * Operation target: the table or module name being operated on.
     *
     * @return the operation target string
     */
    String operationTarget() default "";
}
