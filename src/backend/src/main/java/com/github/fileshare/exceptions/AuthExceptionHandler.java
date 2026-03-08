package com.github.fileshare.exceptions;

import java.io.IOException;
import java.io.OutputStream;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fileshare.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

	private final ObjectMapper mapper;
	
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
    		org.springframework.security.core.AuthenticationException e) throws IOException {
    	ErrorResponse exception = createResponse(response, HttpStatus.UNAUTHORIZED);
        OutputStream out = response.getOutputStream();
        mapper.writeValue(out, exception);
        out.flush();
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException {
    	ErrorResponse exception = createResponse(response, HttpStatus.FORBIDDEN);
        OutputStream out = response.getOutputStream();
        mapper.writeValue(out, exception);
        out.flush();
    }

    private ErrorResponse createResponse(HttpServletResponse response, HttpStatus status) {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        return ErrorResponse.builder()
                .code(status.getReasonPhrase())
                .message(status.getReasonPhrase())
                .build();
    }

}
