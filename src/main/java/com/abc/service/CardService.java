package com.abc.service;

import com.abc.model.Result;
import com.abc.model.UsaCard;
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
public class CardService {

    /**
     * 卡ID序列
     */
    private static final String KEY_CARD_SEQ = "CARD_SEQ";

    /**
     * 当前取卡ID
     */
    private static final String KEY_CARD_CURRENT_INDEX = "CARD_CURRENT_ID";

    /**
     * 卡哈希表
     */
    private static final String KEY_CARD_HASH = "CARD_HASH";

    private final StringRedisTemplate redisTemplate;

    public CardService(StringRedisTemplate redisTemplate) {
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
                Long id = redisTemplate.opsForValue().increment(KEY_CARD_SEQ, 1);
                if (id == null) {
                    errMsg.append("获取ID失败，导入行错误：").append(line).append("\n");
                    continue;
                }
                UsaCard card = new UsaCard();
                card.setId(id);
                card.setCard(arr[0]);
                card.setLink(arr[1]);
                redisTemplate.opsForHash().put(KEY_CARD_HASH, id.toString(), JSON.toJSONString(card));
            }
        }
        if (StringUtils.hasLength(errMsg)) {
            return Result.error(errMsg.toString());
        }
        return Result.success(null);
    }

    public Result<UsaCard> getCard() {
        Long currentSeqId = getCurrentSeqCardId();
        if (currentSeqId == null) {
            return Result.error("取卡失败，当前无卡");
        }
        if (currentSeqId == -99) {
            return Result.error("取卡失败，解析卡序列ID失败");
        }
        if (currentSeqId < 1) {
            return Result.error("取卡失败，当前无卡");
        }
        Long currentId = redisTemplate.opsForValue().increment(KEY_CARD_CURRENT_INDEX);
        if (currentId == null) {
            return Result.error("取卡失败，获取游标失败");
        }
        if (currentId > currentSeqId) {
            currentId = 1L;
            redisTemplate.opsForValue().set(KEY_CARD_CURRENT_INDEX, currentId.toString());
        }
        String value = (String) redisTemplate.opsForHash().get(KEY_CARD_HASH, currentId.toString());
        return Result.success(JSON.parseObject(value, UsaCard.class));
    }

    public int getCardCount() {
        Long size = redisTemplate.opsForHash().size(KEY_CARD_HASH);
        return size.intValue();
    }

    public Result<Void> clear() {
        redisTemplate.delete(KEY_CARD_SEQ);
        redisTemplate.delete(KEY_CARD_CURRENT_INDEX);
        redisTemplate.delete(KEY_CARD_HASH);
        return Result.success(null);
    }

    /**
     * 获取当前ID
     * @return ID
     */
    private Long getCurrentSeqCardId() {
        String idStr = redisTemplate.opsForValue().get(KEY_CARD_SEQ);
        if (!StringUtils.hasLength(idStr)) {
            return null;
        }
        Long id = null;
        try {
            id = Long.parseLong(idStr);
        } catch (Exception e) {
            log.error("卡ID解析错误");
        }
        if (id == null) {
            return -99L;
        }
        return id;
    }

}
