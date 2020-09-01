package com.htsc.hgxn.platform.legal.service;

import com.htsc.hgxn.platform.common.pojo.ResultObject;

import java.util.Map;

/**
 * @author : K0230049
 * @Package com.htsc.hgxn.platform.legal.service
 * @Description: 监管收文服务接口
 * @date 2019/9/16 19:09
 */
public interface LegalContentCollectService {

    ResultObject<Object> startContentCollectProcess(Map<String,Object> param);

    ResultObject<Object> approvalContentCollectProcess(Map<String,Object> param);

}
