package website2018.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import org.springframework.data.repository.query.Param;
import website2018.domain.Match;

public interface MatchDao extends PagingAndSortingRepository<Match, Long>, JpaSpecificationExecutor<Match> {

    public Match findBySource(String source);

    public List<Match> findByPlayDateStrOrderByPlayDateAsc(String dateStr);

    public List<Match> findByPlayDateStrAndProjectAndGame(String dateStr, String project, String game);

    public List<Match> findByPlayDateStrAndProject(String dateStr, String project);

    @Query(" select count(*) ,project  from Match t where addTime>= :addTime  group by project")
    List findByAddTimeGreaterThan(@Param("addTime") Date addTime);

    List<Match> findByAddTimeGreaterThanAndProject(Date d,String project);

}
