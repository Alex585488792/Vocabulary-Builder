import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class NetClientGet {

	// http://localhost:8080/RESTfulExample/json/product/get
	public static void main(String[] args) {

	  try {

		URL url = new URL("https://od-api.oxforddictionaries.com:443/api/v1/entries/en/green/definitions");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/json");
		conn.setRequestProperty("app_id", "7834b310");
		conn.setRequestProperty("app_key", "82781012e44b2637051c49060569cce5");

		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(
			(conn.getInputStream())));
		ArrayList<String> meanings = new ArrayList<String>();
		boolean subsenses = false;
		String output;
		while ((output = br.readLine()) != null) {
			String trimmed = output.trim();
			System.out.println(trimmed);
			if (trimmed.contains("subsenses")) {
				subsenses = true;
				System.out.println("true");
			}
			else if (trimmed.equals("]")) {
				subsenses = false;
				System.out.println("false");
			}
			if (trimmed.contains("\"definitions\"") && !subsenses) {
				System.out.println(trimmed);
				meanings.add(br.readLine().trim().replaceAll("^\"|\"$", ""));
			}
			if (trimmed.contains("lexicalCategory")) {
				String wordType = trimmed.replaceAll("\"lexicalCategory\": \"", "");
				wordType = wordType.replaceAll("\"", "").replaceAll(",", "");
				System.out.println(wordType);
			}
		}
		conn.disconnect();

	  } catch (MalformedURLException e) {

		e.printStackTrace();

	  } catch (IOException e) {

		e.printStackTrace();

	  }

	}

}