package org.itstep.botapp.repository;

import org.itstep.botapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
    User findUserByLogin(String login);
    User findUserByLoginAndPassword(String login, String password);
}
