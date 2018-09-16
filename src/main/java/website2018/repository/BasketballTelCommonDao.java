package website2018.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import website2018.domain.BasketballRank;
import website2018.domain.BasketballTelCommon;


public interface BasketballTelCommonDao extends PagingAndSortingRepository<BasketballTelCommon, Long>, JpaSpecificationExecutor<BasketballTelCommon> {


    BasketballTelCommon findByTypeAndTeamName(String type, String teamName);

}
