package com.raishxn.gtna.planner.neoforge.crafting;

import com.raishxn.gtna.planner.api.crafting.planner.PlanningResult;

import java.util.Locale;

/**
 * Stable external rendering of {@link Ae2PlannerBridge.Diagnostics}.
 *
 * <p>
 * The diagnostics were complete but only reachable from inside the process, which the roadmap
 * lists as the missing exporter. Keys are grouped rather than flattened, an absent plan is reported
 * as {@code null} instead of as zero, and the schema version travels with the payload so a consumer
 * can tell when a field moves.
 */
public final class PlannerDiagnosticsReport {

    /** Bumped when a consumer would have to read the payload differently. */
    public static final int SCHEMA_VERSION = 1;

    private PlannerDiagnosticsReport() {}

    public static String toJson(Ae2PlannerBridge.Diagnostics diagnostics) {
        CaptureSliceMetrics.Snapshot metrics = diagnostics.captureMetrics();
        StringBuilder json = new StringBuilder(768);
        json.append("{\"schema\":").append(SCHEMA_VERSION)
                .append(",\"revision\":").append(diagnostics.revision())
                .append(",\"status\":\"").append(escape(diagnostics.status())).append('"');

        // What the last plan reasoned over. Together with the phase timings this answers "why is it
        // slow", and it costs nothing: the graph counted itself when it was compiled.
        json.append(",\"graph\":{\"keys\":").append(diagnostics.graphKeys())
                .append(",\"patterns\":").append(diagnostics.graphPatterns())
                .append(",\"edges\":").append(diagnostics.graphEdges()).append('}');

        json.append(",\"requests\":{\"inFlight\":").append(diagnostics.inFlightRequests())
                .append(",\"submitted\":").append(diagnostics.submittedRequests())
                .append(",\"deduplicated\":").append(diagnostics.deduplicatedRequests())
                .append(",\"cancelled\":").append(diagnostics.cancelledRequests())
                .append(",\"deferred\":").append(diagnostics.deferredRequests()).append('}');

        json.append(",\"cache\":{\"hits\":").append(diagnostics.cacheHits())
                .append(",\"misses\":").append(diagnostics.cacheMisses())
                .append(",\"snapshots\":").append(diagnostics.cachedSnapshots())
                .append(",\"bytes\":").append(diagnostics.cacheBytes())
                .append(",\"evictions\":").append(diagnostics.cacheEvictions()).append('}');

        json.append(",\"workers\":{\"active\":").append(diagnostics.activeWorkers())
                .append(",\"queued\":").append(diagnostics.queuedRequests()).append('}');

        json.append(",\"pressure\":{\"backpressureRejections\":").append(diagnostics.backpressureRejections())
                .append(",\"circuitState\":\"").append(escape(diagnostics.circuitState()))
                .append("\",\"circuitRejections\":").append(diagnostics.circuitRejections())
                .append(",\"consecutiveFailures\":").append(diagnostics.consecutiveFailures()).append('}');

        json.append(",\"capture\":{\"pending\":").append(diagnostics.pendingCaptures())
                .append(",\"slices\":").append(diagnostics.captureSlices())
                .append(",\"cancellations\":").append(diagnostics.captureCancellations())
                .append(",\"patterns\":").append(diagnostics.capturedPatterns())
                .append(",\"tickBudgetRemainingNanos\":").append(diagnostics.tickBudgetRemainingNanos())
                .append('}');

        json.append(",\"percentiles\":{\"slices\":").append(metrics.slices())
                .append(",\"sliceP50Nanos\":").append(metrics.sliceP50Nanos())
                .append(",\"sliceP95Nanos\":").append(metrics.sliceP95Nanos())
                .append(",\"sliceP99Nanos\":").append(metrics.sliceP99Nanos())
                .append(",\"sliceMaxNanos\":").append(metrics.sliceMaxNanos())
                .append(",\"ticks\":").append(metrics.ticks())
                .append(",\"tickP50Nanos\":").append(metrics.tickP50Nanos())
                .append(",\"tickP95Nanos\":").append(metrics.tickP95Nanos())
                .append(",\"tickP99Nanos\":").append(metrics.tickP99Nanos())
                .append(",\"overflowSamples\":").append(metrics.overflowSamples()).append('}');

        // Before the first plan there is nothing to report, and saying zero would be a lie a consumer
        // would read as a measurement.
        PlanningResultDiagnostics lastPlan = lastPlan(diagnostics);
        if (lastPlan == null) {
            json.append(",\"lastPlan\":null");
        } else {
            var shortage = lastPlan.shortage();
            json.append(",\"lastPlan\":{\"graphRevision\":").append(lastPlan.graphRevision())
                    .append(",\"operations\":").append(lastPlan.operations())
                    .append(",\"maximumDepth\":").append(lastPlan.maximumDepth())
                    .append(",\"elapsedNanos\":").append(lastPlan.elapsedNanos())
                    // The split is what tells an operator that part of a shortage is a catalyst they
                    // get back rather than material they have to find.
                    .append(",\"missingConsumable\":").append(shortage.consumable().asBigInteger())
                    .append(",\"missingSeed\":").append(shortage.seed().asBigInteger())
                    .append(",\"missingCarrier\":").append(shortage.carrier().asBigInteger())
                    .append(",\"missingConsumableKinds\":").append(shortage.consumableKinds())
                    .append(",\"missingSeedKinds\":").append(shortage.seedKinds())
                    .append(",\"missingCarrierKinds\":").append(shortage.carrierKinds())
                    // Routes refused because they would have to reach into a key the plan is already
                    // expanding. It explains a shortage that looks like it should have a route.
                    .append(",\"cycleCuts\":").append(lastPlan.cycleCuts());
            // Why the routes were chosen: one count per comparison link, in the order documented on
            // IterativeCraftingPlanner.CHOICE_LINK_COUNT, or null when the plan had nothing to choose.
            long[] links = lastPlan.routeChoiceLinks();
            if (links == null) {
                json.append(",\"routeChoiceLinks\":null");
            } else {
                json.append(",\"routeChoiceLinks\":[");
                for (int index = 0; index < links.length; index++) {
                    if (index > 0) json.append(',');
                    json.append(links[index]);
                }
                json.append(']');
            }
            json.append('}');
        }
        return json.append('}').toString();
    }

    /** The plan diagnostics as this renderer needs them, so the shape is pinned in one place. */
    private record PlanningResultDiagnostics(long graphRevision, long operations, int maximumDepth,
                                             long elapsedNanos, PlanningResult.ShortageSummary shortage,
                                             long cycleCuts, long[] routeChoiceLinks) {}

    private static PlanningResultDiagnostics lastPlan(Ae2PlannerBridge.Diagnostics diagnostics) {
        var plan = diagnostics.lastPlan();
        return plan == null ? null : new PlanningResultDiagnostics(plan.graphRevision(), plan.operations(),
                plan.maximumDepth(), plan.elapsedNanos(), plan.shortage(), plan.cycleCuts(),
                plan.choiceLinks());
    }

    private static String escape(String value) {
        StringBuilder escaped = new StringBuilder(value.length() + 8);
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format(Locale.ROOT, "\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }
        return escaped.toString();
    }
}
