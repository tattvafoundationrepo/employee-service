package digit.web.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import digit.web.models.EmployeeData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeResponse {
    @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo;

    @JsonProperty("EmployeeData")
    private List<EmployeeData> employeeData;


    @JsonProperty("Message")
    private String message;
}
