package com.htsc.hgxn.platform.legal.service.impl;

import com.htsc.hgxn.common.filter.UserFilter;
import com.htsc.hgxn.common.util.Const;
import com.htsc.hgxn.common.util.DateUtils;
import com.htsc.hgxn.common.util.JSONUtil;
import com.htsc.hgxn.platform.aspect.OperationTraceLog;
import com.htsc.hgxn.platform.branch.dao.BranchTeamMapper;
import com.htsc.hgxn.platform.branch.pojo.BranchTeam;
import com.htsc.hgxn.platform.common.pojo.ProcessInfo;
import com.htsc.hgxn.platform.common.pojo.ResultObject;
import com.htsc.hgxn.platform.common.util.PropertyUtil;
import com.htsc.hgxn.platform.common.util.ResultFactory;
import com.htsc.hgxn.platform.legal.service.LegalContentCollectService;
import com.htsc.hgxn.platform.organization.dao.EvwInterfaceEmpMapper;
import com.htsc.hgxn.platform.organization.pojo.EvwInterfaceEmp;
import com.htsc.platform.spp.api.ProcessApi;
import com.htsc.platform.spp.pojo.process.ProcessParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * @author : K0230049
 * @Package com.htsc.hgxn.platform.legal.service.impl
 * @Description: 外部监管沟通记录相关接口
 * @date 2019/9/16 20:22
 */
@Service("LegalContentCollectService")
public class LegalContentCollectServiceImpl implements LegalContentCollectService {

    private static final Logger logger = LoggerFactory.getLogger(LegalContentCollectServiceImpl.class);

    private String TENANT_ID = PropertyUtil.getProperty("hgxn.platform.key");
    private String BUSINESS_KEY = PropertyUtil.getProperty("legal.content.collect.processModelName");
    private String MODEL_NAME = PropertyUtil.getProperty("legal.content.collect.business.key.prefix");


    @Autowired
    private BranchTeamMapper branchTeamMapper;

    @Autowired
    private EvwInterfaceEmpMapper empMapper;

    @Autowired
    private EvwInterfaceEmpMapper evwInterfaceEmpMapper;

    @Autowired
    private ProcessApi processApi;

    /**
     * 发起监管收文流程
     *
     * @param param
     * @return
     */
    @Override
    @OperationTraceLog(key = "发起监管收文流程")
    public ResultObject<Object> startContentCollectProcess(Map<String, Object> param) {
        if (Objects.isNull(param)) {
            return ResultFactory.error(400, Const.THE_REQUESY_PARAMETER_IS_NULL);
        }
        try {
            String userName = UserFilter.LOCAL.get().get("userName") == null ? "" : UserFilter.LOCAL.get().get("userName").toString();
            //查询发起人合规团队团队长
            BranchTeam team = branchTeamMapper.queryTeamByUserId(userName);
            String leaderId = team.getTeamLeaderId();
            String leaderName = empMapper.findRealNameByUserName(leaderId);
            String teamLeader = leaderName + "(" + leaderId + ")";
            param.put("teamLeader", teamLeader);

            //查询主岗所属部门
            EvwInterfaceEmp evwInterfaceEmp = evwInterfaceEmpMapper.queryMainDeptByUserName(userName);
            String department = evwInterfaceEmp.getDepartment();

            //生成businessKey
            String businessKey = BUSINESS_KEY + DateUtils.dateFormat(new Date(), DateUtils.DATE_TIME_YYYYMMDD_PATTERN);
            ProcessInfo processInfo;
            if (param.get("processInfo") == null) {
                processInfo = new ProcessInfo();
            } else {
                processInfo = JSONUtil.toBean(param.get("processInfo"), ProcessInfo.class);
            }
            processInfo.setProcessStarter(userName);
            processInfo.setSubordinateDept(department);
            processInfo.setProcessCode(businessKey);
            param.put("processInfo", processInfo);

            //获取最新流程定义
            ProcessParam processParam = new ProcessParam();
            processParam.setTenantId(TENANT_ID);
            processParam.setModelName(MODEL_NAME);
            processParam.setStartUser(userName);
            processParam.setBusinessKey(businessKey);
            com.htsc.platform.spp.pojo.ResultObject<String> result = processApi.getDefIdByModelName(processParam);
            String processDefId = result.getData() == null ? "" : result.getData();
            if ("".equals(processDefId)) {
                String msg = MODEL_NAME + Const.THE_PROCESS_DEFINITION_WAS_NOT_QUERIED;
                return ResultFactory.error(404, msg, null);
            }

            //设置流程定义id
            processParam.setProcessDefinitionId(processDefId);
            //设置流程标题
            processParam.setProcessTitle(processInfo.getProcessTitle());
            //设置系统名称
            param.put("reqSystemName", Const.SYSTEM_PLATFORM_NAME);
            processParam.setCustomObject(param);
            //发起流程
            com.htsc.platform.spp.pojo.ResultObject<String> result2 = processApi.startProcess(processParam);
            if (result2.getCode() != 200) {
                return ResultFactory.error(result2.getCode(), result2.getMsg(), result2.getData());
            }
            String processInstanceId = result2.getData();
            return ResultFactory.success(Const.PROCESS_STARTED_SUCCESSFULLY, processInstanceId);
        } catch (Exception e) {
            logger.error("发起监管收文流程异常：{}", e.getMessage());
            e.printStackTrace();
            return ResultFactory.error(500, "发起监管收文流程异常: " + e.getMessage(), null);
        }
    }


    /** 审批监管收文流程
     * @param param
     * @return
     */
    @Override
    @OperationTraceLog(key = "审批监管收文流程")
    public ResultObject<Object> approvalContentCollectProcess(Map<String, Object> param) {

        if(Objects.isNull(param)){
            return ResultFactory.error(400,Const.THE_REQUESY_PARAMETER_IS_NULL);
        }

        if(param.get("taskId") == null || "".equals(param.get("taskId"))){
            logger.info("taskId is null");
            return ResultFactory.error(400,Const.TASKID_CANNOT_BE_EMPTY,null);
        }


        return null;
    }
}
