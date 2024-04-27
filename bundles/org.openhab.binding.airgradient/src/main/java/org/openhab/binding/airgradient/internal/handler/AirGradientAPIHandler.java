/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.airgradient.internal.handler;

import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.*;

import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.airgradient.internal.config.AirGradientAPIConfiguration;
import org.openhab.binding.airgradient.internal.discovery.AirGradientLocationDiscoveryService;
import org.openhab.binding.airgradient.internal.model.LedMode;
import org.openhab.binding.airgradient.internal.model.Measure;
import org.openhab.binding.airgradient.internal.prometheus.PrometheusMetric;
import org.openhab.binding.airgradient.internal.prometheus.PrometheusTextParser;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link AirGradientAPIHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class AirGradientAPIHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(AirGradientAPIHandler.class);

    private @Nullable ScheduledFuture<?> pollingJob;
    private final HttpClient httpClient;
    private final Gson gson;

    public AirGradientAPIHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
        this.gson = new Gson();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            pollingCode();
        } else {
            // This is read only
            logger.warn("Received command {} for channel {}, but the API is read only", command.toString(),
                    channelUID.getId());
        }
    }

    @Override
    public void initialize() {
        AirGradientAPIConfiguration config = getConfiguration();

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            try {
                ContentResponse response = restCall(generatePingUrl());
                if (isSuccess(response)) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            response.getContentAsString());
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        });

        pollingJob = scheduler.scheduleWithFixedDelay(this::pollingCode, 5, config.refreshInterval, TimeUnit.SECONDS);
    }

    private static String getMeasureId(Measure measure) {
        String id = measure.getLocationId();
        if (id.isEmpty()) {
            // Local devices don't have location ID.
            id = measure.getSerialNo();
        }

        return id;
    }

    protected void pollingCode() {
        List<Measure> measures = getMeasures();
        Map<String, Measure> measureMap = measures.stream().collect(Collectors.toMap((m) -> getMeasureId(m), (m) -> m));

        for (Thing t : getThing().getThings()) {
            if (t.getHandler() instanceof AirGradientLocationHandler handler) {
                String locationId = handler.getLocationId();
                @Nullable
                Measure measure = measureMap.get(locationId);
                if (measure != null) {
                    handler.setMeasurment(locationId, measure);
                } else {
                    logger.debug("Could not find measures for location {}", locationId);
                }
            }
        }
    }

    /**
     * Return location ids we already have things for.
     * 
     * @return location ids we already have things for.
     */
    public List<String> getRegisteredLocationIds() {
        List<Thing> things = getThing().getThings();
        List<String> results = new ArrayList<>(things.size());
        for (Thing t : things) {
            if (t.getHandler() instanceof AirGradientLocationHandler handler) {
                results.add(handler.getLocationId());
            }
        }

        return results;
    }

    /**
     * Return list of measures from AirGradient API.
     *
     * @return list of measures
     */
    public List<Measure> getMeasures() {
        try {
            ContentResponse response = restCall(generateMeasuresUrl());
            String contentType = response.getMediaType();
            logger.debug("Got measurements with status {}: {} ({})", response.getStatus(),
                    response.getContentAsString(), contentType);
            if (isSuccess(response)) {
                updateStatus(ThingStatus.ONLINE);
                String stringResponse = response.getContentAsString().trim();

                if (CONTENTTYPE_JSON.equals(contentType)) {
                    return parseJson(stringResponse);
                } else if (CONTENTTYPE_TEXT.equals(contentType)) {
                    return parsePrometheus(stringResponse);
                } else if (CONTENTTYPE_OPENMETRICS.equals(contentType)) {
                    return parsePrometheus(stringResponse);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, response.getContentAsString());
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }

        return Collections.emptyList();
    }

    public void setLedMode(String serialNo, String mode) {
        Request request = httpClient.newRequest(generateGetLedsModeUrl(serialNo));
        request.timeout(REQUEST_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        request.method(HttpMethod.PUT);
        request.header(HttpHeader.CONTENT_TYPE, CONTENTTYPE_JSON);
        LedMode ledMode = new LedMode();
        ledMode.mode = mode;
        String modeJson = gson.toJson(ledMode);
        logger.debug("Setting LEDS mode for {}: {}", serialNo, modeJson);
        request.content(new StringContentProvider(CONTENTTYPE_JSON, modeJson, StandardCharsets.UTF_8));
        try {
            ContentResponse response = request.send();
            logger.debug("Response from setting LEDs mode: {}", response.getStatus());
            if (isSuccess(response)) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, response.getContentAsString());
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    public void calibrateCo2(String serialNo) {
        logger.debug("Triggering CO2 calibration for {}", serialNo);
        try {
            ContentResponse response = restCall(generateCalibrationCo2Url(serialNo), HttpMethod.POST);
            logger.debug("Response from calibration: {}", response.getStatus());
            if (isSuccess(response)) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, response.getContentAsString());
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private List<Measure> parsePrometheus(String stringResponse) {
        List<PrometheusMetric> metrics = PrometheusTextParser.parse(stringResponse);
        Measure measure = new Measure();

        for (PrometheusMetric metric : metrics) {
            if (metric.getMetricName().equals("pm01")) {
                measure.pm01 = metric.getValue();
            } else if (metric.getMetricName().equals("pm02")) {
                measure.pm02 = metric.getValue();
            } else if (metric.getMetricName().equals("pm10")) {
                measure.pm10 = metric.getValue();
            } else if (metric.getMetricName().equals("rco2")) {
                measure.rco2 = metric.getValue();
            } else if (metric.getMetricName().equals("atmp")) {
                measure.atmp = metric.getValue();
            } else if (metric.getMetricName().equals("rhum")) {
                measure.rhum = metric.getValue();
            } else if (metric.getMetricName().equals("tvoc")) {
                measure.tvoc = metric.getValue();
            } else if (metric.getMetricName().equals("nox")) {
                measure.noxIndex = metric.getValue();
            } else if (metric.getMetricName().equals("airgradient_wifi_rssi_dbm")) {
                measure.wifi = metric.getValue();
            } else if (metric.getMetricName().equals("airgradient_co2_ppm")) {
                measure.rco2 = metric.getValue();
            } else if (metric.getMetricName().equals("airgradient_pm1_ugm3")) {
                measure.pm01 = metric.getValue();
            } else if (metric.getMetricName().equals("airgradient_pm2d5_ugm3")) {
                measure.pm02 = metric.getValue();
            } else if (metric.getMetricName().equals("airgradient_pm10_ugm3")) {
                measure.pm10 = metric.getValue();
            } else if (metric.getMetricName().equals("airgradient_pm0d3_p100ml")) {
                measure.pm003Count = metric.getValue();
            } else if (metric.getMetricName().equals("airgradient_tvoc_index")) {
                measure.tvoc = metric.getValue();
            } else if (metric.getMetricName().equals("airgradient_tvoc_raw_index")) {
                measure.tvocIndex = metric.getValue();
            } else if (metric.getMetricName().equals("airgradient_nox_index")) {
                measure.noxIndex = metric.getValue();
            } else if (metric.getMetricName().equals("airgradient_temperature_degc")) {
                measure.atmp = metric.getValue();
            } else if (metric.getMetricName().equals("airgradient_humidity_percent")) {
                measure.rhum = metric.getValue();
            }

            if (metric.getLabels().containsKey("id")) {
                String id = metric.getLabels().get("id");
                measure.serialno = id;
                measure.locationId = id;
                measure.locationName = id;
            }

            if (metric.getLabels().containsKey("airgradient_serial_number")) {
                String id = metric.getLabels().get("airgradient_serial_number");
                measure.serialno = id;
                measure.locationId = id;
                measure.locationName = id;
            }
        }

        return Arrays.asList(measure);
    }

    private List<Measure> parseJson(String stringResponse) {
        List<@Nullable Measure> measures = null;
        if (stringResponse.startsWith("[")) {
            // Array of measures, like returned from the AirGradients API
            Type measuresType = new TypeToken<List<@Nullable Measure>>() {
            }.getType();
            measures = gson.fromJson(stringResponse, measuresType);
        } else if (stringResponse.startsWith("{")) {
            // Single measure e.g. if you read directly from the device
            Type measureType = new TypeToken<Measure>() {
            }.getType();
            Measure measure = gson.fromJson(stringResponse, measureType);
            measures = new ArrayList<@Nullable Measure>(1);
            measures.add(measure);
        }

        if (measures != null) {
            List<@Nullable Measure> nullableMeasuresWithoutNulls = measures.stream().filter(Objects::nonNull).toList();
            List<Measure> measuresWithoutNulls = new ArrayList<>(nullableMeasuresWithoutNulls.size());
            for (@Nullable
            Measure m : nullableMeasuresWithoutNulls) {
                if (m != null) {
                    measuresWithoutNulls.add(m);
                }
            }

            return measuresWithoutNulls;
        }

        return Collections.emptyList();
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> p = pollingJob;
        if (p != null) {
            p.cancel(true);
            pollingJob = null;
        }
    }

    private @Nullable String generatePingUrl() {
        AirGradientAPIConfiguration config = getConfiguration();
        if (hasCloudUrl(config)) {
            return config.hostname + PING_PATH;
        } else {
            return config.hostname;
        }
    }

    private @Nullable String generateMeasuresUrl() {
        AirGradientAPIConfiguration config = getConfiguration();
        if (hasCloudUrl(config)) {
            return config.hostname + String.format(CURRENT_MEASURES_PATH, config.token);
        } else {
            return config.hostname;
        }
    }

    private @Nullable String generateCalibrationCo2Url(String serialNo) {
        AirGradientAPIConfiguration config = getConfiguration();
        if (hasCloudUrl(config)) {
            return config.hostname + String.format(CALIBRATE_CO2_PATH, serialNo, config.token);
        } else {
            return config.hostname;
        }
    }

    private @Nullable String generateGetLedsModeUrl(String serialNo) {
        AirGradientAPIConfiguration config = getConfiguration();
        if (hasCloudUrl(config)) {
            return config.hostname + String.format(LEDS_MODE_PATH, serialNo, config.token);
        } else {
            return config.hostname;
        }
    }

    /**
     * Returns true if this is a URL against the cloud.
     *
     * @return true if this is a URL against the cloud API
     */
    private boolean hasCloudUrl(AirGradientAPIConfiguration config) {
        URI url = URI.create(config.hostname);
        return url.getPath().equals("/");
    }

    private AirGradientAPIConfiguration getConfiguration() {
        return getConfigAs(AirGradientAPIConfiguration.class);
    }

    private @Nullable ContentResponse restCall(@Nullable String url)
            throws InterruptedException, TimeoutException, ExecutionException {
        return restCall(url, HttpMethod.GET);
    }

    private @Nullable ContentResponse restCall(@Nullable String url, HttpMethod method)
            throws InterruptedException, TimeoutException, ExecutionException {
        if (url == null) {
            return null;
        }

        Request request = httpClient.newRequest(url);
        request.timeout(REQUEST_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        request.method(method);
        return request.send();
    }

    private static boolean isSuccess(@Nullable ContentResponse response) {
        if (response == null) {
            return false;
        }

        int status = response.getStatus();
        return status >= 200 && status < 300;
    }

    // Discovery

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(AirGradientLocationDiscoveryService.class);
    }
}
