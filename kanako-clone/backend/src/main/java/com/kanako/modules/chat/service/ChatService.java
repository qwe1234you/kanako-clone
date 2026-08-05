package com.kanako.modules.chat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanako.common.exception.BusinessException;
import com.kanako.modules.chat.entity.ChatMessage;
import com.kanako.modules.chat.entity.ChatSession;
import com.kanako.modules.chat.mapper.ChatMessageMapper;
import com.kanako.modules.chat.mapper.ChatSessionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final ObjectMapper objectMapper;

    @Value("${kanako.llm.base-url:}")
    private String llmBaseUrl;
    @Value("${kanako.llm.api-key:}")
    private String llmApiKey;
    @Value("${kanako.llm.model:gpt-4o-mini}")
    private String llmModel;

    public String newSession(String title) {
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        ChatSession s = new ChatSession();
        s.setSessionId(sessionId);
        s.setTitle(StringUtils.hasText(title) ? title.trim() : "新对话");
        s.setCreatedAt(LocalDateTime.now());
        s.setUpdatedAt(LocalDateTime.now());
        sessionMapper.insert(s);
        return sessionId;
    }

    public List<ChatMessage> history(String sessionId) {
        return messageMapper.selectList(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .orderByAsc(ChatMessage::getId)
                .last("limit 200"));
    }

    public Map<String, Object> send(String sessionId, String content) {
        if (!StringUtils.hasText(content) || content.trim().length() > 4000) {
            throw new BusinessException("消息不能为空且不超过 4000 字");
        }
        String sid = sessionId;
        if (!StringUtils.hasText(sid)) {
            sid = newSession(null);
        }
        ensureSession(sid);

        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sid);
        userMsg.setRole("USER");
        userMsg.setContent(content.trim());
        userMsg.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(userMsg);

        touchSession(sid);

        String reply;
        if (!StringUtils.hasText(llmApiKey) || !StringUtils.hasText(llmBaseUrl)) {
            reply = "（AI 服务尚未配置）管理员在 application.yml 的 kanako.llm 中填入接口地址与密钥后，这里就会有真实回复了。";
        } else {
            reply = callLlm(sid);
        }

        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSessionId(sid);
        aiMsg.setRole("AI");
        aiMsg.setContent(reply);
        aiMsg.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(aiMsg);

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sid);
        result.put("reply", reply);
        result.put("messageId", aiMsg.getId());
        return result;
    }

    private void ensureSession(String sessionId) {
        if (sessionMapper.selectOne(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getSessionId, sessionId)) == null) {
            newSession(null);
        }
    }

    private void touchSession(String sessionId) {
        sessionMapper.update(null, new LambdaUpdateWrapper<ChatSession>()
                .eq(ChatSession::getSessionId, sessionId)
                .set(ChatSession::getUpdatedAt, LocalDateTime.now()));
    }

    private String callLlm(String sessionId) {
        try {
            List<ChatMessage> history = history(sessionId);
            List<Map<String, String>> messages = new java.util.ArrayList<>();
            messages.add(Map.of("role", "system", "content",
                    "你是 Kanako Space 博客的 AI 助手，回答应简洁友好，默认使用中文。"));
            for (ChatMessage m : history) {
                messages.add(Map.of("role", "user".equals(m.getRole().toLowerCase()) ? "user" : "assistant",
                        "content", m.getContent()));
            }
            Map<String, Object> reqBody = new HashMap<>();
            reqBody.put("model", llmModel);
            reqBody.put("messages", messages);
            reqBody.put("max_tokens", 1024);

            RestClient client = RestClient.builder()
                    .baseUrl(llmBaseUrl)
                    .defaultHeader("Authorization", "Bearer " + llmApiKey)
                    .build();

            String body = client.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(reqBody))
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(body);
            String reply = node.path("choices").path(0).path("message").path("content").asText("");
            return StringUtils.hasText(reply) ? reply.trim() : "（AI 返回了空回复）";
        } catch (Exception e) {
            return "（AI 调用失败：" + e.getMessage() + "）";
        }
    }
}
