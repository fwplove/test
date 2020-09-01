package com.fwp.demo.test;


import com.alibaba.fastjson.JSON;
import com.htsc.platform.spp.api.ModelApi;
import com.htsc.platform.spp.api.ProcessApi;
import com.htsc.platform.spp.api.TaskApi;
import com.htsc.platform.spp.pojo.PageWrapper;
import com.htsc.platform.spp.pojo.ResultObject;
import com.htsc.platform.spp.pojo.process.ProcessParam;
import com.htsc.platform.spp.pojo.task.TaskApprovelParam;
import com.htsc.platform.spp.pojo.task.TaskBase;
import com.htsc.platform.spp.pojo.task.TaskBaseParam;
import com.htsc.platform.spp.pojo.task.TaskDetailInfo;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/process")
public class ProcessTest {

    @Reference(version = "1.0.0", timeout = 30000, check = false, filter = "rpcinfocollect")
    private ProcessApi processApi;

    @Reference(version = "1.0.0", timeout = 30000, check = false, filter = "rpcinfocollect")
    private TaskApi taskApi;

    @Reference(version = "1.0.0", timeout = 30000, check = false, filter = "rpcinfocollect")
    private ModelApi modelApi;

    private static final String TENANT_ID = "HGXN";
    private static final String USER_NUMBER = "0220";
    private static final String BUSINESS_KEY = TENANT_ID + "01111";

    private static Map<String, String> map = new HashMap<>();

    static {

        map.put("同意", "agree");
        map.put("不同意", "reject");
    }

    @GetMapping("/start") //http://localhost:8080/process/start
    public String startProcess() {

        //根据流程模型名称获取流程定义id
        ProcessParam processParam = new ProcessParam();
        processParam.setTenantId(TENANT_ID);
        processParam.setModelName("流程学习测试");
        ResultObject<String> resultObject = processApi.getDefIdByModelName(processParam);
        String processDefId = resultObject.getData();
        System.out.println("processDefId = " + processDefId);

        //根据流程定义id启动流程,返回流程实例id
        processParam = new ProcessParam();
        processParam.setTenantId(TENANT_ID);
        processParam.setBusinessKey(BUSINESS_KEY);
        processParam.setStartUser("013005"); //内部人员 发起人
        processParam.setProcessDefinitionId(processDefId);

        //设置下个阶段审批人
        HashMap<String, Object> customObject = new HashMap<>();
        customObject.put("approvers", Arrays.asList("李四(ext_K0230062)", "王五(ext_K0230063)"));
        processParam.setCustomObject(customObject);
        resultObject = processApi.startProcess(processParam);
        String instanceId = resultObject.getData();
        System.out.println("instanceId = " + instanceId);

        //查询流程审批记录
        processParam.setProcessInstanceId(instanceId);
        ResultObject<List<TaskDetailInfo>> listResultObject = processApi.queryApprovalRecord(processParam);
        System.out.println("listResultObject = " + listResultObject);
        return "执行成功！";
    }

    /**
     * 审批
     */
    @GetMapping("/approve/{approvalResult}")
    public void approve(@PathVariable String approvalResult, @PathVariable String instanceId) {
        //http://localhost:8080/process/approve/同意/9035ca75-e8cf-11ea-a13e-0242ea7ad75d
        TaskApprovelParam taskApprovelParam = new TaskApprovelParam();
        taskApprovelParam.setTenantId(TENANT_ID);
        taskApprovelParam.setBusinessKey(BUSINESS_KEY);
        taskApprovelParam.setApprovalResult(approvalResult);
        taskApprovelParam.setApprovalAdvices("very good !");
        taskApprovelParam.setProcessInstanceId(instanceId);

        //获取任务ID并设置
        List<TaskBase> taskBases = queryTaskList("王五", "ext_K0230063");
        TaskBase taskBase = taskBases.get(0);
        String taskId = taskBase.getTaskId();
        System.out.println("taskId = " + taskId);
        taskApprovelParam.setTaskId(taskId);

        //设置自定义参数用于流程流转
        Map<String, Object> customObject = taskApprovelParam.getCustomObject();
        if (customObject == null || customObject.isEmpty()) {
            customObject = new HashMap<>();
        } else {
            customObject = taskApprovelParam.getCustomObject();
        }
        customObject.put("approvalResult", approvalResult);//审批结果
        customObject.put("approvers", Arrays.asList("王五(ext_K0230063)"));//下个阶段审批

//        if (customObject.containsKey(map.get(approvalResult))) {
//            customObject.put(map.get(approvalResult),customObject.get(map.get(approvalResult))+1);
//        }

        taskApprovelParam.setCustomObject(customObject);

        //审批人审批
        ResultObject<List<TaskDetailInfo>> listResultObject = taskApi.updateApprovalRecord(taskApprovelParam);
        List<TaskDetailInfo> data = listResultObject.getData();
        System.out.println("data = " + data);

    }

    /**
     * 查询代办任务
     */
    @GetMapping("/queryTaskList/{userName}/{userNumber}")//http://localhost:8080/process/queryTaskList/李四/ext_K0230062
    public List<TaskBase> queryTaskList(@PathVariable String userName, @PathVariable String userNumber) {
        //查询代办任务
        TaskBaseParam taskBaseParam = new TaskBaseParam();
        taskBaseParam.setTenantId(TENANT_ID);
        taskBaseParam.setBusinessKey(BUSINESS_KEY);
        taskBaseParam.setUserNumber(userNumber);

        if (userNumber.startsWith("ext")) {
            Map<String, String> extAssigneeOne = new HashMap<>();
            extAssigneeOne.put("extUserName", userName);
            extAssigneeOne.put("extUserNumber", userNumber);
            taskBaseParam.setUserNumber(JSON.toJSONString(extAssigneeOne));
        }
        PageWrapper<TaskBase> data = taskApi.queryTaskList(taskBaseParam).getData();
        List<TaskBase> dataList = data.getDataList();
        System.out.println("我的代办 = " + dataList);
        return dataList;
    }


}
