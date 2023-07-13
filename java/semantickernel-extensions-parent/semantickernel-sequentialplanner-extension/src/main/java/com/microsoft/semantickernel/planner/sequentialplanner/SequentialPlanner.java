// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.planner.sequentialplanner;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.builders.FunctionBuilders;
import com.microsoft.semantickernel.builders.SKBuilders;
import com.microsoft.semantickernel.orchestration.SKContext;
import com.microsoft.semantickernel.planner.PlanningException;
import com.microsoft.semantickernel.planner.actionplanner.Plan;
import com.microsoft.semantickernel.semanticfunctions.PromptTemplateConfig;
import com.microsoft.semantickernel.textcompletion.CompletionSKFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Mono;

import javax.annotation.Nullable;

/** A planner that uses semantic function to create a sequential plan. */
public class SequentialPlanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SequentialPlanner.class);
    private static final String StopSequence = "<!--";

    // The name to use when creating semantic functions that are restricted from plan creation
    private static final String RestrictedSkillName = "SequentialPlanner_Excluded";

    private final SequentialPlannerRequestSettings config;
    private final SKContext context;

    // the function flow semantic function, which takes a goal and creates an xml plan that can be
    // executed
    private final CompletionSKFunction functionFlowFunction;

    public SequentialPlanner(
            Kernel kernel,
            @Nullable SequentialPlannerRequestSettings config,
            @Nullable String prompt) {
        // Verify.NotNull(kernel);

        if (config == null) {
            config = new SequentialPlannerRequestSettings();
        }

        this.config = config.addExcludedSkillName(RestrictedSkillName);

        String promptTemplate;
        if (prompt != null) {
            promptTemplate = prompt;
        } else {
            promptTemplate = EmbeddedResource.read("skprompt.txt");
        }

        this.functionFlowFunction =
                FunctionBuilders.getCompletionBuilder(kernel)
                        .createFunction(
                                promptTemplate,
                                null,
                                RestrictedSkillName,
                                "Given a request or command or goal generate a step by step plan to"
                                    + " fulfill the request using functions. This ability is also"
                                    + " known as decision making and function flow",
                                new PromptTemplateConfig.CompletionConfig(
                                        0.0, 0.0, 0.0, 0.0, this.config.getMaxTokens()));

        this.context = SKBuilders.context().build(kernel);
    }

    /**
     * Create a plan for a goal.
     *
     * @param goal The goal to create a plan for.
     * @return The plan
     */
    public Mono<Plan> createPlanAsync(String goal) {
        if (goal == null || goal.isEmpty()) {
            throw new PlanningException(
                    PlanningException.ErrorCodes.InvalidGoal, "The goal specified is empty");
        }

        try {
            return new DefaultSequentialPlannerSKContext(context)
                    .getFunctionsManualAsync(goal, this.config)
                    .flatMap(
                            relevantFunctionsManual -> {
                                SKContext updatedContext =
                                        context.setVariable(
                                                        "available_functions",
                                                        relevantFunctionsManual)
                                                .update(goal);

                                return functionFlowFunction.invokeAsync(updatedContext, null);
                            })
                    .map(
                            planResult -> {
                                String planResultString = planResult.getResult().trim();

                                LOGGER.debug("Plan result: " + planResultString);

                                Plan plan =
                                        SequentialPlanParser.toPlanFromXml(
                                                planResultString, goal, context);
                                return plan;
                            });
        } catch (Exception e) {
            throw new PlanningException(
                    PlanningException.ErrorCodes.InvalidPlan, "Plan parsing error, invalid XML", e);
        }
    }

    public SKContext getContext() {
        return context;
    }
}