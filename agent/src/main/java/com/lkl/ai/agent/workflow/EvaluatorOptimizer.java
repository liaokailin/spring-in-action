
package com.lkl.ai.agent.workflow;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.Assert;

public class EvaluatorOptimizer {

	public static final String DEFAULT_GENERATOR_PROMPT = """
			你的目标是根据输入完成任务。如果存在之前生成的反馈，
			你应该反思这些反馈以改进你的解决方案。

			关键要求：响应必须是单行有效的JSON，除明确使用\\n转义外，不得包含换行符。
			以下是必须严格遵守的格式（包括所有引号和花括号）：

			{"thoughts":"此处填写简要说明","response":"public class Example {\\n    // 代码写在这里\\n}"}

			响应字段的规则：
			1. 所有换行必须使用\\n
			2. 所有引号必须使用\\"
			3. 所有反斜杠必须双写：\\
			4. 不允许实际换行或格式化 - 所有内容必须在一行
			5. 不允许制表符或特殊字符
			6. Java代码必须完整且正确转义

			正确格式的响应示例：
			{"thoughts":"实现计数器","response":"public class Counter {\\n    private int count;\\n    public Counter() {\\n        count = 0;\\n    }\\n    public void increment() {\\n        count++;\\n    }\\n}"}

			必须严格遵循此格式 - 你的响应必须是单行有效的JSON。
			""";

	public static final String DEFAULT_EVALUATOR_PROMPT = """
			评估这段代码实现的正确性、时间复杂度和最佳实践。
			确保代码有完整的javadoc文档。
			用单行JSON格式精确响应：

			{"evaluation":"PASS,NEEDS_IMPROVEMENT,FAIL", "feedback":"你的反馈意见"}

			evaluation字段必须是以下之一: "PASS", "NEEDS_IMPROVEMENT", "FAIL"
			仅当所有标准都满足且无需改进时才使用"PASS"。
			""";


	public static record Generation(String thoughts, String response) {
	}


	public static record EvaluationResponse(Evaluation evaluation, String feedback) {

		public enum Evaluation {
			PASS, NEEDS_IMPROVEMENT, FAIL
		}
	}


	public static record RefinedResponse(String solution, List<Generation> chainOfThought) {
	}

	private final ChatClient chatClient;

	private final String generatorPrompt;

	private final String evaluatorPrompt;

	public EvaluatorOptimizer(ChatClient chatClient) {
		this(chatClient, DEFAULT_GENERATOR_PROMPT, DEFAULT_EVALUATOR_PROMPT);
	}

	public EvaluatorOptimizer(ChatClient chatClient, String generatorPrompt, String evaluatorPrompt) {
		Assert.notNull(chatClient, "ChatClient must not be null");
		Assert.hasText(generatorPrompt, "Generator prompt must not be empty");
		Assert.hasText(evaluatorPrompt, "Evaluator prompt must not be empty");

		this.chatClient = chatClient;
		this.generatorPrompt = generatorPrompt;
		this.evaluatorPrompt = evaluatorPrompt;
	}


	public RefinedResponse loop(String task) {
		List<String> memory = new ArrayList<>();
		List<Generation> chainOfThought = new ArrayList<>();

		return loop(task, "", memory, chainOfThought);
	}


	private RefinedResponse loop(String task, String context, List<String> memory,
			List<Generation> chainOfThought) {

		Generation generation = generate(task, context);
		memory.add(generation.response());
		chainOfThought.add(generation);

		EvaluationResponse evaluationResponse = evaluate(generation.response(), task);

		if (evaluationResponse.evaluation().equals(EvaluationResponse.Evaluation.PASS)) {
			// Solution is accepted!
			return new RefinedResponse(generation.response(), chainOfThought);
		}

		// Accumulated new context including the last and the previous attempts and
		// feedbacks.
		StringBuilder newContext = new StringBuilder();
		newContext.append("以前的尝试:");
		for (String m : memory) {
			newContext.append("\n- ").append(m);
		}
		newContext.append("\nFeedback: ").append(evaluationResponse.feedback());

		return loop(task, newContext.toString(), memory, chainOfThought);
	}


	private Generation generate(String task, String context) {
		Generation generationResponse = chatClient.prompt()
				.user(u -> u.text("{prompt}\n{context}\nTask: {task}")
						.param("prompt", this.generatorPrompt)
						.param("context", context)
						.param("task", task))
				.call()
				.entity(Generation.class);

		System.out.println(String.format("\n=== 输出 ===\n思考: %s\n\n返回:\n %s\n",
				generationResponse.thoughts(), generationResponse.response()));
		return generationResponse;
	}


	private EvaluationResponse evaluate(String content, String task) {

		EvaluationResponse evaluationResponse = chatClient.prompt()
				.user(u -> u.text("{prompt}\nOriginal task: {task}\nContent to evaluate: {content}")
						.param("prompt", this.evaluatorPrompt)
						.param("task", task)
						.param("content", content))
				.call()
				.entity(EvaluationResponse.class);

		System.out.println(String.format("\n=== 评价输出 ===\n评价: %s\n\n反馈: %s\n",
				evaluationResponse.evaluation(), evaluationResponse.feedback()));
		return evaluationResponse;
	}

}
