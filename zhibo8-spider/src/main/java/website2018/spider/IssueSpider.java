package website2018.spider;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import website2018.Enum.IssueCheck;
import website2018.Enum.IssueStatus;
import website2018.MyApplication;
import website2018.base.BaseSpider;
import website2018.domain.*;
import website2018.repository.*;

import java.text.DecimalFormat;
import java.util.*;

@Component
public class IssueSpider extends BaseSpider {

    private static Logger logger = LoggerFactory.getLogger(IssueSpider.class);

    @Autowired
    IssueDao issueDao;

    @Autowired
    IssueQuestionDao issueQuestionDao;

    @Autowired
    IssueUserDao issueUserDao;
    @Scheduled(cron = "0 0/10 * * * *")
    @Transactional
    public void runSchedule() throws Exception {
        if (MyApplication.DONT_RUN_SCHEDULED) {
            return;
        }

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    awardIssue();

                } catch (Exception e) {
                    logger.error(e.getLocalizedMessage());
                }
            }
        }, "IssueSpider - " + System.currentTimeMillis());
        t.start();
    }

    @Transactional
    public void awardIssue() throws Exception {
        try {
           List<Issue> issueList=issueDao.findByStatus(IssueStatus.DRAWING.getCode());
            for(Issue issue:issueList){
                try {
                    if(StringUtils.isEmpty(issue.issueAnswer)){
                        logger.error("开奖错误{},{}", issue.issue, "未设置开奖结果");
                        continue;
                    }

                    String[] ans=issue.issueAnswer.split("-");

                    List<IssueQuestion> issueQuestionList=issueQuestionDao.findByIssueId(issue.id);
                    List<IssueQuestion> tem= Lists.newArrayList();
                    for(IssueQuestion issueQuestion:issueQuestionList){
                        if(StringUtils.equals(issueQuestion.issueChecked, IssueCheck.CHECKED.getCode())){
                            tem.add(issueQuestion);
                        }
                    }
                    if(tem.size()!=ans.length){
                        logger.error("开奖错误{},{}",issue.issue, "开奖结果长度不对");
                        continue;
                    }
                    int len=ans.length;
                    List<IssueUser> issueUserList=issueUserDao.findByIssueId(issue.id);
                  userFor:  for(IssueUser issueUser:issueUserList){
                      issueUser.updateTime=new Date();
                      issueUser.answerRate=0;
                      try {
                          if(StringUtils.isEmpty(issueUser.answer)){
                              continue userFor;
                          }
                          String[] userAns=issueUser.answer.split("-");
                          if(userAns.length!=ans.length){
                              continue userFor;
                          }
                          double t=0;
                          for(int i=0;i<len;i++){
                              if(StringUtils.equals("0",ans[i])){
                                  t++;
                              }else{
                                  if(StringUtils.equals(userAns[i],ans[i])){
                                      t++;
                                  }
                              }
                          }
                          DecimalFormat df = new DecimalFormat("#.00");
                          issueUser.answerRate=Double.parseDouble(df.format((t*100/len)));
                      }catch (Exception e){
                          logger.error("开奖错误{},{}", issue.issue,issueUser.userMobile);

                      }
                      issueUserDao.save(issueUserList);
                    }
                    issue.status=IssueStatus.DRAW.getCode();
                    issue.statusDesc=IssueStatus.DRAW.getDesc();
                    issue.updateTime=new Date();
                    issueDao.save(issue);
                }catch (Exception e){
                    logger.error("开机错误{}，{}", issue.issue,e);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("系统自动开机奖错误{}", e);
        }

    }


}
