package it.rasxodbot.Repositories;

import it.rasxodbot.Entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Integer> {

    Group findByGroupId(Long id);
}
