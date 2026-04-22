package br.com.brunobs.auth.model;

import java.util.List;

public record AccountCacheDTO(
        Long accountId,
        String authorizationTypeDefault,
        List<GroupDetailDTO> groups
) {
    @Override
    public String toString() {
        return "AccountCacheDTO{" +
                "accountId=" + accountId +
                ", authorizationTypeDefault='" + authorizationTypeDefault + '\'' +
                ", groups=" + groups +
                '}';
    }
}