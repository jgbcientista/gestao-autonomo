package br.com.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping
    public String test() {
        return "OK - Aplicação funcionando!";
    }
    
    @GetMapping("/status")
    public String status() {
        return "Status: ATIVO";
    }
} 