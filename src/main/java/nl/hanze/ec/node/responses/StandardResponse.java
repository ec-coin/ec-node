package nl.hanze.ec.node.responses;

import com.google.gson.JsonElement;

public class StandardResponse {
    private JsonElement meta_data;
    private StatusResponse status;
    private String message;
    private JsonElement data;

    public StandardResponse(StatusResponse status) {
        this.status = status;
    }

    public StandardResponse(StatusResponse status, String message) {
        this.status = status;
        this.message = message;
    }

    public StandardResponse(StatusResponse status, JsonElement data) {
        if (data.isJsonNull()) {
            this.status = StatusResponse.NO_CONTENT;
        }
        else {
            this.status = status;
        }
        this.data = data;
    }

    public StandardResponse(StatusResponse status, JsonElement meta_data, JsonElement data) {
        if (data.isJsonNull()) {
            this.status = StatusResponse.NO_CONTENT;
        }
        else {
            this.status = status;
        }
        this.data = data;
        this.meta_data = meta_data;
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
