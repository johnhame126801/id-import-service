package com.abc.controller;

import com.abc.model.*;
import com.abc.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 批量创建订单
     * @param params  参数
     * @return Result
     */
    @PostMapping("/batch")
    public Result<Void> createOrder(@RequestBody CreateOrderParams params) {
        return orderService.createOrderBatch(params.getIdCount(), params.getPriority(), params.getOrderNum(), params.getRemark());
    }

    /**
     * 获取订单列表
     * @return Result
     */
    @GetMapping("/all")
    public Result<List<OrderDTO>> getAllOrder() {
        return orderService.getAllOrder();
    }

    /**
     * 删除订单
     * @param params  参数
     * @return  Result
     */
    @DeleteMapping("")
    public Result<Void> delete(@RequestBody DeleteOrderParams params) {
        return orderService.deleteOrder(params.getIds());
    }

    /**
     * 获取订单下ID列表
     * @param id  订单ID
     * @return Result
     */
    @GetMapping("/id")
    public Result<List<AppleID>> getIdList(String id) {
        return orderService.getIdList(id);
    }

    /**
     * 清理订单数据
     * @return Result
     */
    @DeleteMapping("/clear")
    public Result<Void> clear() {
        return orderService.clear();
    }

}
