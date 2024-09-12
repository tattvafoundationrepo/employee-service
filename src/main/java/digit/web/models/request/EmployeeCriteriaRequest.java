package digit.web.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import digit.web.models.EmployeeData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeCriteriaRequest {
    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("EmployeeData")
    private EmployeeData employeeData;

    @JsonProperty("EMP_ID")
    private String empId;

    private Long createdAt;
    private Long updatedAt;
    private String createdBy;
    private String updatedBy;
}
