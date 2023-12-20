package com.abc.service;

import com.abc.model.*;
import com.alibaba.fastjson2.JSON;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Log4j2
public class OrderService {

    /**
     * 进行中订单: 优先级0
     */
    private static final String KEY_PROGRESSING_ORDER_0 = "PROGRESSING_ORDER:0";

    /**
     * 进行中订单: 优先级1
     */
    private static final String KEY_PROGRESSING_ORDER_1 = "PROGRESSING_ORDER:1";

    /**
     * 订单哈希表
     */
    private static final String KEY_ORDER_HASH = "ORDER_HASH";

    private static final String KEY_ORDER_ID_LIST = "ORDER_ID_LIST";


    /**
     * 最高优先级
     */
    private static final int MAX_PRIORITY = 0;

    /**
     * 最低优先级
     */
    private static final int MIN_PRIORITY = 1;
    private final StringRedisTemplate redisTemplate;

    public OrderService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 批量创建订单
     * @param idNum      订单ID数量
     * @param priority   优先级  0/1  0:优先  1:正常
     * @param orderNum   订单数量
     * @return Result
     */
    public Result<Void> createOrderBatch(int idNum, int priority, int orderNum, String remark) {
        if (idNum <= 0 || orderNum <= 0) {
            return Result.badRequest("参数错误");
        }
        if (priority < MAX_PRIORITY || priority > MIN_PRIORITY) {
            return Result.badRequest("参数错误");
        }
        for (int i = 0; i < orderNum; i++) {
            createOrder(idNum, priority, remark);
        }
        return Result.success(null);
    }

    /**
     * 创建订单
     * @param idNum      订单ID数量
     * @param priority   优先级  0/1  0:优先  1:正常
     */
    private void createOrder(int idNum, int priority, String remark) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Order order = new Order();
        order.setId(UUID.randomUUID().toString().replaceAll("-", "").toLowerCase());
        order.setPriority(priority);
        order.setIdCount(idNum);
        order.setCreateTime(fmt.format(new Date()));
        order.setStatus(Order.STATUS_PROGRESSING);
        order.setRemark(remark);
        String storedValue = JSON.toJSONString(order);
        // 存储到订单hash表中
        redisTemplate.opsForHash().put(KEY_ORDER_HASH, order.getId(), storedValue);
        // 存储到待分配ID队列中
        if (order.getPriority() == 0) {
            redisTemplate.opsForList().rightPush(KEY_PROGRESSING_ORDER_0, order.getId());
        } else {
            redisTemplate.opsForList().rightPush(KEY_PROGRESSING_ORDER_1, order.getId());
        }
    }

    /**
     * 是否存在进行中订单
     * @return 是否存在
     */
    public boolean existsProgressingOrder() {
        Long priority0Size = redisTemplate.opsForList().size(KEY_PROGRESSING_ORDER_0);
        if (priority0Size != null && priority0Size > 0) {
            return true;
        }
        Long priority1Size = redisTemplate.opsForList().size(KEY_PROGRESSING_ORDER_1);
        return priority1Size != null && priority1Size > 0;
    }

    /**
     * 获取进行中订单
     * @return 订单ID
     */
    public String getProgressingOrder() {
        String orderId = redisTemplate.opsForList().leftPop(KEY_PROGRESSING_ORDER_0);
        if (StringUtils.hasLength(orderId)) {
            return orderId;
        }
        orderId = redisTemplate.opsForList().leftPop(KEY_PROGRESSING_ORDER_1);
        return orderId;
    }

    /**
     * 将ID加入订单
     * @param appleID  AppleID
     * @param orderId  订单ID
     */
    public void pushAppleIDToOrder(AppleID appleID, String orderId) {
        // 从订单hash表中获取订单
        String orderValue = (String) redisTemplate.opsForHash().get(KEY_ORDER_HASH, orderId);
        Order order = JSON.parseObject(orderValue, Order.class);
        // 订单不存在，停止分配
        if (order == null) {
            // ID放回待分配队列中
            redisTemplate.opsForList().rightPush(AssignService.KEY_WAITING_ASSIGN_QUEUE, appleID.getId().toString());
            return;
        }
        // 将ID加入订单
        String key = KEY_ORDER_ID_LIST + ":" + orderId;
        redisTemplate.opsForList().rightPush(key, appleID.getId().toString());
        // 更新订单状态
        Long idListSize = redisTemplate.opsForList().size(key);
        if (idListSize != null && idListSize >= order.getIdCount()) {
            // 已分配完成
            order.setStatus(Order.STATUS_COMPLETED);
            redisTemplate.opsForHash().put(KEY_ORDER_HASH, order.getId(), JSON.toJSONString(order));
        } else {
            // 未分配完成，订单放回进行中队列
            if (order.getPriority() == 0) {
                redisTemplate.opsForList().rightPush(KEY_PROGRESSING_ORDER_0, order.getId());
            } else {
                redisTemplate.opsForList().rightPush(KEY_PROGRESSING_ORDER_1, order.getId());
            }
        }
    }

    /**
     * 查询所有订单
     * @return  订单列表
     */
    public Result<List<OrderDTO>> getAllOrder() {
        List<Object> values = redisTemplate.opsForHash().values(KEY_ORDER_HASH);
        List<OrderDTO> result = new LinkedList<>();
        values.forEach(value -> {
            Order order = JSON.parseObject((String) value, Order.class);
            OrderDTO dto = new OrderDTO(order);
            Long size = redisTemplate.opsForList().size(KEY_ORDER_ID_LIST + ":" + order.getId());
            dto.setAssignNum(size == null ? 0 : size.intValue());
            result.add(dto);
        });
        return Result.success(result);
    }

    /**
     * 删除订单
     * @param ids  订单ID列表
     * @return 处理结果
     */
    public Result<Void> deleteOrder(List<String> ids) {
        if (ids != null) {
            ids.forEach(id -> {
                redisTemplate.opsForHash().delete(KEY_ORDER_HASH, id);
            });
        }
        return Result.success(null);
    }

    /**
     * 获取指定订单下所有ID列表
     * @param orderId  订单ID
     * @return  ID列表
     */
    public Result<List<AppleID>> getIdList(String orderId) {
        List<AppleID> result = new LinkedList<>();
        String key = KEY_ORDER_ID_LIST + ":" + orderId;
        Long size = redisTemplate.opsForList().size(key);
        if (size == null || size <= 0) {
            return Result.success(result);
        }
        List<String> values = redisTemplate.opsForList().range(key, 0, size - 1);
        if (values != null) {
            values.forEach(idValue -> {
                String value = (String) redisTemplate.opsForHash().get(IdService.KEY_ID_HASH, idValue);
                if (StringUtils.hasLength(value)) {
                    result.add(JSON.parseObject(value, AppleID.class));
                }
            });
        }
        return Result.success(result);
    }

    public Result<Void> clear() {
        redisTemplate.delete(KEY_ORDER_HASH);
        redisTemplate.delete(KEY_PROGRESSING_ORDER_0);
        redisTemplate.delete(KEY_PROGRESSING_ORDER_1);
        Set<String> keyList = redisTemplate.keys(KEY_ORDER_ID_LIST + ":*");
        if (keyList != null) {
            redisTemplate.delete(keyList);
        }
        return Result.success(null);
    }

}
