/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020. All rights reserved.
 */

package com.huawei.bankcenter.controller;

import com.huawei.middleware.dtm.client.annotations.DTMTxBegin;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.core.MediaType;

@Component
@RestSchema(schemaId = "bankcenter")
@RequestMapping(path = "/bankcenter", produces = MediaType.APPLICATION_JSON)
public class BankCenterController {

    private final RestTemplate restInvoker = RestTemplateBuilder.create();

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BankCenterController.class);

    private static final String BANKA_TRANSFER = "cse://bank-a/banka/transfer?id=%s&money=%s";
    private static final String BANKA_INIT = "cse://bank-a/banka/init?userIds=%s&money=%s";
    private static final String BANKA_QUERYBYID = "cse://bank-a/banka/queryByID?id=%s";
    private static final String BANKA_QUERY = "cse://bank-a/banka/query";

    private static final String BANKB_TRANSFER = "cse://bank-b/bankb/transfer?id=%s&money=%s&errRate=%s";
    private static final String BANKB_INIT = "cse://bank-b/bankb/init?userIds=%s&money=%s";
    private static final String BANKB_QUERYBYID = "cse://bank-b/bankb/queryByID?id=%s";
    private static final String BANKB_QUERY = "cse://bank-b/bankb/query";


    @GetMapping(value = "/err-rate")
    public int getErrRate() {
        return errorRate.get();
    }

    DynamicIntProperty errorRate = DynamicPropertyFactory.getInstance().getIntProperty("dtm.error", 50);


    /**
     * 调用 bankA和bankB执行转账业务
     * @param id 账号
     * @param money 钱数
     * @param errRate 异常概率
     */
    @GetMapping(value = "transfer")
    @DTMTxBegin(appName = "noninvasive-transfer")
    public String transfer(@RequestParam(value = "id") int id, @RequestParam(value = "money") int money, @RequestParam(value = "errRate") int errRate) {
        LOGGER.info("Bank-center start invoke bankA and bankB");
        restInvoker.getForObject(String.format(BANKA_TRANSFER, id, money), String.class);
        restInvoker.getForObject(String.format(BANKB_TRANSFER, id, money, errRate), String.class);
        return "ok";
    }

    /**
     * 初始化 bankA和bankB数据库
     * @param userIds 账号
     * @param money 钱数
     */
    @GetMapping(value = "init")
    public String init(@RequestParam(value = "userIds") int userIds, @RequestParam(value = "money") int money) {
        LOGGER.info("Bank-center init bankA and bankB");
        restInvoker.getForObject(String.format(BANKA_INIT, userIds, money), String.class);
        restInvoker.getForObject(String.format(BANKB_INIT, userIds, money), String.class);
        return "ok";
    }

    /**
     * 根据 id 查询 bankA 余额
     * @param id 账号
     */
    @GetMapping(value = "queryAById")
    public long queryAById(@RequestParam(value = "id") int id) {
        LOGGER.info("Bank-center query bankA id {}", id);
        return restInvoker.getForObject(String.format(BANKA_QUERYBYID, id), Long.class);
    }

    /**
     * 根据 id 查询 bankB 余额
     * @param id 账号
     */
    @GetMapping(value = "queryBById")
    public long queryBById(@RequestParam(value = "id") int id) {
        LOGGER.info("Bank-center query bankB id {}", id);
        return restInvoker.getForObject(String.format(BANKB_QUERYBYID, id), Long.class);
    }

    /**
     * 查询 bankA 余额
     */
    @GetMapping(value = "queryA")
    public long queryA() {
        LOGGER.info("Bank-center query bankA");
        return restInvoker.getForObject(String.format(BANKA_QUERY), Long.class);
    }

    /**
     * 查询 bankB 余额
     */
    @GetMapping(value = "queryB")
    public long queryB() {
        LOGGER.info("Bank-center query bankB");
        return restInvoker.getForObject(String.format(BANKB_QUERY), Long.class);
    }
}
