package me.whereareiam.yui.common.service.requirement;

import lombok.RequiredArgsConstructor;
import me.whereareiam.yui.api.model.requirement.RequirementEntry;
import me.whereareiam.yui.api.model.requirement.Requirements;
import me.whereareiam.yui.api.output.requirement.RequirementContext;
import me.whereareiam.yui.api.output.requirement.RequirementEntryEvaluator;
import me.whereareiam.yui.api.output.requirement.RequirementEvaluator;
import me.whereareiam.yui.api.output.requirement.RequirementEvaluatorConfig;
import me.whereareiam.yui.api.type.RequirementOperator;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default implementation of the requirement evaluator that can be reused across different modules.
 * This service coordinates the evaluation of requirements using registered entry evaluators.
 */
@Service
@RequiredArgsConstructor
public class DefaultRequirementEvaluator implements RequirementEvaluator {
	private final List<RequirementEntryEvaluator> entryEvaluators;

	@Override
	public boolean evaluate(RequirementContext context, Requirements requirements) {
		return evaluate(context, requirements, null);
	}

	/**
	 * Evaluates requirements with a specific configuration that filters which evaluators to use.
	 *
	 * @param context      The context object containing UserProfile and original context
	 * @param requirements The requirements to evaluate
	 * @param config       Configuration specifying which requirement types to use (null for all)
	 * @return true if all requirements are met, false otherwise
	 */
	public boolean evaluate(RequirementContext context, Requirements requirements, RequirementEvaluatorConfig config) {
		if (requirements == null || requirements.getGroups() == null || requirements.getGroups().isEmpty())
			return true;

		Map<String, RequirementEntry> entries = requirements.getGroups();
		List<Boolean> results = entries.values().stream()
				.map(entry -> evaluateSingle(context, entry, config))
				.collect(Collectors.toList());

		return applyOperator(requirements.getOperator(), results);
	}

	private boolean evaluateSingle(RequirementContext context, RequirementEntry entry, RequirementEvaluatorConfig config) {
		RequirementEntryEvaluator evaluator = entryEvaluators.stream()
				.filter(ev -> ev.supports(entry))
				.filter(ev -> {
					// If no config is provided, support all types (default behavior)
					if (config == null)
						return true;

					// If config is provided, check if it supports this requirement type
					return config.supportsRequirementType(entry.getClass());
				})
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("No evaluator for requirement type: " + entry.getClass().getSimpleName()));

		boolean result = evaluator.evaluate(context, entry);
		Boolean expected = entry.getExpected();
		if (expected == null) expected = true;

		return expected == result;
	}

	private boolean applyOperator(RequirementOperator op, Collection<Boolean> values) {
		if (op == null) op = RequirementOperator.AND;

		long trues = values.stream().filter(Boolean::booleanValue).count();
		long size = values.size();

		return switch (op) {
			case AND -> trues == size;
			case OR -> trues > 0;
			case XOR -> trues == 1;
			case NOT, NOR -> trues == 0;
			case NAND -> trues != size;
		};
	}
}
