package cieloecommerce.sdk.ecommerce.request;

import com.google.gson.annotations.SerializedName;

/**
 * <p>
 *     Wallet additional data
 * </p>
 * Created by marciocamurati on 30/06/17.
 */
public class AdditionalData {
    @SerializedName("CaptureCode")
    private String captureCode;

    public AdditionalData(String captureCode)   {
        this.captureCode = captureCode;
    }

    public String getCaptureCode() {
        return captureCode;
    }

    public void setCaptureCode(String captureCode) {
        this.captureCode = captureCode;
    }
}
