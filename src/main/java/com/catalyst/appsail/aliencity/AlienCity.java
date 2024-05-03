package com.catalyst.appsail.aliencity;
 
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zc.auth.CatalystSDK;
import com.zc.component.object.ZCObject;
import com.zc.component.object.ZCRowObject;
import com.zc.component.zcql.ZCQL;
import org.springframework.http.ResponseEntity;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
@RestController
public class AlienCity {
    Map<String, String> responseMap = new HashMap<>();
 
    private int getAlienCountFromCatalystDataStore(String cityName) throws Exception {
        String query = "Select * from AlienCity where CityName = " + cityName;
        ArrayList<ZCRowObject> rowList = ZCQL.getInstance().executeQuery(query);
        return rowList.size();
    }
 
    @GetMapping("/alien")
    public ResponseEntity<String> getAlienEncounter(@RequestParam String cityname, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        CatalystSDK.init(request);
        try {
            int length = getAlienCountFromCatalystDataStore(cityname);
 
            if (length == 0) {
                responseMap.put("message", "Hurray! No alien encounters in this city yet!");
                responseMap.put("signal", "negative");
                String jsonResponse = new ObjectMapper().writeValueAsString(responseMap);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(jsonResponse);
            } else {
                responseMap.put("message", "Uh oh! Looks like there are aliens in this city!");
                responseMap.put("signal", "positive");
                String jsonResponse = new ObjectMapper().writeValueAsString(responseMap);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(jsonResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseMap.put("message", "Internal Server Error");
            responseMap.put("signal", "error");
            String jsonResponse = new ObjectMapper().writeValueAsString(responseMap);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonResponse);
        }
    }
 
    @PostMapping("/alien")
    public ResponseEntity<String> postAlienEncounter(HttpServletRequest request, HttpServletResponse response,
            @RequestBody String requestBody) throws Exception {
        CatalystSDK.init(request);
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(requestBody);
        String cityname = (String) jsonObject.get("city_name");
 
        try {
            int length = getAlienCountFromCatalystDataStore(cityname);
 
            if (length > 0) {
                responseMap.put("message",
                        "Looks like you are not the first person to encounter aliens in this city! Someone has already reported an alien encounter here!");
                String jsonResponse = new ObjectMapper().writeValueAsString(responseMap);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(jsonResponse);
            } else {
                ZCRowObject row = ZCRowObject.getInstance();
                row.set("CityName", cityname);
                ZCObject.getInstance().getTableInstance("AlienCity").insertRow(row);
                responseMap.put("message", "Thanks for reporting!");
                String jsonResponse = new ObjectMapper().writeValueAsString(responseMap);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(jsonResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseMap.put("message", "Internal Server Error");
            String jsonResponse = new ObjectMapper().writeValueAsString(responseMap);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonResponse);
        }
    }
}