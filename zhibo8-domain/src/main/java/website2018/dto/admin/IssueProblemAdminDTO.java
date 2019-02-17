package website2018.dto.admin;


import website2018.domain.Issue;
import website2018.domain.ProblemDb;

import java.util.Date;

public class IssueProblemAdminDTO  {
    public Long id;
    public Date addTime;
    public Date updateTime;

    public String answer;

    public ProblemDb problemDb;

    public Issue issue;
}
