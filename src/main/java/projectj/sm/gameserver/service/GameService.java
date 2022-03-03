package projectj.sm.gameserver.service;

public interface GameService {
    void changeRoomStatusByStartingTheGame(Long id);
    void changeRoomStatusByEndingTheGame(Long roomId);
    void createYahtzeeGameSession(Long roomId);
    void removeYahtzeeGameSession(Long roomId);
    void reflectionOfYahtzeeGameResults(Long roomId);
}
