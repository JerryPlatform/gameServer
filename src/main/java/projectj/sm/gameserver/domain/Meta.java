package projectj.sm.gameserver.domain;

import java.util.Arrays;

public interface Meta {
    String getDescription();
    default String[] constants() {
        return Arrays.stream(getClass().getEnumConstants()).map(Meta::getDescription).toArray(String[]::new);
    }
}
