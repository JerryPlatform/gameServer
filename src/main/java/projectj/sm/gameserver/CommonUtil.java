package projectj.sm.gameserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Log
public class CommonUtil {
    public static String getLocalTime() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }

    public static String objectToJsonString(Object o) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = mapper.writeValueAsString(o);
        return jsonInString;
    }

    public static String extractDataFromEventMessages(SessionSubscribeEvent event, String key) {
        String data = ((Map<String, Object>) event.getMessage().getHeaders().get("nativeHeaders")).get(key).toString()
                .replace("[", "")
                .replace("]", "");
        return data;
    }

    public static String extractDataFromEventMessages(SessionUnsubscribeEvent event, String key) {
        String data = ((Map<String, Object>) event.getMessage().getHeaders().get("nativeHeaders")).get(key).toString()
                .replace("[", "")
                .replace("]", "");
        return data;
    }

    public static Map<String, String> redisJsonToMap(String jsonText) throws JsonProcessingException {
        Map<String, String> resultMap = new HashMap<>();
        jsonText = jsonText
                .replace("{", "")
                .replace("}", "")
                .replaceAll(" ", "");

        String[] splitText = jsonText.split(",");
        for (String text : splitText) {
            String[] keyValueText = text.split("=");
            resultMap.put(keyValueText[0], keyValueText[1]);
        }

        return resultMap;
    }
}
