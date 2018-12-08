package website2018.service.admin;

import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springside.modules.utils.mapper.BeanMapper;
import website2018.domain.News;
import website2018.dto.NewsNoContentDTO;
import website2018.exception.ErrorCode;
import website2018.exception.ServiceException;
import website2018.repository.NewsDao;

@Service
public class NewsService {

    @Autowired
    NewsDao newsDao;

    @Transactional(readOnly = true)
    public List<News> findAll(Specification spec) {
        return newsDao.findAll(spec);
    }

    @Transactional(readOnly = true)
    public Page<News> findAll(Pageable pageable) {
        return newsDao.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<News> findAll(Specification<News> specification, Pageable pageable) {
        return newsDao.findAll(specification, pageable);
    }


    @Transactional(readOnly = true)
    public List<NewsNoContentDTO> findByMatchName(String matchName) {

        List<News> newses=newsDao.findByMatchName(matchName);

        List<NewsNoContentDTO> newsNoContentDTOs= Lists.newArrayList();
        for(News news:newses){
            NewsNoContentDTO mdto = BeanMapper.map(news, NewsNoContentDTO.class);
            newsNoContentDTOs.add(mdto);
        }
        return newsNoContentDTOs;
    }

    @Transactional(readOnly = true)
    public News findOne(Long id) {
        return newsDao.findOne(id);
    }

    @Transactional
    public void create(News news) {
        newsDao.save(news);
    }

    @Transactional
    public void modify(News news) {

        News orginalNews = newsDao.findOne(news.id);

        if (orginalNews == null) {
            throw new ServiceException("新闻不存在", ErrorCode.BAD_REQUEST);
        }
        
        orginalNews.project = news.project;
        orginalNews.game = news.game;
        orginalNews.title = news.title;
        orginalNews.source = news.source;
        orginalNews.image = news.image;
        orginalNews.content = news.content;
        orginalNews.matchName=news.matchName;
        orginalNews.updateTime=new Date();
        if(StringUtils.isNotEmpty(orginalNews.matchName)){
            orginalNews.matchPreFlag="1";
        }
        newsDao.save(orginalNews);
    }

    @Transactional
    public void delete(Long id) {
        News news = newsDao.findOne(id);

        if (news == null) {
            throw new ServiceException("新闻不存在", ErrorCode.BAD_REQUEST);
        }

        newsDao.delete(id);
    }

}
