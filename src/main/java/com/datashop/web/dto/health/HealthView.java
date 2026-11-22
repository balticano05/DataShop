package com.datashop.web.dto.health;

public record HealthView(String status, String timestamp, long uptime) {
}
