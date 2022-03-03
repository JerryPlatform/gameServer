package projectj.sm.gameserver.dto;

import lombok.Getter;
import lombok.Setter;
import projectj.sm.gameserver.dto.validator.password.PasswordNotEqual;

@PasswordNotEqual(
        passwordFieldName = "password",
        passwordVerificationFieldName = "passwordConfirm"
)
@Getter
@Setter
public class MemberDto {
    private Long id;
    private String name;
    private String account;
    private String password;
    private String passwordConfirm;
    private boolean valid;
}
