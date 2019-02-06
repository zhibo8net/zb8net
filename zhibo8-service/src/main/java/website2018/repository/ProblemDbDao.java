package website2018.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import website2018.domain.ProblemDb;


public interface ProblemDbDao extends PagingAndSortingRepository<ProblemDb, Long>, JpaSpecificationExecutor<ProblemDb> {


}
