package website2018.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import website2018.domain.LiveSource;

public interface LiveSourceDao
        extends PagingAndSortingRepository<LiveSource, Long>, JpaSpecificationExecutor<LiveSource> {

    public LiveSource findByActive(int active);

}
