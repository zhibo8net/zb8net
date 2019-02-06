package website2018.service.admin;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import website2018.domain.ProblemDb;
import website2018.exception.ErrorCode;
import website2018.exception.ServiceException;
import website2018.repository.ProblemDbDao;

import java.util.Date;

/**
 * Created by Administrator on 2019/1/27.
 */
@Service
public class ProblemDbAdminService {


    @Autowired
    ProblemDbDao problemDbDao;


    @Transactional(readOnly = true)
    public Page<ProblemDb> findAll(Specification<ProblemDb> specification, Pageable pageable) {
        return problemDbDao.findAll(specification, pageable);
    }

    @Transactional
    public void delete(Long id) {
        ProblemDb problemDb = problemDbDao.findOne(id);

        if (problemDb == null) {
            throw new ServiceException("该题目不存在", ErrorCode.BAD_MESSAGE_REQUEST);
        }

        problemDbDao.delete(id);
    }

    @Transactional(readOnly = true)
    public ProblemDb findOne(Long id) {
        return problemDbDao.findOne(id);
    }
    @Transactional
    public void create(ProblemDb problemDb) {
      if(problemDb==null){
          throw new ServiceException("请求数据不正确", ErrorCode.BAD_MESSAGE_REQUEST);
      }
        Date d=new Date();
        problemDb.addTime=d;
        problemDb.updateTime=d;
        problemDbDao.save(problemDb);

    }
}
