package com.htsc.hgxn.platform.legal.pojo;

import com.htsc.hgxn.platform.common.pojo.BaseDocument;
import com.htsc.hgxn.platform.common.pojo.ProcessInfo;
import com.htsc.hgxn.platform.organization.pojo.BaseEntity;
import lombok.Data;

import java.util.List;

@Data
public class LegalContentCollect extends BaseEntity {

    private ProcessInfo processInfo; //流程基本信息

    private String processInstanceId;//流程实例id
/*
    private String processCode;//流程编号

    private String createdBy; //拟稿人

    private String communicationUserDept;//发起人所属部门

    private String processStartTime;//发起时间

    private String title;//标题*/

    private String contactInfo ;//拟稿人及其联系方式 拟稿人+空格+联系方式

    private String collectTime ;//收文时间

    private String notifyType ;//通知形式

    private String notifyPublisher;//通知发布人

    private String notifyUnit;//通知单位

    private List<BaseDocument> documents;//附件


}
