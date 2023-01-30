package overwatch.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class HttpService {

    private static final Logger logger = Logger.getLogger(HttpService.class.getName());

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    private static final String updateEndpoint = ConfigurationService.getString(ConfigurationService.Keys.SERVICE_UPDATE_ENDPOINT);

    private HttpService() {}

    public static void sendZoneUpdate(int[] zoneNrs){

        System.out.println(Arrays.toString(zoneNrs));

        final Collection<QueryParam> queryParams = new ArrayList<>(zoneNrs.length);
        for (int zoneNr : zoneNrs)
            queryParams.add(new QueryParam("zones", Integer.toString(zoneNr)));

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(buildUri(queryParams))
                    .header("key", ConfigurationService.getString(ConfigurationService.Keys.SERVICE_UPDATE_KEY))
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() > 204)
                logger.severe("Illegal response from service. Response-Status: '"
                        + response.statusCode()
                        + "', Response-Body: '"
                        + response.body() + "'.");
        }
        catch (Exception e){
            logger.severe("Could not send Http-Request to service.");
        }
    }

    private static URI buildUri(Collection<QueryParam> queryParams) {
        final String params = queryParams.stream()
                .map(queryParam -> queryParam.key + "=" + queryParam.value)
                .collect(Collectors.joining("&"));
        return URI.create(HttpService.updateEndpoint + "?" + params);

    }

    private record QueryParam(String key, String value){};

}
