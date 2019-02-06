package website2018.service.admin;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import website2018.Enum.IssueStatus;
import website2018.Enum.MatchFlag;
import website2018.domain.Issue;
import website2018.domain.Match;
import website2018.exception.ErrorCode;
import website2018.exception.ServiceException;
import website2018.repository.IssueDao;
import website2018.repository.MatchDao;
import website2018.utils.StrUtils;

import java.util.Date;

/**
 * Created by Administrator on 2019/1/27.
 */
@Service
public class IssueAdminService {


    @Autowired
    IssueDao issueDao;

    @Autowired
    MatchDao matchDao;
    @Transactional(readOnly = true)
    public Page<Issue> findAll(Specification<Issue> specification, Pageable pageable) {
        return issueDao.findAll(specification, pageable);
    }

    @Transactional
    public void delete(Long id) {
        Issue issue = issueDao.findOne(id);

        if (issue == null) {
            throw new ServiceException("期次不存在", ErrorCode.BAD_MESSAGE_REQUEST);
        }

        issueDao.delete(id);
    }

    @Transactional(readOnly = true)
    public Issue findOne(Long id) {
        return issueDao.findOne(id);
    }
    @Transactional
    public void create(Issue issue) {
      if(issue==null|| StringUtils.isEmpty(issue.matchNameAndId)){
          throw new ServiceException("请选择对阵", ErrorCode.BAD_MESSAGE_REQUEST);
      }
        String[] str=issue.matchNameAndId.split("-");

        if(str.length<=1){
            throw new ServiceException("对阵信息不正确", ErrorCode.BAD_MESSAGE_REQUEST);
        }

        Match match=    matchDao.findById(Long.parseLong(str[1]));
        if(match==null){
            throw new ServiceException("对阵信息不正确", ErrorCode.BAD_MESSAGE_REQUEST);
        }

        match.matchNewFlag= MatchFlag.ATIVE_FLAG.getCode();

        matchDao.save(match);

        Date d=new Date();
        issue.match=match;
        issue.matchName=match.name;
        issue.project=match.project;
        issue.game=match.game;
        issue.playDate=match.playDate;
        issue.addTime=d;
        issue.updateTime=d;

        issue.status= IssueStatus.DOING.getCode();
        issue.statusDesc= IssueStatus.DOING.getDesc();
        String maxIssue=issueDao.findMaxIssue();
        Integer  integer=1;
        if (StringUtils.isNotEmpty(maxIssue)){
            integer= Integer.parseInt(maxIssue)+1;
        }

        issue.issue=StrUtils.addZeroForNum(integer.toString(),6);
        issueDao.save(issue);

    }
}
