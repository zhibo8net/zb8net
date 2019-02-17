package website2018.dto.admin;

import website2018.base.BaseEntity;
import website2018.domain.Issue;
import website2018.domain.User;

import java.util.Date;

public class IssueUserAdminDTO extends BaseEntity {
    public Long id;
    public Date addTime;
    public Date updateTime;


    public User user;

    //手机
    public String userMobile;

    //微信
    public String userWx;

    public String status;

    //竞猜答案
    public String answer;

    //竞猜正确率
    public double answerRate;
    public Issue issue;
}
