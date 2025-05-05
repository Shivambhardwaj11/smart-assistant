package com.research.assistant.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.research.assistant.Controller.Researchrequest;
import com.research.assistant.Controller.geminiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Objects;

@Service
public class ResearchServices {
    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ResearchServices(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public String processContent(Researchrequest request) {
        //building the prompt then query the api then parse the response
        String prompt = buildPrompt(request);

        //QUERY
        Map<String , Object> requestBody = Map.of(
                "contents", new Object[] {
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );

        String response = webClient.post()
                .uri(geminiApiUrl + geminiApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return extractTextFromResponse(response);
    }

    private String extractTextFromResponse(String response) {

        try {
            geminiResponse geminiResponse = objectMapper.readValue(response, geminiResponse.class);
            if(geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()){
                geminiResponse.Candidate firstCandidate = geminiResponse.getCandidates().get(0);
                if(firstCandidate.getContent() != null &&
                        firstCandidate.getContent().getParts() != null &&
                        !firstCandidate.getContent().getParts().isEmpty()) {
                    return firstCandidate.getContent().getParts().get(0).getText();
                }
            }
            return "No Content Found";
        }catch (Exception e){
            return "Error Parsing :"+ e.getMessage();
        }
    }

    private String buildPrompt(Researchrequest request){
        StringBuilder prompt = new StringBuilder();
        switch (request.getOperation()){
            case "summarize" :
                prompt.append("Provide the clean and concise summary of the following text in few sentences\n\n");
                break;

            case "suggest" :
                prompt.append("Based on the suggested content: suggest related topics and further reading. Format the response with clear headings and bullet points: \n\n");
                break;

            default:
                throw new IllegalArgumentException("Unknown operation :"+ request.getOperation());
        }
        prompt.append(request.getContent());
        return prompt.toString();
    }
}
