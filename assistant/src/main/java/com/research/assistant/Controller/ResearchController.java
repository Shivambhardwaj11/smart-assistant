package com.research.assistant.Controller;


import com.research.assistant.Services.ResearchServices;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/research")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class ResearchController {
    private final ResearchServices researchServices;

    @PostMapping("/process")
    public ResponseEntity<String> processContent(@RequestBody Researchrequest request){
        String result = researchServices.processContent(request);
        return ResponseEntity.ok(result);
    }
}
