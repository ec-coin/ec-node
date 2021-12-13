package nl.hanze.ec.node.responses;

import com.google.gson.JsonElement;

public class StandardResponse {
    private StatusResponse status;
    private String message;
    private JsonElement data;

    public StandardResponse(StatusResponse status) {
        this.status = status;
    }

    public StandardResponse(StatusResponse status, JsonElement data) {
        if (data.isJsonNull()) {
            this.status = StatusResponse.NO_CONTENT;
            System.out.println("REACCHED ------------------------------------------------------------------------------------------------");
        }
        else {
            this.status = status;
        }
        this.data = data;
    }

    public StatusResponse getStatusResponse() {
        return status;
    }

    public JsonElement getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public void setData(JsonElement data) {
        this.data = data;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatusResponse(StatusResponse status) {
        this.status = status;
    }
}
