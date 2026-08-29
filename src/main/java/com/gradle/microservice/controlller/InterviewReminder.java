package com.gradle.microservice.controlller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/interview")
public class InterviewReminder {

    private static final Logger log = LoggerFactory.getLogger(InterviewReminder.class);

    // in-memory storage — developer way!
    private Map<Integer, String> store = new HashMap<>();

    // GET — fetch all
    @GetMapping
    public ResponseEntity<Map<Integer, String>> getAll() {
        log.info("GET called");
        return ResponseEntity.ok(store);
    }

    // GET by ID
    @GetMapping("/{id}")
    public ResponseEntity<String> getById(@PathVariable int id) {
        log.info("GET by id called: {}", id);
        if (!store.containsKey(id))
            throw new RuntimeException("Item not found with id: " + id); // 404
        return ResponseEntity.ok(store.get(id));
    }

    // POST — add new
    @PostMapping
    public ResponseEntity<String> create(@RequestBody Map<String, String> body) {
        log.info("POST called: {}", body);
        if (body.get("value") == null)
            throw new IllegalArgumentException("Value cannot be null"); // 400
        int id = store.size() + 1;
        store.put(id, body.get("value"));
        return ResponseEntity.status(HttpStatus.CREATED).body("Created with id: " + id);
    }

    // PUT — update
    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable int id, @RequestBody Map<String, String> body) {
        log.info("PUT called: {}", id);
        if (!store.containsKey(id))
            throw new RuntimeException("Item not found with id: " + id); // 404
        store.put(id, body.get("value"));
        return ResponseEntity.ok("Updated id: " + id);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable int id) {
        log.info("DELETE called: {}", id);
        if (!store.containsKey(id))
            throw new RuntimeException("Item not found with id: " + id); // 404
        store.remove(id);
        return ResponseEntity.ok("Deleted id: " + id);
    }
}