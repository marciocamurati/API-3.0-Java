package cieloecommerce.sdk.ecommerce.request;

import cieloecommerce.sdk.Environment;
import cieloecommerce.sdk.Merchant;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Abstraction to reuse most of the code that send and receive the HTTP
 * messages.
 */
public abstract class AbstractSaleRequest<Request, Response> {
	final Environment environment;
	private final Merchant merchant;
	private HttpClient httpClient;

	/*
	 * RegExp patterns to PCI log informations
	 */
	private final Pattern patternCreditCardNumber = Pattern.compile("(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|" +
			"6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|" +
			"[68][0-9])[0-9]{11}|(?:2131|1800|35\\d{3})\\d{11})");

	private final Pattern patternSecurityCode = Pattern.compile("\\\"(\\d{3}|\\d{4})\\\"");

	private final Pattern patternValidateDate = Pattern.compile("\\\"\\d{1,2}/\\d{4}\\\"");

	private final Pattern patternMaskedCreditCardNumber = Pattern.compile("\\d{6}\\*+\\d{4}");

	private static final Logger LOG = Logger.getLogger(AbstractSaleRequest.class.getName());

	AbstractSaleRequest(Merchant merchant, Environment environment) {
		this.merchant = merchant;
		this.environment = environment;
	}

	public abstract Response execute(Request param) throws IOException, CieloRequestException;

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	/**
	 * Send the HTTP request to Cielo with the mandatory HTTP Headers set
	 *
	 * @param request
	 *            The POST, PUT, GET request and its content is defined by the
	 *            derivations
	 * @return the HTTP response returned by Cielo
	 * @throws IOException
	 *             yeah, deal with it
	 */
	HttpResponse sendRequest(HttpUriRequest request) throws IOException {
		if (httpClient == null) {
			httpClient = HttpClientBuilder.create().build();
		}

		request.addHeader("Accept", "application/json");
		request.addHeader("Accept-Encoding", "gzip");
		request.addHeader("Content-Type", "application/json");
		request.addHeader("User-Agent", "CieloEcommerce/3.0 Android SDK");
		request.addHeader("MerchantId", merchant.getId());
		request.addHeader("MerchantKey", merchant.getKey());
		request.addHeader("RequestId", UUID.randomUUID().toString());

		if (LOG.isLoggable(Level.INFO))	{
			HttpEntity httpEntity = null;

			if (request instanceof HttpRequestWrapper)  {
				httpEntity = ((HttpEntityEnclosingRequest) request).getEntity();
			} else if(request instanceof HttpPost) {
				httpEntity = ((HttpPost)request).getEntity();
			}

			if (httpEntity != null) {
				InputStream content = httpEntity.getContent();

				String responseBody = IOUtils.toString(content, "UTF-8");

				LOG.info(String.format("sendRequest: %s", this.maskSensitiveCardInformations(responseBody)));

				try {
					content.reset();
				} catch (Exception e)    {
					content.mark(0);
				}
			}
		}

		return httpClient.execute(request);
	}

	/**
	 * Read the response body sent by Cielo
	 *
	 * @param response
	 *            HttpResponse by Cielo, with headers, status code, etc.
	 * @return An instance of Sale with the response entity sent by Cielo.
	 * @throws IOException
	 *             yeah, deal with it
	 * @throws CieloRequestException
	 */
	Response readResponse(HttpResponse response, Class<Response> responseClassOf)
			throws IOException, CieloRequestException {
		HttpEntity responseEntity = response.getEntity();
		InputStream responseEntityContent = responseEntity.getContent();

		Header contentEncoding = response.getFirstHeader("Content-Encoding");

		if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
			responseEntityContent = new GZIPInputStream(responseEntityContent);
		}

		BufferedReader responseReader = new BufferedReader(new InputStreamReader(responseEntityContent));
		StringBuilder responseBuilder = new StringBuilder();
		String line;

		while ((line = responseReader.readLine()) != null) {
			responseBuilder.append(line);
		}

		if (LOG.isLoggable(Level.INFO))	{
			LOG.info(String.format("readResponse: %s", this.maskSensitiveCardInformations(responseBuilder.toString())));
		}

		return parseResponse(response.getStatusLine().getStatusCode(), responseBuilder.toString(), responseClassOf);
	}

	/**
	 * <p>
	 *     Try to mask the sensitive credit card informations, like CVV, validate date and card number
	 *     If couldn't return a fixed text message
	 * </p>
	 * @param value
	 * @return
	 */
	private String maskSensitiveCardInformations(String value) {
		Matcher matcher = patternCreditCardNumber.matcher(value);

		if (matcher.find()) {
			String cardNumber = matcher.group(0);

			StringBuilder maskCardNumber = new StringBuilder();

			maskCardNumber.append(cardNumber.substring(0, 6));

			for (int mask = 0; mask < (cardNumber.length() - 10); mask++) {
				maskCardNumber.append("*");
			}

			maskCardNumber.append(cardNumber.substring(cardNumber.length() - 4));

			value = value.replace(cardNumber, maskCardNumber.toString());

			matcher = patternValidateDate.matcher(value);

			if (matcher.find()) {
				value = matcher.replaceAll("\"*\"");
			}

			matcher = patternSecurityCode.matcher(value);

			if (matcher.find()) {
				value =  matcher.replaceAll("\"*\"");
			}

			return value;
		}	else	{
			matcher = patternMaskedCreditCardNumber.matcher(value);

			if (matcher.find()) {
				matcher = patternValidateDate.matcher(value);

				if (matcher.find()) {
					value = matcher.replaceAll("\"*\"");
				}

				matcher = patternSecurityCode.matcher(value);

				if (matcher.find()) {
					value =  matcher.replaceAll("\"*\"");
				}

				return value;
			}
		}

		return "Find a credit card number but can't mask it!";
	}

	/**
	 * Just decode the JSON into a Sale or create the exception chain to be
	 * thrown
	 *
	 * @param statusCode
	 *            The status code of response
	 * @param responseBody
	 *            The response sent by Cielo
	 * @return An instance of Sale or null
	 * @throws CieloRequestException
	 */
	private Response parseResponse(int statusCode, String responseBody, Class<Response> responseClassOf)
			throws CieloRequestException {
		Response response = null;
		Gson gson = new Gson();

		switch (statusCode) {
		case 200:
		case 201:
			response = gson.fromJson(responseBody, responseClassOf);
			break;
		case 400:
			CieloRequestException exception = null;
			CieloError[] errors = gson.fromJson(responseBody, CieloError[].class);

			for (CieloError error : errors) {
				exception = new CieloRequestException(error.getMessage(), error, exception);
			}

			throw exception;
		case 404:
			throw new CieloRequestException("Not found", new CieloError(404, "Not found"), null);
		}

		return response;
	}
}
