package com.kanako.modules.friend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kanako.common.jwt.RequireAdmin;
import com.kanako.common.response.R;
import com.kanako.modules.friend.entity.FriendLink;
import com.kanako.modules.friend.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api-common/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    /** 公开友链列表 */
    @GetMapping("/links")
    public R<List<FriendLink>> links() {
        return R.ok(friendService.links());
    }

    /** 公开朋友圈动态 */
    @GetMapping("/circle")
    public R<List<Map<String, Object>>> circle(@RequestParam(defaultValue = "1") long page,
                                               @RequestParam(defaultValue = "10") long size) {
        return R.ok(friendService.circleWithLink(page, size));
    }

    /** 申请友链 */
    @PostMapping("/apply")
    public R<Long> apply(@RequestBody Map<String, String> body) {
        return R.ok(friendService.apply(body.get("name"), body.get("url"), body.get("description")));
    }

    /** 管理端：友链分页 */
    @GetMapping("/admin/links")
    @RequireAdmin
    public R<Page<FriendLink>> adminLinks(@RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "10") long size,
                                          @RequestParam(required = false) String status) {
        return R.ok(friendService.adminPage(page, size, status));
    }

    /** 管理端：审核 / 批量状态 */
    @PostMapping("/admin/link/{id}")
    @RequireAdmin
    public R<Void> setStatus(@PathVariable Long id, @RequestParam String status) {
        friendService.updateStatus(id, status);
        return R.ok();
    }

    @DeleteMapping("/admin/link/{id}")
    @RequireAdmin
    public R<Void> deleteLink(@PathVariable Long id) {
        friendService.delete(id);
        return R.ok();
    }

    /** 管理端：发布朋友圈 */
    @PostMapping("/admin/circle")
    @RequireAdmin
    public R<Long> createCircle(@RequestBody Map<String, String> body) {
        Long linkId = body.get("linkId") == null || body.get("linkId").isBlank()
                ? null : Long.parseLong(body.get("linkId"));
        return R.ok(friendService.createCircle(linkId, body.get("content"), body.get("images")));
    }

    @DeleteMapping("/admin/circle/{id}")
    @RequireAdmin
    public R<Void> deleteCircle(@PathVariable Long id) {
        friendService.deleteCircle(id);
        return R.ok();
    }
}