package projectj.sm.gameserver.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class Result {
    @Builder.Default
    protected String message = "success";
    @Builder.Default
    protected int status = 200;
    @Builder.Default
    protected String errorCode = "";
    @Setter
    @Builder.Default
    protected String token = "";
}

