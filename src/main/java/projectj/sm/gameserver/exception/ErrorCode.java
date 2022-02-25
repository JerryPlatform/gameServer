package projectj.sm.gameserver.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    AUTHENTICATION_FAILED(401, "AUTH001", "인증이 실패하였습니다."),
    LOGIN_FAILED(433, "AUTH002", "로그인이 실패하였습니다."),
    ACCESS_DENIED(434, "AUTH003", "접근 권한이 없습니다."),
    TOKEN_GENERATION_FAILED(500, "AUTH004", "jwt 인증 토큰 생성 실패."),
    NOT_EXIST_SUCH_USER(435, "AUTH05", "존재하지 않는 사용자입니다."),
    LOCKED(402, "AUTH006", "계정이 잠겼습니다."),
    ETC_ERROR(500, "ERROR00", "공통 에러"),
    NOT_FOUND_EXCEPTION(404, "ERROR01", "존재하지 않습니다."),
    VIOLATION_DATA_INTEGRITY(501, "ERROR02", "자식이 존재하여 삭제할 수 없습니다."),
    NOT_QR_EXIST(501, "ERROR03", "식별코드가 존재하지 않아 생성할 수 없습니다."),
    SPACE_NOT_EXIST(501, "ERROR04", "올바른 스페이스 ID를 대입하여 주십쇼."),
    SPACE_MOVE_NOT_ALLOWD(501, "ERROR05", "선택한 스페이스로 옮길 수 없습니다.");


    protected final String code;
    protected final String message;
    protected int status;

    ErrorCode(final int status, final String code, final String message) {
        this.status = status;
        this.message = message;
        this.code = code;
    }
}
