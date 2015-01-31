/*
 * Copyright 2014 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.metrics.intent;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.metrics.EventMetric;
import org.onlab.metrics.MetricsService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.intent.IntentEvent;
import org.onosproject.net.intent.IntentListener;
import org.onosproject.net.intent.IntentService;
import org.slf4j.Logger;

/**
 * ONOS Intent Metrics Application that collects intent-related metrics.
 */
@Component(immediate = true)
@Service
public class IntentMetrics implements IntentMetricsService,
                                      IntentListener {
    private static final Logger log = getLogger(IntentMetrics.class);

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentService intentService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected MetricsService metricsService;

    private ApplicationId appId;

    private LinkedList<IntentEvent> lastEvents = new LinkedList<>();
    private static final int LAST_EVENTS_MAX_N = 100;

    //
    // Metrics
    //
    private static final String COMPONENT_NAME = "Intent";
    private static final String FEATURE_SUBMITTED_NAME = "Submitted";
    private static final String FEATURE_INSTALLED_NAME = "Installed";
    private static final String FEATURE_WITHDRAW_REQUESTED_NAME =
        "WithdrawRequested";
    private static final String FEATURE_WITHDRAWN_NAME = "Withdrawn";
    //
    // Event metrics:
    //  - Intent Submitted API operation
    //  - Intent Installed operation completion
    //  - Intent Withdraw Requested API operation
    //  - Intent Withdrawn operation completion
    //
    private EventMetric intentSubmittedEventMetric;
    private EventMetric intentInstalledEventMetric;
    private EventMetric intentWithdrawRequestedEventMetric;
    private EventMetric intentWithdrawnEventMetric;

    @Activate
    protected void activate() {
        appId =
            coreService.registerApplication("org.onosproject.metrics.intent");

        clear();
        registerMetrics();
        intentService.addListener(this);
        log.info("Started with Application ID {}", appId.id());
    }

    @Deactivate
    public void deactivate() {
        intentService.removeListener(this);
        removeMetrics();
        clear();
        log.info("Stopped");
    }

    @Override
    public List<IntentEvent> getEvents() {
        synchronized (lastEvents) {
            return ImmutableList.<IntentEvent>copyOf(lastEvents);
        }
    }

    @Override
    public EventMetric intentSubmittedEventMetric() {
        return intentSubmittedEventMetric;
    }

    @Override
    public EventMetric intentInstalledEventMetric() {
        return intentInstalledEventMetric;
    }

    @Override
    public EventMetric intentWithdrawRequestedEventMetric() {
        return intentWithdrawRequestedEventMetric;
    }

    @Override
    public EventMetric intentWithdrawnEventMetric() {
        return intentWithdrawnEventMetric;
    }

    @Override
    public void event(IntentEvent event) {
        synchronized (lastEvents) {
            switch (event.type()) {
            case INSTALL_REQ:
                intentSubmittedEventMetric.eventReceived();
                break;
            case INSTALLED:
                intentInstalledEventMetric.eventReceived();
                break;
            case FAILED:
                // ignore
                break;
            case WITHDRAW_REQ:
                intentWithdrawRequestedEventMetric.eventReceived();
                break;
            case WITHDRAWN:
                intentWithdrawnEventMetric.eventReceived();
                break;
            default:
                break;
            }

            //
            // Keep only the last N events, where N = LAST_EVENTS_MAX_N
            //
            while (lastEvents.size() >= LAST_EVENTS_MAX_N) {
                lastEvents.remove();
            }
            lastEvents.add(event);
        }

        log.debug("Intent Event: time = {} type = {} event = {}",
                  event.time(), event.type(), event);
    }

    /**
     * Clears the internal state.
     */
    private void clear() {
        synchronized (lastEvents) {
            lastEvents.clear();
        }
    }

    /**
     * Registers the metrics.
     */
    private void registerMetrics() {
        intentSubmittedEventMetric =
            new EventMetric(metricsService, COMPONENT_NAME,
                            FEATURE_SUBMITTED_NAME);
        intentInstalledEventMetric =
            new EventMetric(metricsService, COMPONENT_NAME,
                            FEATURE_INSTALLED_NAME);
        intentWithdrawRequestedEventMetric =
            new EventMetric(metricsService, COMPONENT_NAME,
                            FEATURE_WITHDRAW_REQUESTED_NAME);
        intentWithdrawnEventMetric =
            new EventMetric(metricsService, COMPONENT_NAME,
                            FEATURE_WITHDRAWN_NAME);

        intentSubmittedEventMetric.registerMetrics();
        intentInstalledEventMetric.registerMetrics();
        intentWithdrawRequestedEventMetric.registerMetrics();
        intentWithdrawnEventMetric.registerMetrics();
    }

    /**
     * Removes the metrics.
     */
    private void removeMetrics() {
        intentSubmittedEventMetric.removeMetrics();
        intentInstalledEventMetric.removeMetrics();
        intentWithdrawRequestedEventMetric.removeMetrics();
        intentWithdrawnEventMetric.removeMetrics();
    }
}
