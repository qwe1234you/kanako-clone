package com.kanako.modules.chat.controller;

import com.kanako.common.response.R;
import com.kanako.modules.chat.entity.ChatMessage;
import com.kanako.modules.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api-llm/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/new")
    public R<String> newSession(@RequestBody(required = false) Map<String, String> body) {
        String title = body == null ? null : body.get("title");
        return R.ok(chatService.newSession(title));
    }

    @GetMapping("/history")
    public R<List<ChatMessage>> history(@RequestParam String sessionId) {
        return R.ok(chatService.history(sessionId));
    }

    @PostMapping("/send")
    public R<Map<String, Object>> send(@RequestBody Map<String, String> body) {
        return R.ok(chatService.send(body.get("sessionId"), body.get("message")));
    }
}
