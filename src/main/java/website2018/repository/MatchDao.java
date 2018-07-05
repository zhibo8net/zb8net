package website2018.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import website2018.domain.Match;

public interface MatchDao extends PagingAndSortingRepository<Match, Long>, JpaSpecificationExecutor<Match> {

    public Match findBySource(String source);

    public List<Match> findByPlayDateStrOrderByPlayDateAsc(String dateStr);

    public List<Match> findByPlayDateStrAndProjectAndGame(String dateStr, String project, String game);

    public List<Match> findByPlayDateStrAndProject(String dateStr, String project);
}
