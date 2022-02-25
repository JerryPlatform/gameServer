package projectj.sm.gameserver.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class Response<T> {
    Result response;
    T contents;
}