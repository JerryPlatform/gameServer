package projectj.sm.gameserver.dto.validator.password;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordNotEqualValidator.class)
@Documented
public @interface PasswordNotEqual {
    String message() default "password is not equal";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String passwordFieldName() default "";
    String passwordVerificationFieldName() default "";
}
