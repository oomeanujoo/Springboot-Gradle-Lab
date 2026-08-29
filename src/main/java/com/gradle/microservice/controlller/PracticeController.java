package com.gradle.microservice.controlller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;

import java.util.*;

@RestController
public class PracticeController {

    private Map<Integer, String> map = new HashMap<>();

    @GetMapping("/getAllData")
    public ResponseEntity<?> getAllData() {
        if (map.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("No data present!");
        return ResponseEntity.ok(map);
    }

    @PostMapping("/postData")
    public ResponseEntity<Map> postData(@RequestBody List<String> body) {
        int id = map.size() + 1;
        String val = body.get(0);
        map.put(id, val);

        Map<Integer, String> resp = new HashMap<>();
        resp.put(id, val);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/getIdData")
    public ResponseEntity<Map> getIdData(@RequestParam int id) {
        return map.containsKey(id) ? ResponseEntity.ok(Map.of(id, map.get(id))) : ResponseEntity.notFound().build();
    }

}
