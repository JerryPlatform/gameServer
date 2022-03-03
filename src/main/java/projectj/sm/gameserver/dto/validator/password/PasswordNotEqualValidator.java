package projectj.sm.gameserver.dto.validator.password;

import lombok.SneakyThrows;
import projectj.sm.gameserver.dto.MemberDto;
import projectj.sm.gameserver.dto.validator.ValidatorUtil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class PasswordNotEqualValidator implements ConstraintValidator<PasswordNotEqual, MemberDto> {
    private String passwordFieldName;
    private String passwordVerificationFieldName;

    @Override
    public void initialize(PasswordNotEqual constraintAnnotation) {
        passwordFieldName = constraintAnnotation.passwordFieldName();
        passwordVerificationFieldName = constraintAnnotation.passwordVerificationFieldName();
    }

    @SneakyThrows({IllegalAccessException.class, NoSuchFieldException.class})
    @Override
    public boolean isValid(MemberDto dto, ConstraintValidatorContext constraintValidatorContext) {
        Optional<String> password1 = Optional.ofNullable(ValidatorUtil.getFieldValue(dto, passwordFieldName));
        Optional<String> password2 = Optional.ofNullable(ValidatorUtil.getFieldValue(dto, passwordVerificationFieldName));
        if(dto.getId() == null) {
        	if (!password1.equals(password2))
                    ValidatorUtil.addValidationError(passwordVerificationFieldName, constraintValidatorContext);
            return password1.isPresent() && password2.isPresent() && password1.equals(password2);
        }
        return true;
    }
}

