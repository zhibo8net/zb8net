package website2018.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import website2018.domain.News;

public interface NewsDao extends PagingAndSortingRepository<News, Long>, JpaSpecificationExecutor<News> {

    public List<News> findBySource(String source);

    public List<News> findTop32ByAddTimeGreaterThanOrderByIdDesc( Date addTime);
    public List<News> findTop16ByProjectOrderByAddTimeDesc( String  project);
}
