package de.sharknoon.slash.networking.endpoints.file;

import com.google.gson.annotations.Expose;
import org.bson.types.ObjectId;

public class FileMessage {

    @Expose
    private String sessionid = "";
    @Expose
    private FileMessageStatus status;
    @Expose
    private String base64Data = "";
    @Expose
    private ObjectId dataId;


    public FileMessage() {
    }

    public FileMessage(final String base64Image) {
        if (base64Image != null)
            this.base64Data = base64Image;
    }


    public String getBase64Data() {
        return base64Data;
    }

    public void setBase64Data(final String base64Data) {
        if (base64Data != null) {
            this.base64Data = base64Data;
        }
    }

    public void setBase64Data(final byte[] data) {
        if (data != null) {
            this.base64Data = new String(data);
        }
    }

    public FileMessageStatus getStatus() {
        return status;
    }

    public void setStatus(FileMessageStatus status) {
        this.status = status;
    }

    public ObjectId getDataId() {
        return dataId;
    }

    public void setDataId(ObjectId dataId) {
        this.dataId = dataId;
    }

    public String getSessionid() {
        return sessionid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }
}
