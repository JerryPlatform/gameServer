package projectj.sm.gameserver.graphql;

import graphql.kickstart.tools.GraphQLQueryResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import projectj.sm.gameserver.service.MemberService;
import projectj.sm.gameserver.vo.Result;

@Log
@Component
@RequiredArgsConstructor
@Transactional
public class CommonQuery implements GraphQLQueryResolver {

    private final MemberService memberService;

    public Result getNewToken() {
        return memberService.getNewToken();
    }
}
