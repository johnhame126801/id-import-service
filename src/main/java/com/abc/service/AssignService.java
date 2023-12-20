package com.abc.service;

import com.abc.model.AppleID;
import com.alibaba.fastjson2.JSON;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class AssignService {

    /**
     * ID等待分配队列
     */
    public static final String KEY_WAITING_ASSIGN_QUEUE = "ID_WAITING_ASSIGN_QUEUE";

    /**
     * ID分配任务队列
     */
    private static final String KEY_ASSIGN_QUEUE = "TASK_ASSIGN_ID";

    private final StringRedisTemplate redisTemplate;

    private final RedissonClient redisson;

    private final OrderService orderService;

    private final RBlockingQueue<String> taskQueue;

    public AssignService(StringRedisTemplate redisTemplate, RedissonClient redisson, OrderService orderService) {
        this.redisTemplate = redisTemplate;
        this.redisson = redisson;
        this.orderService = orderService;
        taskQueue = redisson.getBlockingQueue(KEY_ASSIGN_QUEUE);
        startAssignThread();
    }

    /**
     * 开启分配线程
     */
    private void startAssignThread() {
        new Thread(() -> {
            while (true) {
                try {
                    String taskValue = taskQueue.poll(30, TimeUnit.SECONDS);
                    if (!StringUtils.hasLength(taskValue)) {
                        continue;
                    }
                    log.debug("ID分配任务 - 开始分配。 {}", taskValue);
                    // 获取进行中订单
                    String orderId = orderService.getProgressingOrder();
                    if (orderId == null) {
                        continue;
                    }
                    RLock lock = redisson.getLock(orderId);
                    lock.lock();
                    try {
                        // 获取待分配ID
                        String idValue = redisTemplate.opsForList().leftPop(KEY_WAITING_ASSIGN_QUEUE);
                        if (idValue != null) {
                            String value = (String) redisTemplate.opsForHash().get(IdService.KEY_ID_HASH, idValue);
                            AppleID appleID = JSON.parseObject(value, AppleID.class);
                            doAssign(appleID, orderId);
                        }
                    } catch (Exception e) {
                        log.error("ID分配任务 - 分配失败", e);
                    } finally {
                        lock.unlock();
                    }
                    log.debug("ID分配任务 - 分配完成。 {}", taskValue);
                } catch (Exception e) {
                    log.error("ID分配任务 - 分配失败", e);
                }
            }
        }).start();
    }

    /**
     * 分配库存
     */
    public void assignStock() {
        // 不存在进行中订单，不启动分配
        if (!orderService.existsProgressingOrder()) {
            return;
        }
        String stockId;
        do {
            stockId = redisTemplate.opsForList().leftPop(KEY_WAITING_ASSIGN_QUEUE);
            if (StringUtils.hasLength(stockId)) {
                try {
                    String orderId = orderService.getProgressingOrder();
                    if (orderId == null) {
                        redisTemplate.opsForList().rightPush(KEY_WAITING_ASSIGN_QUEUE, stockId);
                        return;
                    }
                    RLock lock = redisson.getLock(orderId);
                    lock.lock();
                    try {
                        String stock = (String) redisTemplate.opsForHash().get(IdService.KEY_ID_HASH, stockId);
                        AppleID appleID = JSON.parseObject(stock, AppleID.class);
                        doAssign(appleID, orderId);
                    } catch (Exception e) {
                        redisTemplate.opsForList().rightPush(KEY_WAITING_ASSIGN_QUEUE, stockId);
                        log.error("ID分配任务 - 分配失败", e);
                    } finally {
                        lock.unlock();
                    }
                } catch (Exception e) {
                    redisTemplate.opsForList().rightPush(KEY_WAITING_ASSIGN_QUEUE, stockId);
                    log.error("分配库存失败", e);
                }
            }
        } while (StringUtils.hasLength(stockId));
    }

    /**
     * 分配AppleID
     * @param appleID AppleID
     */
    public void assign(AppleID appleID) {
        redisTemplate.opsForList().rightPush(KEY_WAITING_ASSIGN_QUEUE, appleID.getId().toString());
        // 存在进行中订单，启动分配
        if (orderService.existsProgressingOrder()) {
            taskQueue.add(appleID.getId().toString());
        }
    }

    private void doAssign(AppleID appleID, String orderId) {
        // 分配ID
        orderService.pushAppleIDToOrder(appleID, orderId);
        // 更新ID状态
        appleID.setStatus(AppleID.STATUS_ASSIGNED);
        redisTemplate.opsForHash().put(IdService.KEY_ID_HASH, appleID.getId().toString(), JSON.toJSONString(appleID));
    }

}
