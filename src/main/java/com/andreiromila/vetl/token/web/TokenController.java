package com.andreiromila.vetl.token.web;

import com.andreiromila.vetl.responses.CustomPage;
import com.andreiromila.vetl.token.Token;
import com.andreiromila.vetl.token.TokenService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

import static com.andreiromila.vetl.utils.PageableUtils.getPageableWithSafeSort;

@RestController
@RequestMapping("/api/v1/access-tokens")
public class TokenController {

    /**
     * The allowed sorting columns for the user table
     */
    public static final Set<String> sortingColumns = Set.of("id", "username", "userAgent", "enable", "expiresAt");

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping
    public ResponseEntity<CustomPage<?>> list(Pageable pageable) {

        // Get the pageable with safe sorting
        final Pageable safeSort = getPageableWithSafeSort(pageable, sortingColumns);

        // Get the requested page from the database
        final Page<Token> tokenPage = tokenService.findAll(safeSort);

        // Transform the token page to the expected output
        final List<TokenBasicResponse> tokenList = tokenPage.getContent()
                .stream()
                .map(TokenBasicResponse::from)
                .toList();

        // Create the response body
        final CustomPage<TokenBasicResponse> response = new CustomPage<>(tokenList, tokenPage.getPageable(), tokenPage.getTotalElements());

        // Return the 200 ok response
        return ResponseEntity.ok(response);

    }

}
