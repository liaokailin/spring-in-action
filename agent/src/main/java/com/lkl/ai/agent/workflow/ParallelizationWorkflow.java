
package com.lkl.ai.agent.workflow;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.Assert;

public class ParallelizationWorkflow {

	private final ChatClient chatClient;

	public ParallelizationWorkflow(ChatClient chatClient) {
		this.chatClient = chatClient;
	}


	public List<String> parallel(String prompt, List<String> inputs, int nWorkers) {
		Assert.notNull(prompt, "Prompt cannot be null");
		Assert.notEmpty(inputs, "Inputs list cannot be empty");
		Assert.isTrue(nWorkers > 0, "Number of workers must be greater than 0");

		ExecutorService executor = Executors.newFixedThreadPool(nWorkers);
		try {
			List<CompletableFuture<String>> futures = inputs.stream()
					.map(input -> CompletableFuture.supplyAsync(() -> {
						try {
							return chatClient.prompt(prompt + "\nInput: " + input).call().content();
						} catch (Exception e) {
							throw new RuntimeException("Failed to process input: " + input, e);
						}
					}, executor))
					.collect(Collectors.toList());

			// Wait for all tasks to complete
			CompletableFuture<Void> allFutures = CompletableFuture.allOf(
					futures.toArray(CompletableFuture[]::new));
			allFutures.join();

			return futures.stream()
					.map(CompletableFuture::join)
					.collect(Collectors.toList());

		} finally {
			executor.shutdown();
		}
	}

}
