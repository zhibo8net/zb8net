package website2018.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import website2018.domain.BasketballRank;
import website2018.domain.BasketballTelCommon;

import java.util.List;


public interface BasketballTelCommonDao extends PagingAndSortingRepository<BasketballTelCommon, Long>, JpaSpecificationExecutor<BasketballTelCommon> {


    BasketballTelCommon findByTypeAndTeamName(String type, String teamName);

    //得分
    List<BasketballTelCommon> findByTypeOrderByScoreDesc(String type);
    //篮板
    List<BasketballTelCommon> findByTypeOrderByBackboardDesc(String type);

    //助攻 抢断 盖帽
    List<BasketballTelCommon> findByTypeOrderByMatchAvgDesc(String type);


}
