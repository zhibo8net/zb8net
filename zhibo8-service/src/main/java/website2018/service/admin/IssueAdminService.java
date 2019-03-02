package website2018.service.admin;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import website2018.Enum.IssueStatus;
import website2018.Enum.IssueUserStatus;
import website2018.Enum.MatchFlag;
import website2018.domain.*;
import website2018.dto.admin.IssueAdminDTO;
import website2018.exception.ErrorCode;
import website2018.exception.ServiceException;
import website2018.repository.IssueDao;
import website2018.repository.MatchDao;
import website2018.repository.ProblemDbDao;
import website2018.utils.StrUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2019/1/27.
 */
@Service
public class IssueAdminService {


    @Autowired
    IssueDao issueDao;

    @Autowired
    MatchDao matchDao;


    @Autowired
    ProblemDbDao problemDbDao;

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
    public void modifyIssue(IssueAdminDTO issueAdminDTO) {
        if(issueAdminDTO==null||StringUtils.isEmpty(issueAdminDTO.matchNameAndId)){
            throw new ServiceException("请求参数错误", ErrorCode.BAD_MESSAGE_REQUEST);
        }
        String[] str=issueAdminDTO.matchNameAndId.split("-");

        if(str.length<=1){
            throw new ServiceException("对阵信息不正确", ErrorCode.BAD_MESSAGE_REQUEST);
        }
        Issue issue=issueDao.findById(issueAdminDTO.id);
        if(issue==null){
            throw new ServiceException("请求参数错误", ErrorCode.BAD_MESSAGE_REQUEST);
        }
        if(!IssueStatus.INIT.getCode().equals(issue.status)){
            throw new ServiceException("只能操作已初始化状态的竞猜活动", ErrorCode.BAD_MESSAGE_REQUEST);
        }
        if(!issue.matchNameAndId.equals(issueAdminDTO.matchNameAndId)){
            Match match=    matchDao.findById(Long.parseLong(str[1]));
            if(match==null){
                throw new ServiceException("对阵信息不正确", ErrorCode.BAD_MESSAGE_REQUEST);
            }
            match.matchActiveFlag= MatchFlag.ATIVE_FLAG.getCode();

            matchDao.save(match);
            Match m=issue.match;
            m.matchNewFlag= "0";
            matchDao.save(m);
        }

        issueDao.save(issue);
        }
        @Transactional
    public void create(IssueAdminDTO issueAdminDTO) {
      if(issueAdminDTO==null|| StringUtils.isEmpty(issueAdminDTO.matchNameAndId)){
          throw new ServiceException("请选择对阵", ErrorCode.BAD_MESSAGE_REQUEST);
      }
        String[] str=issueAdminDTO.matchNameAndId.split("-");

        if(str.length<=1){
            throw new ServiceException("对阵信息不正确", ErrorCode.BAD_MESSAGE_REQUEST);
        }

        Match match=    matchDao.findById(Long.parseLong(str[1]));
        if(match==null){
            throw new ServiceException("对阵信息不正确", ErrorCode.BAD_MESSAGE_REQUEST);
        }



        match.matchActiveFlag= MatchFlag.ATIVE_FLAG.getCode();

        matchDao.save(match);

        Date d=new Date();
        Issue issue=new Issue();
        issue.matchNameAndId=issueAdminDTO.matchNameAndId;
        issue.match=match;
        issue.matchName=match.name;
        issue.project=match.project;
        issue.game=match.game;
        issue.playDate=match.playDate;
        issue.addTime=d;
        issue.updateTime=d;

        issue.status= IssueStatus.INIT.getCode();
        issue.statusDesc= IssueStatus.INIT.getDesc();
        String maxIssue=issueDao.findMaxIssue();
        Integer  integer=1;
        if (StringUtils.isNotEmpty(maxIssue)){
            integer= Integer.parseInt(maxIssue)+1;
        }

        issue.issue=StrUtils.addZeroForNum(integer.toString(),6);
        issueDao.save(issue);


        issueDao.save(issue);
    }


    @Transactional
    public void updateIssueUser(Issue issue) {
        if(issue==null){
            throw new ServiceException("请求参数错误", ErrorCode.BAD_MESSAGE_REQUEST);
        }
      Issue issuedb=  issueDao.findById(issue.id);
        if(issuedb==null){
            throw new ServiceException("请求参数错误", ErrorCode.BAD_MESSAGE_REQUEST);
        }
        if(!IssueStatus.DRAW.getCode().equals(issuedb.status)){
            throw new ServiceException("只能操作已开奖状态的竞猜活动", ErrorCode.BAD_MESSAGE_REQUEST);
        }

        List<IssueUser> issueUserList=issue.issueUserList;
        List<Long> listId= Lists.newArrayList();
        for(IssueUser issueUser:issueUserList){
            listId.add(issueUser.id);
        }

        int i=0;
        List<IssueUser> issuedbUserList=issuedb.issueUserList;
        for(IssueUser issueUser:issuedbUserList){
            if(listId.contains(issueUser.id)){
                i=i+1;
                issueUser.status= IssueUserStatus.AWARD.getCode();
            }else{
                issueUser.status=IssueUserStatus.UMAWARD.getCode();
            }
        }
        issuedb.awardNum=i+"";
        //设置派奖
        issuedb.status=IssueStatus.PIE_AWARD.getCode();
        issuedb.statusDesc=IssueStatus.PIE_AWARD.getDesc();
        issue.updateTime=new Date();
        issueDao.save(issuedb);

    }

    @Transactional
    public void issuePublic(IssueAdminDTO issueAdminDTO) {
        if(issueAdminDTO==null){
            throw new ServiceException("请求参数错误", ErrorCode.BAD_MESSAGE_REQUEST);
        }

        Issue issue=issueDao.findById(issueAdminDTO.id);
        if(issue==null){
            throw new ServiceException("请求参数错误", ErrorCode.BAD_MESSAGE_REQUEST);
        }

        if(!IssueStatus.INIT.getCode().equals(issue.status)){
            throw new ServiceException("只能发布初始化状态的竞猜活动", ErrorCode.BAD_MESSAGE_REQUEST);
        }

        issue.status=IssueStatus.DOING.getCode();
        issue.statusDesc=IssueStatus.DOING.getDesc();
        issue.updateTime=new Date();
        issueDao.save(issue);
    }

    @Transactional
    public void issueEnd(IssueAdminDTO issueAdminDTO) {
        if(issueAdminDTO==null){
            throw new ServiceException("请求参数错误", ErrorCode.BAD_MESSAGE_REQUEST);
        }
        if(StringUtils.isEmpty(issueAdminDTO.issueAnswer)){
            throw new ServiceException("请设置竞猜答案", ErrorCode.BAD_MESSAGE_REQUEST);
        }
        Issue issue=issueDao.findById(issueAdminDTO.id);
        if(issue==null){
            throw new ServiceException("请求参数错误", ErrorCode.BAD_MESSAGE_REQUEST);
        }

        if(!IssueStatus.DOING.getCode().equals(issue.status)){
            throw new ServiceException("只能操作竞猜中状态的竞猜活动", ErrorCode.BAD_MESSAGE_REQUEST);
        }
        issue.issueAnswer=issueAdminDTO.issueAnswer;
        issue.status=IssueStatus.MATCH_END.getCode();
        issue.statusDesc=IssueStatus.MATCH_END.getDesc();
        issue.updateTime=new Date();
        issueDao.save(issue);
    }
    }
