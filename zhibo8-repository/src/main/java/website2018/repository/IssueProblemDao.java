package website2018.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import website2018.domain.IssueProblem;


public interface IssueProblemDao extends PagingAndSortingRepository<IssueProblem, Long>, JpaSpecificationExecutor<IssueProblem> {

}
