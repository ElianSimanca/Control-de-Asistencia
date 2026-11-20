package com.asistencia.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    // Acepta tanto GET (para el navegador) como HEAD (para UptimeRobot)
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD})
    public String healthCheck() {
        return "OK";
    }
}
