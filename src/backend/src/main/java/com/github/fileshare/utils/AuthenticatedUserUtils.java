package com.github.fileshare.utils;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.github.fileshare.config.entities.UserEntity;
import com.github.fileshare.dto.internal.SimplifiedUser;
import com.github.fileshare.exceptions.ResourceNotFoundException;
import com.github.fileshare.respositories.UserRepository;

/**
 * Utilitário para acessar informações do usuário autenticado, tanto em formato
 * enriquecido quanto simplificado.
 */
public class AuthenticatedUserUtils {

    private static UserRepository userRepository;

    private AuthenticatedUserUtils() {}

    private static final Supplier<? extends RuntimeException> DEFAULT_EXCEPTION_SUPPLIER =
            () -> new ResourceNotFoundException("Usuário não encontrado");

    /**
     * Configura o repositório de usuários utilizado para buscar informações
     * detalhadas do usuário.
     *
     * @param userRepository o repositório de usuários
     */
    public static void setRepository(UserRepository userRepository) {
        AuthenticatedUserUtils.userRepository = userRepository;
    }

    /**
     * Retorna o usuário autenticado em formato enriquecido.
     * 
     * @return o usuário autenticado
     * @throws ResourceNotFoundException se o usuário não estiver presente ou não puder ser encontrado
     */
    public static UserEntity requireEnrichedUser() {
        return requireOrThrow(getEnrichedUser(), DEFAULT_EXCEPTION_SUPPLIER);
    }

    /**
     * Retorna o usuário autenticado em formato enriquecido, utilizando um
     * fornecedor de exceção customizado caso o usuário não seja encontrado.
     *
     * @param exceptionSupplier fornecedor da exceção a ser lançada
     * @return o usuário autenticado
     * @throws RuntimeException especificada via <code>exceptionSupplier</code> se o usuário não estiver presente
     */
    public static UserEntity requireEnrichedUser(Supplier<? extends RuntimeException> exceptionSupplier) {
        return requireOrThrow(getEnrichedUser(), exceptionSupplier);
    }

    /**
     * Retorna o usuário autenticado em formato simplificado.
     *
     * @return o usuário autenticado simplificado
     * @throws ResourceNotFoundException se o usuário não estiver presente
     */
    public static SimplifiedUser requireSimplifiedUser() {
        return requireOrThrow(getSimplifiedUser(), DEFAULT_EXCEPTION_SUPPLIER);
    }

    /**
     * Retorna o usuário autenticado em formato simplificado, utilizando um
     * fornecedor de exceção customizado caso o usuário não seja encontrado.
     *
     * @param exceptionSupplier fornecedor da exceção a ser lançada
     * @return o usuário autenticado simplificado
     * @throws RuntimeException especificada via <code>exceptionSupplier</code> se o usuário não estiver presente
     */
    public static SimplifiedUser requireSimplifiedUser(Supplier<? extends RuntimeException> exceptionSupplier) {
        return requireOrThrow(getSimplifiedUser(), exceptionSupplier);
    }

    /**
     * Retorna o valor do Optional ou lança a exceção fornecida.
     *
     * @param optional o optional a ser verificado
     * @param exceptionSupplier fornecedor da exceção a ser lançada se o valor não estiver presente
     * @param <T> tipo do valor contido no Optional
     * @return o valor presente no Optional
     * @throws RuntimeException se o Optional estiver vazio
     */
    private static <T> T requireOrThrow(Optional<T> optional, Supplier<? extends RuntimeException> exceptionSupplier) {
        return optional.orElseThrow(exceptionSupplier);
    }

    /**
     * Retorna o usuário autenticado em formato enriquecido, caso exista.
     * Se o usuário não estiver enriquecido, busca no repositório e marca como
     * enriquecido.
     *
     * @return Optional com o usuário enriquecido
     */
    public static Optional<UserEntity> getEnrichedUser() {
        return getUser().map(user -> {
            if (user.isEnriched()) {
                return user;
            }
            UserEntity enrichedUser = userRepository.findByEmail(user.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Usuário não encontrado com o email: " + user.getEmail()));
            enrichedUser.setEnriched(true);
            
            Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
            if (currentAuth != null) {
                Authentication newAuth = new PreAuthenticatedAuthenticationToken(
                        enrichedUser, 
                        currentAuth.getCredentials(), 
                        currentAuth.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(newAuth);
            }
            
            return enrichedUser;
        });
    }

    /**
     * Retorna o usuário autenticado em formato simplificado, caso exista.
     *
     * @return Optional com o usuário simplificado
     */
    public static Optional<SimplifiedUser> getSimplifiedUser() {
        return getUser().map(Function.identity());
    }

    /**
     * Obtém o usuário autenticado do contexto de segurança.
     *
     * @return Optional com o usuário autenticado, ou vazio caso não haja autenticação
     */
    private static Optional<UserEntity> getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserEntity)) {
            return Optional.empty();
        }
        return Optional.of((UserEntity) authentication.getPrincipal());
    }
}

