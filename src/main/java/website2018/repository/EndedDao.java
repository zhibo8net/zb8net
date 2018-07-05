package website2018.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import website2018.domain.Ended;

public interface EndedDao extends PagingAndSortingRepository<Ended, Long>, JpaSpecificationExecutor<Ended> {

    public List<Ended> findBySource(String source);
    
    public List<Ended> findTop20ByProjectOrderByIdDesc(String project);

    public List<Ended> findTop6ByProjectAndGameOrderByIdDesc(String project, String game);
}
