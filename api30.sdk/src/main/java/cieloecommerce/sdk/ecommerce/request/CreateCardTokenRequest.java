package cieloecommerce.sdk.ecommerce.request;

import cieloecommerce.sdk.Environment;
import cieloecommerce.sdk.Merchant;
import cieloecommerce.sdk.ecommerce.Card;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import java.io.IOException;

public class CreateCardTokenRequest extends AbstractSaleRequest<Card, Card> {
	public CreateCardTokenRequest(Merchant merchant, Environment environment) {
		super(merchant, environment);
	}

	@Override
	public Card execute(Card param) throws IOException, CieloRequestException {
		String url = environment.getApiUrl() + "1/card/";
		HttpPost request = new HttpPost(url);

		request.setEntity(new StringEntity(new GsonBuilder().create().toJson(param)));

		HttpResponse response = sendRequest(request);

		return readResponse(response, Card.class);
	}
}