package cieloecommerce.sdk.ecommerce;

import cieloecommerce.sdk.ecommerce.request.AdditionalData;
import com.google.gson.annotations.SerializedName;

/**
 * <p>
 *     Wallet object
 * </p>
 *
 * Created by marciocamurati on 30/06/17.
 */
public class Wallet {
    @SerializedName("Type")
    private Type type;

    @SerializedName("WalletKey")
    private String walletKey;

    @SerializedName("AdditionalData")
    private AdditionalData additionalData;

    public Wallet VisaCheckout(String walletKey)    {
        this.setType(Type.VISACHECKOUT);
        this.setWalletKey(walletKey);

        return this;
    }

    public Wallet MasterPass(String captureCode)    {
        this.setType(Type.MASTERPASS);
        this.setAdditionalData(new AdditionalData(captureCode));

        return this;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getWalletKey() {
        return walletKey;
    }

    public void setWalletKey(String walletKey) {
        this.walletKey = walletKey;
    }

    public AdditionalData getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(AdditionalData additionalData) {
        this.additionalData = additionalData;
    }

    /**
     * <p>
     *     Wallet types enumerator
     * </p>
     */
    public enum Type {
        VISACHECKOUT("VisaCheckout"),
        MASTERPASS("MasterPass");

        private String value;

        Type(String value)   {
            this.value = value;
        }
    }
}
