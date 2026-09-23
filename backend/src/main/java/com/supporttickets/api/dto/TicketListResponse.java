package com.supporttickets.api.dto;

import java.util.List;

public record TicketListResponse(List<TicketSummaryResponse> items) {
}
