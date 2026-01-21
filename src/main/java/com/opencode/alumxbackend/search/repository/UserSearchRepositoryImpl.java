package com.opencode.alumxbackend.search.repository;

import com.opencode.alumxbackend.users.dto.UserResponseDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserSearchRepositoryImpl implements UserSearchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<UserResponseDto> searchUsers(String query) {

        String jpql = """
    SELECT DISTINCT new com.opencode.alumxbackend.users.dto.UserResponseDto(
        u.id,
        u.name,
        u.email,
        u.role,
        u.createdAt
    )
    FROM User u
    LEFT JOIN u.education e
    LEFT JOIN u.experience exp
    LEFT JOIN u.internships i
    WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
       OR LOWER(u.name) LIKE LOWER(CONCAT('%', :q, '%'))
       OR LOWER(u.currentCompany) LIKE LOWER(CONCAT('%', :q, '%'))
       OR LOWER(e) LIKE LOWER(CONCAT('%', :q, '%'))
       OR LOWER(exp) LIKE LOWER(CONCAT('%', :q, '%'))
       OR LOWER(i) LIKE LOWER(CONCAT('%', :q, '%'))
""";


        return entityManager.createQuery(jpql, UserResponseDto.class)
                .setParameter("q", query)
                .getResultList();
    }
}
