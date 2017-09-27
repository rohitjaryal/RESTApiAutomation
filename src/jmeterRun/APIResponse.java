package jmeterRun;

import java.util.ArrayList;

public class APIResponse {
    private String apiUseCaseName = "";
    private String requestUrl = "";
    private String requestType = "";
    private String requestHeader = "";
    private String responseHeader = "";
    private String requestData = "";
    private String responseData = "";
    private String useCaseID = "";
    private ArrayList<String[]> assertion = new ArrayList<String[]>();
    private boolean useCaseResult = false;

    public APIResponse(String apiUseCaseName, String requestUrl, String requestType, String requestHeader,
                       String responseHeader, String requestData, String responseData, ArrayList<String[]> assertions,
                       String useCaseId, boolean useCaseTestResult) {
        setApiUseCaseName(apiUseCaseName);
        setRequestUrl(requestUrl);
        setRequestType(requestType);
        setRequestHeader(requestHeader);
        setResponseHeader(responseHeader);
        setRequestData(requestData);
        setResponseData(responseData);
        setAssertions(assertions);
        setUseCaseID(useCaseId);
        setUseCaseResult(useCaseTestResult);
    }

    public APIResponse() {
    }

    public String getApiUseCaseName() {
        return apiUseCaseName;
    }

    public void setApiUseCaseName(String apiUseCaseName) {
        this.apiUseCaseName = apiUseCaseName;
    }


    public void setUseCaseResult(boolean useCaseTestResult) {
        this.useCaseResult = useCaseTestResult;
    }

    public boolean getUseCaseResult() {
        return useCaseResult;
    }

    public String getUseCaseID() {
        return useCaseID;
    }

    public void setUseCaseID(String useCaseID) {
        this.useCaseID = useCaseID;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getRequestHeader() {
        return requestHeader;
    }

    public void setRequestHeader(String requestHeader) {
        this.requestHeader = requestHeader;
    }

    public String getResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(String responseHeader) {
        this.responseHeader = responseHeader;
    }

    public String getRequestData() {
        return requestData;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public ArrayList<String[]> getAssertions() {
        return assertion;
    }

    public void setAssertions(ArrayList<String[]> assertions) {
        this.assertion = assertions;
    }

}
