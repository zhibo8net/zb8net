package website2018.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import website2018.domain.FootballJsb;


public interface FootballJsbDao extends PagingAndSortingRepository<FootballJsb, Long>, JpaSpecificationExecutor<FootballJsb> {


    FootballJsb findByTypeAndTypeMoldAndTeamName(String type,String typeMold, String teamName);

}
