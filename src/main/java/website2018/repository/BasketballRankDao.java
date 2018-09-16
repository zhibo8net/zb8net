package website2018.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import website2018.domain.BasketballRank;


public interface BasketballRankDao extends PagingAndSortingRepository<BasketballRank, Long>, JpaSpecificationExecutor<BasketballRank> {


    BasketballRank findByTypeAndTeamName(String type,String teamName);

}
