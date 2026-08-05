package com.kanako.modules.site.controller;

import com.kanako.common.exception.BusinessException;
import com.kanako.common.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 对象存储接口：当前实现为本地磁盘存储（kanako.storage.local-dir），
 * 接口与阿里云 OSS 版保持一致，便于后续切换。
 */
@RestController
@RequestMapping("/api-common/oss")
@RequiredArgsConstructor
public class OssController {

    @Value("${kanako.storage.local-dir:./data/upload}")
    private String localDir;

    private Path root() {
        return Paths.get(localDir).toAbsolutePath().normalize();
    }

    private Path resolve(String path) {
        Path p = root().resolve(path == null ? "" : path).normalize();
        if (!p.startsWith(root())) {
            throw new BusinessException("非法路径");
        }
        return p;
    }

    @GetMapping("/fetchDirFile")
    public R<Map<String, Object>> fetchDir(@RequestParam(required = false) String path) {
        Path dir = resolve(path == null ? "" : path);
        if (!Files.isDirectory(dir)) {
            throw new BusinessException(404, "目录不存在");
        }
        List<Map<String, Object>> dirs = new ArrayList<>();
        List<Map<String, Object>> files = new ArrayList<>();
        try (var stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                Map<String, Object> m = new HashMap<>();
                m.put("name", entry.getFileName().toString());
                m.put("isDir", Files.isDirectory(entry));
                m.put("size", Files.isDirectory(entry) ? 0L : Files.size(entry));
                m.put("lastModified", Files.getLastModifiedTime(entry).toMillis());
                if (Files.isDirectory(entry)) {
                    dirs.add(m);
                } else {
                    files.add(m);
                }
            }
        } catch (IOException e) {
            throw new BusinessException("读取目录失败: " + e.getMessage());
        }
        dirs.sort((a, b) -> String.valueOf(a.get("name")).compareTo(String.valueOf(b.get("name"))));
        files.sort((a, b) -> String.valueOf(a.get("name")).compareTo(String.valueOf(b.get("name"))));
        Map<String, Object> result = new HashMap<>();
        result.put("dirs", dirs);
        result.put("files", files);
        return R.ok(result);
    }

    @PostMapping("/uploadFiles")
    public R<List<String>> upload(@RequestParam("files") MultipartFile[] files,
                                  @RequestParam(required = false) String path) {
        Path dir = resolve(path == null ? "" : path);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new BusinessException("创建目录失败");
        }
        List<String> urls = new ArrayList<>();
        for (MultipartFile f : files) {
            if (f == null || f.isEmpty()) continue;
            String original = f.getOriginalFilename() == null ? "file" : f.getOriginalFilename();
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0) {
                ext = original.substring(dot);
            }
            String name = UUID.randomUUID().toString().replace("-", "") + ext;
            try {
                f.transferTo(dir.resolve(name).toFile());
            } catch (IOException e) {
                throw new BusinessException("上传失败: " + e.getMessage());
            }
            urls.add((path == null || path.isBlank() ? "" : "/" + path) + "/" + name);
        }
        return R.ok(urls);
    }

    @PostMapping("/makeDirectory")
    public R<Void> mkdir(@RequestParam(required = false) String path,
                         @RequestParam String dirName) {
        Path dir = resolve(path == null ? "" : path).resolve(dirName).normalize();
        if (!dir.startsWith(root())) {
            throw new BusinessException("非法路径");
        }
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new BusinessException("创建目录失败: " + e.getMessage());
        }
        return R.ok();
    }

    @PostMapping("/renameFile")
    public R<Void> rename(@RequestParam String path,
                          @RequestParam String newName) {
        Path src = resolve(path);
        if (!Files.exists(src)) {
            throw new BusinessException(404, "文件不存在");
        }
        Path dst = src.resolveSibling(newName).normalize();
        try {
            Files.move(src, dst);
        } catch (IOException e) {
            throw new BusinessException("重命名失败: " + e.getMessage());
        }
        return R.ok();
    }

    @PostMapping("/deleteFile")
    public R<Void> delete(@RequestParam String path) {
        Path p = resolve(path);
        if (!Files.exists(p)) {
            throw new BusinessException(404, "文件不存在");
        }
        try {
            if (Files.isDirectory(p)) {
                try (var stream = Files.newDirectoryStream(p)) {
                    if (stream.iterator().hasNext()) {
                        throw new BusinessException("目录非空，无法删除");
                    }
                }
                Files.delete(p);
            } else {
                Files.delete(p);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            throw new BusinessException("删除失败: " + e.getMessage());
        }
        return R.ok();
    }
}
