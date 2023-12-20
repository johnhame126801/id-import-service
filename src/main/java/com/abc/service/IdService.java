package com.abc.service;

import com.abc.model.AppleID;
import com.abc.model.Bind;
import com.abc.model.Result;
import com.alibaba.fastjson2.JSON;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Log4j2
public class IdService {

    /**
     * ID序列号
     */
    private static final String KEY_ID_SEQ = "ID_SEQ";

    /**
     * 待处理队列
     */
    private static final String KEY_WAITING_QUEUE = "ID_WAITING_QUEUE";

    /**
     * ID哈希表
     */
    public static final String KEY_ID_HASH = "ID_HASH";

    private final StringRedisTemplate redisTemplate;

    private final AssignService assignService;

    public IdService(StringRedisTemplate redisTemplate, AssignService assignService) {
        this.redisTemplate = redisTemplate;
        this.assignService = assignService;
    }

    /**
     * 上传AppleID
     * @param appleID  AppleID
     * @return Result
     */
    public Result<Void> uploadAppleID(AppleID appleID) {
        // 检测参数完整性
        if (!StringUtils.hasLength(appleID.getEmail()) ||
                !StringUtils.hasLength(appleID.getPassword()) ||
                !StringUtils.hasLength(appleID.getEmailPassword()) ||
                !StringUtils.hasLength(appleID.getBirthday())) {
            return Result.badRequest("参数不完整");
        }

        // 初始化部分字段
        Long id = redisTemplate.opsForValue().increment(KEY_ID_SEQ, 1L);
        if (id == null) {
            return Result.error("ID序列号获取失败");
        }
        appleID.setId(id);
        appleID.setStatus(AppleID.STATUS_WAIT_BIND);
        String storeValue = JSON.toJSONString(appleID);
        // 存储到待处理队列
        redisTemplate.opsForList().rightPush(KEY_WAITING_QUEUE, id.toString());
        // 存储到hash结构，持久化存储
        redisTemplate.opsForHash().put(KEY_ID_HASH, id.toString(), storeValue);
        return Result.success(null);
    }

    public Result<AppleID> getAppleID() {
        String value;
        do {
            // 从待处理队列中取ID
            String id = redisTemplate.opsForList().leftPop(KEY_WAITING_QUEUE);
            if (!StringUtils.hasLength(id)) {
                return Result.error("取ID失败：等待处理队列为空");
            }
            // 从ID哈希表中取ID
            value = (String) redisTemplate.opsForHash().get(KEY_ID_HASH, id);
        } while (!StringUtils.hasLength(value));
        AppleID appleID = JSON.parseObject(value, AppleID.class);
        appleID.setStatus(AppleID.STATUS_TAKE_OUT);
        redisTemplate.opsForHash().put(KEY_ID_HASH, appleID.getId().toString(), JSON.toJSONString(appleID));
        return Result.success(appleID);
    }

    public Result<Void> bind(Bind bind) {
        String value = (String) redisTemplate.opsForHash().get(KEY_ID_HASH, bind.getId().toString());
        if (!StringUtils.hasLength(value)) {
            return Result.error("绑定失败：ID不存在");
        }
        AppleID appleID = JSON.parseObject(value, AppleID.class);
        appleID.setStatus(AppleID.STATUS_REGISTERED);
        appleID.setUsaCard(bind.getCard());
        appleID.setUsaCardLink(bind.getLink());
        String storedValue = JSON.toJSONString(appleID);
        redisTemplate.opsForHash().put(KEY_ID_HASH, appleID.getId().toString(), storedValue);
        assignService.assign(appleID);
        return Result.success(null);
    }

    public Result<Void> resetPassword(long id, String password) {
        String value = (String) redisTemplate.opsForHash().get(KEY_ID_HASH, id + "");
        if (!StringUtils.hasLength(value)) {
            return Result.error("修改失败：ID不存在");
        }
        AppleID appleID = JSON.parseObject(value, AppleID.class);
        appleID.setPassword(password);
        redisTemplate.opsForHash().put(KEY_ID_HASH, id + "", JSON.toJSONString(appleID));
        return Result.success(null);
    }

    public int getUnbindIdCount() {
        Long size = redisTemplate.opsForList().size(KEY_WAITING_QUEUE);
        if (size == null) {
            return 0;
        }
        return size.intValue();
    }

    public int getUnAssignIdCount() {
        Long size = redisTemplate.opsForList().size(AssignService.KEY_WAITING_ASSIGN_QUEUE);
        if (size == null) {
            return 0;
        }
        return size.intValue();
    }

    public Result<Void> clear() {
        redisTemplate.delete(KEY_ID_SEQ);
        redisTemplate.delete(KEY_WAITING_QUEUE);
        redisTemplate.delete(KEY_ID_HASH);
        redisTemplate.delete(AssignService.KEY_WAITING_ASSIGN_QUEUE);
        return Result.success(null);
    }

}
