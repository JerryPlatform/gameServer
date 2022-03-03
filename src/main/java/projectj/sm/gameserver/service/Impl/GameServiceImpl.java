package projectj.sm.gameserver.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projectj.sm.gameserver.domain.chat.Room;
import projectj.sm.gameserver.domain.game.yahtzee.YahtzeeGameRelativeRecord;
import projectj.sm.gameserver.domain.game.yahtzee.YahtzeeGameResult;
import projectj.sm.gameserver.domain.game.yahtzee.YahtzeePlayMember;
import projectj.sm.gameserver.repository.MemberRepository;
import projectj.sm.gameserver.repository.chat.ChatRoomRepository;
import projectj.sm.gameserver.repository.game.yahtzee.YahtzeeGameRelativeRecordRepository;
import projectj.sm.gameserver.repository.game.yahtzee.YahtzeeGameResultRepository;
import projectj.sm.gameserver.service.GameService;
import projectj.sm.gameserver.vo.session.UserChatSession;
import projectj.sm.gameserver.vo.session.YahtzeeGameSession;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static projectj.sm.gameserver.controller.chat.ChatController.userChatSessions;
import static projectj.sm.gameserver.controller.game.YahtzeeController.yahtzeeGameSessions;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {
    private final ChatRoomRepository chatRoomRepository;
    private final YahtzeeGameResultRepository yahtzeeGameResultRepository;
    private final MemberRepository memberRepository;
    private final YahtzeeGameRelativeRecordRepository yahtzeeGameRelativeRecordRepository;
    private final SimpMessagingTemplate template;

    @Transactional
    @Override
    public void changeRoomStatusByStartingTheGame(Long id) {
        chatRoomRepository.changeRoomStatus(id, Room.Status.PROCEEDING);
    }

    @Transactional
    @Override
    public void changeRoomStatusByEndingTheGame(Long id) {
        chatRoomRepository.changeRoomStatus(id, Room.Status.GENERAL);
    }

    @Override
    public void createYahtzeeGameSession(Long roomId) {
        List<UserChatSession> roomVisitors =
                        userChatSessions.stream()
                                .filter(userChatSession -> userChatSession.getChatRoomId().equals(roomId))
                                .collect(Collectors.toList());
                List<YahtzeeGameSession.userInfo> userInfos = new ArrayList<>();
                for (UserChatSession userChatSession : roomVisitors) {
                    YahtzeeGameSession.userInfo userInfo = YahtzeeGameSession.userInfo.builder()
                            .simpSessionId(userChatSession.getSimpSessionId())
                            .userId(userChatSession.getUserId())
                            .userAccount(userChatSession.getUserAccount())
                            .userName(userChatSession.getUserName())
                            .aces(0)
                            .twos(0)
                            .threes(0)
                            .fours(0)
                            .fives(0)
                            .sixes(0)
                            .generalScoreTotal(0)
                            .bonus(0)
                            .threeOfKind(0)
                            .fourOfKind(0)
                            .fullHouse(0)
                            .smallStraight(0)
                            .largeStraight(0)
                            .chance(0)
                            .yahtzee(0)
                            .totalScore(0)
                            .build();
                    userInfos.add(userInfo);
                }
                yahtzeeGameSessions.add(YahtzeeGameSession.builder()
                        .roomId(roomId)
                        .userCount(userInfos.size())
                        .remainingTurns(12 * userInfos.size())
                        .turnUserName(userInfos.get(0).getUserName())
                        .userInfos(userInfos)
                        .build());
    }

    @Override
    public void removeYahtzeeGameSession(Long roomId) {
        yahtzeeGameSessions.removeIf(yahtzeeGameSession -> yahtzeeGameSession.getRoomId().equals(roomId));
    }

    @Transactional
    @Override
    public void reflectionOfYahtzeeGameResults(Long roomId) {
        YahtzeeGameSession session = yahtzeeGameSessions.stream()
                .filter(yahtzeeGameSession -> yahtzeeGameSession.getRoomId().equals(roomId))
                .findFirst().get();

        YahtzeeGameResult yahtzeeGameResult = new YahtzeeGameResult();
        List<YahtzeeGameSession.userInfo> tempUserInfos = session.getUserInfos();
        for (YahtzeeGameSession.userInfo userinfo : tempUserInfos) {
            YahtzeePlayMember yahtzeePlayMember = new YahtzeePlayMember();
            yahtzeePlayMember.setMember(memberRepository.getById(userinfo.getUserId()));
            yahtzeePlayMember.setTotalScore(userinfo.getTotalScore());
            yahtzeeGameResult.addYahtzeeGamePlayMember(yahtzeePlayMember);
        }

        YahtzeeGameSession.userInfo winner = tempUserInfos.stream()
                .max(Comparator.comparing(YahtzeeGameSession.userInfo::getTotalScore)).get();

        yahtzeeGameResult.setWinner(memberRepository.getById(winner.getUserId()));
        yahtzeeGameResult.setTotalScore(winner.getTotalScore());
        YahtzeeGameResult result = yahtzeeGameResultRepository.save(yahtzeeGameResult);

        if (tempUserInfos.size() == 2) {
            YahtzeeGameSession.userInfo loser = tempUserInfos.stream()
                    .filter(userInfo -> !userInfo.getUserId().equals(winner.getUserId()))
                    .findFirst().get();
            YahtzeeGameRelativeRecord yahtzeeGameRelativeRecord = new YahtzeeGameRelativeRecord();
            yahtzeeGameRelativeRecord.setGameResult(result);
            yahtzeeGameRelativeRecord.setVictor(memberRepository.getById(winner.getUserId()));
            yahtzeeGameRelativeRecord.setLoser(memberRepository.getById(loser.getUserId()));
            yahtzeeGameRelativeRecordRepository.save(yahtzeeGameRelativeRecord);
        }
    }
}
