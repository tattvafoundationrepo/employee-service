package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeSearchCriteria {
    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("empCode")
    private String empCode;
}
