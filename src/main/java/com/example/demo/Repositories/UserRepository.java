    package com.example.demo.Repositories;

    import com.example.demo.models.UserModel;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;

    import java.util.Optional;

    @Repository
    public interface UserRepository extends JpaRepository<UserModel, Long> {
        Optional<UserModel> findByUserName(String username);
        Optional<UserModel> findByUserId(Long id);
        boolean existsByUserName(String username);
    }
