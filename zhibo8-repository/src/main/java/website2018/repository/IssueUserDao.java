package website2018.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import website2018.domain.IssueUser;


public interface IssueUserDao extends PagingAndSortingRepository<IssueUser, Long>, JpaSpecificationExecutor<IssueUser> {

    IssueUser findByUserIdAndIssueId(long userId,long issueId);
}
