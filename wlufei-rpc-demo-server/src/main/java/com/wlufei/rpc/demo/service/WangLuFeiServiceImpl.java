package com.wlufei.rpc.demo.service;

import com.wlufei.rpc.demo.api.GoingMerry;
import com.wlufei.rpc.demo.api.WangLufeiService;
import com.wlufei.rpc.demo.exception.BizException;
import lombok.extern.slf4j.Slf4j;


/**
 * impl服务
 *
 * @author labu
 * @date 2021/08/01
 */
@Slf4j
public class WangLuFeiServiceImpl implements WangLufeiService {
    @Override
    public String onePiece(GoingMerry goingMerry) {
        String captain = goingMerry.getCaptain();
        if (null == goingMerry.getPartners() || goingMerry.getPartners().isEmpty()) {
            throw new BizException(captain + ", you have no partners.can't begin lookup one piece");
        }
        StringBuilder result = new StringBuilder(captain);

        goingMerry.getPartners().forEach(p -> result.append(",").append(p));
        return "this one piece for you " + result.toString();
    }
}
