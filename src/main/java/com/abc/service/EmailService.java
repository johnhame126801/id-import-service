package com.abc.service;

import com.abc.model.AppleID;
import com.abc.model.Email;
import com.abc.model.Result;
import com.alibaba.fastjson2.JSON;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
@Log4j2
public class EmailService {

    /**
     * Email ID序列
     */
    private static final String KEY_EMAIL_SEQ = "EMAIL_SEQ";

    /**
     * 未使用Email队列
     */
    private static final String KEY_EMAIL_UNUSED_LIST = "EMAIL_UNUSED_LIST";

    /**
     * Email 哈希表
     */
    private static final String KEY_EMAIL_HASH = "EMAIL_HASH";

    private final StringRedisTemplate redisTemplate;

    public EmailService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Result<Void> upload(MultipartFile file) throws Exception {
        StringBuilder errMsg = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("{}", line);
                String[] arr = line.split("----");
                if (arr.length < 2) {
                    errMsg.append("解析行错误：").append(line).append("\n");
                    continue;
                }
                Long id = redisTemplate.opsForValue().increment(KEY_EMAIL_SEQ, 1);
                if (id == null) {
                    errMsg.append("获取ID失败，导入行错误：").append(line).append("\n");
                    continue;
                }
                Email email = new Email();
                email.setId(id);
                email.setEmail(arr[0]);
                email.setPassword(arr[1]);
                email.setStatus(Email.STATUS_UNUSED);
                String storedValue = JSON.toJSONString(email);
                redisTemplate.opsForHash().put(KEY_EMAIL_HASH, id.toString(), storedValue);
                redisTemplate.opsForList().rightPush(KEY_EMAIL_UNUSED_LIST, storedValue);
            }
        }
        if (StringUtils.hasLength(errMsg)) {
            return Result.error(errMsg.toString());
        }
        return Result.success(null);
    }

    public Result<Email> getUnusedEmail() {
        String value = redisTemplate.opsForList().leftPop(KEY_EMAIL_UNUSED_LIST);
        if (!StringUtils.hasLength(value)) {
            return Result.error("取Email失败：待使用队列为空");
        }
        Email email = JSON.parseObject(value, Email.class);
        email.setStatus(Email.STATUS_USED);
        redisTemplate.opsForHash().put(KEY_EMAIL_HASH, email.getId().toString(), JSON.toJSONString(email));
        return Result.success(email);
    }

    public int getUnusedEmailCount() {
        Long value = redisTemplate.opsForList().size(KEY_EMAIL_UNUSED_LIST);
        if (value == null) {
            return 0;
        }
        return value.intValue();
    }

    public Result<Void> clear() {
        redisTemplate.delete(KEY_EMAIL_SEQ);
        redisTemplate.delete(KEY_EMAIL_UNUSED_LIST);
        redisTemplate.delete(KEY_EMAIL_HASH);
        return Result.success(null);
    }

}
